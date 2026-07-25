package com.example.musicplayer.network

data class PlaylistResponse(val songs: List<Song>)
data class Song(
    val id: Long,
    val name: String,
    val artist: String = "",
    val album: String = "",
    val picUrl: String = "",
    val url: String = "",
    val duration: Long = 0
)

data class LyricsResponse(val lrc: LyricsData?)
data class LyricsData(val lyric: String = "")
