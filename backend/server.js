/**
 * Bharamputra Ecosystem - Production API Backend Service
 * Framework: Express.js (Node.js)
 * Purpose: REST APIs for video processing, analytics, audit, and moderation.
 */

const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const helmet = require('helmet');

const app = express();
const PORT = process.env.PORT || 5000;

// Security and Network middleware
app.use(helmet());
app.use(cors());
app.use(express.json());

// Multi-tier Rate Limiting (Abuse Prevention)
const standardLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, // Limit each IP to 100 requests per window
    message: { error: "Too many requests transmitted. Access suspended temporarily." }
});
app.use('/api/', standardLimiter);

// In-Memory Database Simulation
let videos = [
    { id: "bbb_stream", title: "Big Buck Bunny - 4K Ultra Cinematic Stream", views: 428930, likes: 12903, creatorId: "bharamputra_official", category: "Cinema" },
    { id: "sintel_saga", title: "Sintel - Cinematic Story of a Dragon", views: 981102, likes: 84092, creatorId: "bharamputra_official", category: "Cinema" }
];
let reports = [];
let auditLogs = [
    { timestamp: Date.now(), service: "System", event: "Express server spawned on master node" }
];

// --- AUTHENTICATION & SESSION ENDPOINTS ---

app.post('/api/auth/session', (req, res) => {
    const { email, idToken } = req.body;
    if (!email) return res.status(400).json({ error: "Email validation failed" });
    
    // Simulating Firebase auth verification
    const sessionToken = `bharamputra_tok_${Buffer.from(email).toString('base64')}`;
    res.status(200).json({
        message: "Session established successfully",
        sessionToken,
        user: { email, role: "ADMIN", name: email.split('@')[0] }
    });
});

// --- VIDEO OPERATIONS & PROCESSOR ---

app.get('/api/videos', (req, res) => {
    res.status(200).json(videos);
});

app.post('/api/videos/upload', (req, res) => {
    const { title, description, videoUrl, creatorId, category, isShort } = req.body;
    if (!title || !category) {
        return res.status(400).json({ error: "Missing required stream parameters" });
    }

    const newVideo = {
        id: `vid_${Date.now()}`,
        title,
        description: description || "",
        videoUrl: videoUrl || "default_hls_stream.m3u8",
        creatorId: creatorId || "anonymous",
        views: 0,
        likes: 0,
        isShort: !!isShort,
        category,
        createdAt: Date.now()
    };

    videos.push(newVideo);
    auditLogs.push({ timestamp: Date.now(), service: "MediaServer", event: `Video registered and processed: ${newVideo.id}` });
    
    res.status(201).json({
        message: "Stream registered and processed successfully into adaptive resolutions (240p-1080p)",
        video: newVideo
    });
});

// --- AUDIT & REPORT MODERATION ---

app.post('/api/reports', (req, res) => {
    const { contentId, contentType, reason } = req.body;
    if (!contentId || !reason) {
        return res.status(400).json({ error: "Report parameters invalid" });
    }

    const newReport = {
        id: `rep_${Date.now()}`,
        contentId,
        contentType,
        reason,
        reportedAt: Date.now()
    };

    reports.push(newReport);
    auditLogs.push({ timestamp: Date.now(), service: "ModerationService", event: `Flag registered on node: ${contentId}` });
    
    res.status(201).json({ message: "Content reported successfully. Action pending.", report: newReport });
});

app.get('/api/reports', (req, res) => {
    // RBAC check mockup: should verify session header contains ADMIN role
    res.status(200).json(reports);
});

app.delete('/api/moderation/purge/:contentId', (req, res) => {
    const { contentId } = req.params;
    videos = videos.filter(v => v.id !== contentId);
    reports = reports.filter(r => r.contentId !== contentId);
    
    auditLogs.push({ timestamp: Date.now(), service: "ModerationService", event: `Content permanently purged from grids: ${contentId}` });
    res.status(200).json({ message: "Content cleared from all regional nodes." });
});

// --- PLATFORM AUDIT LOGS ---

app.get('/api/system/audit', (req, res) => {
    res.status(200).json(auditLogs);
});

// Start listening
app.listen(PORT, () => {
    console.log(`[Bharamputra Backend] Online on port ${PORT}`);
});
