package com.example.musicplayer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.musicplayer.network.MusicApi
import com.example.musicplayer.network.Song
import com.example.musicplayer.player.MusicPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlayerScreen(player: MusicPlayer, onSettings: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentSong by player.currentSong.collectAsState()
    val playing by player.isPlaying.collectAsState()
    val progress by player.progress.collectAsState()
    val playlist by player.playlist.collectAsState()

    var showPlaylist by remember { mutableStateOf(false) }
    var bgUri by remember { mutableStateOf<Uri?>(null) }
    var lyrics by remember { mutableStateOf("") }
    var currentLrcIndex by remember { mutableIntStateOf(0) }

    val scaleAnim = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }

    // Load default playlist on first launch
    LaunchedEffect(Unit) {
        if (playlist.isEmpty()) {
            val songs = MusicApi.getPlaylist("18182198856")
            if (songs.isNotEmpty()) player.setPlaylist(songs)
        }
    }

    // Scale animation on song change
    LaunchedEffect(currentSong) {
        scaleAnim.animateTo(0.85f, spring(dampingRatio = 0.4f, stiffness = 300f))
        scaleAnim.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 300f))
        // Load lyrics
        currentSong?.let {
            lyrics = MusicApi.getLyrics(it.id)
            currentLrcIndex = 0
        }
    }

    // Rotation animation
    LaunchedEffect(playing) {
        if (playing) {
            while (true) {
                rotation.animateTo(rotation.value + 360f, tween(8000, easing = LinearEasing))
            }
        }
    }

    // Progress-based lyric sync
    LaunchedEffect(progress) {
        if (lyrics.isNotEmpty()) {
            val lines = lyrics.split("\n").filter { it.contains("]") }
            val totalLines = lines.size
            if (totalLines > 0) {
                currentLrcIndex = ((progress * totalLines).toInt()).coerceIn(0, totalLines - 1)
            }
        }
    }

    val imgPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { bgUri = it }
    }

    // Parse lyrics for display
    val lyricLines = remember(lyrics) {
        lyrics.split("\n").map { line ->
            val timeEnd = line.indexOf(']')
            if (timeEnd > 0) line.substring(timeEnd + 1).trim() else line.trim()
        }.filter { it.isNotEmpty() }
    }

    Box(Modifier.fillMaxSize()) {
        // Background layer
        if (bgUri != null) {
            AsyncImage(model = bgUri, contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(60.dp),
                contentScale = ContentScale.Crop)
        } else if (currentSong?.picUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(currentSong!!.picUrl).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(60.dp),
                contentScale = ContentScale.Crop)
        }
        // Gradient overlay
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xAA1A1A2E), Color(0xDD1A1A2E), Color(0xFF1A1A2E)))
        ))

        // Main content
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 16.dp)) {
            // Top bar
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("純粋の浅", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = { imgPicker.launch("image/*") }) {
                        Icon(Icons.Default.Wallpaper, "壁紙", tint = Color.White.copy(0.7f), modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, "設定", tint = Color.White.copy(0.7f), modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(Modifier.weight(0.15f))

            // Album art - rotating circle
            Box(Modifier.fillMaxWidth().aspectRatio(1f).padding(horizontal = 56.dp),
                contentAlignment = Alignment.Center) {
                Card(
                    Modifier.fillMaxSize().graphicsLayer {
                        scaleX = scaleAnim.value; scaleY = scaleAnim.value
                    },
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                ) {
                    if (currentSong?.picUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx).data(currentSong!!.picUrl).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation.value },
                            contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.fillMaxSize().background(Color(0xFF2D2D44)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, null, tint = Color.White.copy(0.2f), modifier = Modifier.size(60.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Song info
            Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currentSong?.name ?: "加载中...", color = Color.White,
                    fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(currentSong?.artist ?: "", color = Color.White.copy(0.5f), fontSize = 14.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Lyrics (2 lines)
            Box(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center) {
                if (lyricLines.isNotEmpty() && currentLrcIndex < lyricLines.size) {
                    Text(lyricLines[currentLrcIndex],
                        color = Color.White.copy(0.7f), fontSize = 14.sp,
                        textAlign = TextAlign.Center, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            Slider(
                value = progress,
                onValueChange = { player.seekTo(it) },
                modifier = Modifier.padding(horizontal = 32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White, activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(0.15f)))

            // Controls
            Row(Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showPlaylist = !showPlaylist }) {
                    Icon(Icons.Default.List, "歌单", tint = Color.White.copy(0.6f), modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { player.prev() }) {
                    Icon(Icons.Default.SkipPrevious, "上一首", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { player.togglePlay() }, modifier = Modifier.size(56.dp)) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (playing) "暂停" else "播放",
                        tint = Color.White, modifier = Modifier.size(40.dp))
                }
                IconButton(onClick = { player.next() }) {
                    Icon(Icons.Default.SkipNext, "下一首", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = {
                    scope.launch {
                        val songs = MusicApi.getPlaylist("18182198856")
                        if (songs.isNotEmpty()) player.setPlaylist(songs)
                    }
                }) {
                    Icon(Icons.Default.Refresh, "刷新", tint = Color.White.copy(0.6f), modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.weight(0.1f))
        }

        // Playlist bottom sheet
        if (showPlaylist) {
            Box(Modifier.fillMaxSize().background(Color(0xCC1A1A2E)).clickable { showPlaylist = false }) {
                Card(Modifier.fillMaxWidth().fillMaxHeight(0.55f).align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252540))) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("播放列表", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            var plId by remember { mutableStateOf("") }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(plId, { plId = it },
                                    modifier = Modifier.width(120.dp).height(48.dp),
                                    singleLine = true,
                                    placeholder = { Text("歌单ID", color = Color.White.copy(0.3f), fontSize = 12.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFB39DDB), unfocusedBorderColor = Color.White.copy(0.2f)))
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = {
                                    scope.launch {
                                        val id = plId.ifEmpty { "18182198856" }
                                        val songs = MusicApi.getPlaylist(id)
                                        if (songs.isNotEmpty()) player.setPlaylist(songs)
                                    }
                                }) {
                                    Icon(Icons.Default.Download, "加载", tint = Color(0xFFB39DDB), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(Modifier.weight(1f)) {
                            itemsIndexed(playlist) { idx, song ->
                                val isCurrent = currentSong?.id == song.id
                                Row(Modifier.fillMaxWidth().clickable { player.playIndex(idx); showPlaylist = false }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .background(if (isCurrent) Color(0xFFB39DDB).copy(0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    if (isCurrent) {
                                        Icon(Icons.Default.PlayArrow, null, tint = Color(0xFFB39DDB), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(song.name, color = if (isCurrent) Color(0xFFB39DDB) else Color.White,
                                            fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(song.artist, color = Color.White.copy(0.4f), fontSize = 12.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
