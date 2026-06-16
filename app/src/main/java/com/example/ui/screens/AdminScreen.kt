package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ReportedContent
import com.example.ui.theme.*
import com.example.ui.viewmodel.BharamputraViewModel

@Composable
fun AdminScreen(viewModel: BharamputraViewModel) {
    val reports by viewModel.reportedContent.collectAsState()
    val videos by viewModel.allVideos.collectAsState()
    val channels by viewModel.allChannels.collectAsState()
    val context = LocalContext.current

    // Admin audit log
    var auditLogs by remember {
        mutableStateOf(
            listOf(
                "System: Core node initialized under Bharamputra protocols.",
                "System: Seeded 4 regional channels and 9 high performance videos.",
                "Auth: Administrator session initialized."
            )
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RiverDarkSurfaceVariant)
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateTo("main") }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Moderation Console",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = RiverSecondary)
            }
        },
        containerColor = RiverDarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics summary card
            item {
                Text("System Operations Health", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricsCard(
                        title = "Channels",
                        count = "${channels.size}",
                        color = RiverPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricsCard(
                        title = "Videos",
                        count = "${videos.size}",
                        color = RiverSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricsCard(
                        title = "Flags",
                        count = "${reports.size}",
                        color = Color.Red,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Flagged reports listing
            item {
                Text("Flagged Node Moderation Queue", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (reports.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RiverDarkSurface, shape = RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RiverSecondary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("All Nodes Clear", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Reports queue cleared successfully.", color = RiverMutedGrey, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(reports) { report ->
                    ReportedContentCard(
                        report = report,
                        onDelete = {
                            if (report.contentType == "VIDEO") {
                                viewModel.adminDeleteVideo(report.id)
                                auditLogs = auditLogs + "Moderation: Deleted Video ID: ${report.id} due to report: [${report.reportReason}]"
                            } else {
                                viewModel.adminDeleteComment(report.id)
                                auditLogs = auditLogs + "Moderation: Deleted Comment ID: ${report.id} due to report: [${report.reportReason}]"
                            }
                            Toast.makeText(context, "Content purged from nodes!", Toast.LENGTH_SHORT).show()
                        },
                        onDismiss = {
                            viewModel.adminDismissReport(report.id)
                            auditLogs = auditLogs + "Moderation: Dismissed report on ID: ${report.id}"
                            Toast.makeText(context, "Flag dismissed successfully", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Audit logging console
            item {
                Text("Chronological Audit Ledger", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = RiverDarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        auditLogs.takeLast(6).forEach { log ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(text = "•", color = RiverSecondary, modifier = Modifier.width(14.dp))
                                Text(
                                    text = log,
                                    fontSize = 11.sp,
                                    color = OnBackgroundDark.copy(alpha = 0.85f),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricsCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = RiverDarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, color = RiverMutedGrey, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = count, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun ReportedContentCard(
    report: ReportedContent,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("report_card_${report.id}"),
        colors = CardDefaults.cardColors(containerColor = RiverDarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (report.contentType == "VIDEO") Icons.Default.VideoCameraFront else Icons.Default.Comment,
                        contentDescription = null,
                        tint = RiverSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Content Type: ${report.contentType}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RiverMutedGrey
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Red.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("PENDING", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Draft content: \"${report.titleOrContent}\"",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Reason for Report: ${report.reportReason}",
                fontSize = 12.sp,
                color = Color.Yellow.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Dismiss", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete Clean", fontSize = 11.sp)
                }
            }
        }
    }
}
