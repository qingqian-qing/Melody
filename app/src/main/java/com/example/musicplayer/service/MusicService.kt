package com.example.musicplayer.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MusicService : MediaSessionService() {
    override fun onCreate() {
        super.onCreate()
        val session = MediaSession.Builder(this, Unit).build()
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = null
    override fun onTaskRemoved(rootIntent: Intent?) { stopSelf() }
}
