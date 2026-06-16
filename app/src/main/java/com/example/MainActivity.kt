package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BharamputraViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Instantiate our unified controller via the robust Provider initialization
    val viewModel = ViewModelProvider(this).get(BharamputraViewModel::class.java)

    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          BharamputraNavigationContainer(viewModel)
        }
      }
    }
  }
}

@Composable
fun BharamputraNavigationContainer(viewModel: BharamputraViewModel) {
  val route by viewModel.currentRoute.collectAsState()

  // High fidelity animated route transit dispatcher
  AnimatedContent(
    targetState = route,
    transitionSpec = {
      fadeIn() togetherWith fadeOut()
    },
    label = "navigationTransition"
  ) { targetRoute ->
    when {
      targetRoute == "splash" -> SplashScreen(viewModel)
      targetRoute == "auth" -> AuthScreen(viewModel)
      targetRoute == "main" -> MainFeedScreen(viewModel)
      targetRoute == "player" -> VideoPlayerScreen(viewModel)
      targetRoute == "admin" -> AdminScreen(viewModel)
      targetRoute == "studio" -> StudioScreen(viewModel)
      targetRoute.startsWith("channel") -> MainFeedScreen(viewModel) // default wrapper
      else -> MainFeedScreen(viewModel)
    }
  }
}
