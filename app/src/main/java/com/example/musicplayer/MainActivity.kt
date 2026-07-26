package com.example.musicplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.musicplayer.player.MusicPlayer
import com.example.musicplayer.ui.screens.PlayerScreen
import com.example.musicplayer.ui.screens.SettingsScreen
import com.example.musicplayer.ui.theme.MusicPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val player = MusicPlayer(this)
        setContent {
            MusicPlayerTheme {
                PlayerScreen(player, onSettings = {})
            }
        }
    }
}
