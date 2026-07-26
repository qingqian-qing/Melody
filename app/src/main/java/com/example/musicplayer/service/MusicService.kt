package com.example.musicplayer.service

import android.content.Intent
import android.os.IBinder
import android.app.Service

class MusicService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onTaskRemoved(rootIntent: Intent?) { stopSelf() }
}