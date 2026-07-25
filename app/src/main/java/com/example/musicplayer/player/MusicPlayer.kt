package com.example.musicplayer.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.network.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MusicPlayer(context: Context) {
    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private val _currentSong = MutableStateFlow<Song?>()
    val currentSong: StateFlow<Song?> = _currentSong
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress
    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist
    private var currentIndex = -1

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { _isPlaying.value = playing }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    exoPlayer.duration.let { d ->
                        if (d > 0) _progress.value = exoPlayer.currentPosition.toFloat() / d
                    }
                }
            }
        })
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        _playlist.value = songs
        currentIndex = startIndex
        playIndex(startIndex)
    }

    fun playIndex(index: Int) {
        if (index !in _playlist.value.indices) return
        currentIndex = index
        _currentSong.value = _playlist.value[index]
        val song = _playlist.value[index]
        val mediaItem = MediaItem.fromUri(song.url.ifEmpty {
            "http://music.163.com/song/media/outer/url?id=${song.id}.mp3"
        })
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlay() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun next() { playIndex((currentIndex + 1) % _playlist.value.size) }
    fun prev() {
        playIndex(if (currentIndex - 1 < 0) _playlist.value.size - 1 else currentIndex - 1)
    }

    fun seekTo(frac: Float) {
        exoPlayer.duration.let { d -> if (d > 0) exoPlayer.seekTo((d * frac).toLong()) }
    }

    fun release() { exoPlayer.release() }
}
