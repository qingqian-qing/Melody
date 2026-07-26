package com.example.musicplayer.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object MusicApi {
    private const val BASE = "http://musicapi.chuyel.top/meting/api"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    suspend fun searchSong(keyword: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE?server=netease&type=search&id=${URLEncoder.encode(keyword, "UTF-8")}"
            val json = fetch(url)
            parseSongs(json)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getPlaylist(id: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE?server=netease&type=playlist&id=$id"
            val json = fetch(url)
            parseSongs(json)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getSongUrl(songId: Long): String = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE?server=netease&type=url&id=$songId"
            val json = fetch(url)
            val obj = gson.fromJson(json, Map::class.java)
            (obj["url"] as? String) ?: ""
        } catch (e: Exception) { "" }
    }

    suspend fun getLyrics(songId: Long): String = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE?server=netease&type=lyric&id=$songId"
            val json = fetch(url)
            val obj = gson.fromJson(json, Map::class.java)
            (obj["lrc"] as? Map<*,*>)?.get("lyric") as? String ?: ""
        } catch (e: Exception) { "" }
    }

    private fun fetch(url: String): String {
        val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
        return client.newCall(req).execute().use { it.body?.string() ?: "" }
    }

    private fun parseSongs(json: String): List<Song> {
        val arr = gson.fromJson(json, List::class.java)
        return arr.mapNotNull { item ->
            val map = item as? Map<*,*> ?: return@mapNotNull null
            Song(
                id = (map["id"] as? Number)?.toLong() ?: 0,
                name = (map["name"] as? String) ?: "",
                artist = (map["artist"] as? String) ?: "",
                album = (map["album"] as? String) ?: "",
                picUrl = (map["pic_id"] as? String)?.let { "https://p1.music.126.net/$it.jpg" } ?: ""
            )
        }
    }
}
