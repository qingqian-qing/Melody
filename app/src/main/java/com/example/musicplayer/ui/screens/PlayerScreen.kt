package com.example.musicplayer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(player: MusicPlayer, onSettings: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentSong by player.currentSong.collectAsState()
    val playing by player.isPlaying.collectAsState()
    val progress by player.progress.collectAsState()
    val playlist by player.playlist.collectAsState()

    var showPlaylist by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Song>>(emptyList()) }
    var bgUri by remember { mutableStateOf<Uri?>() }
    var wallpapers by remember { mutableStateOf(listOf<Uri>()) }

    val scaleAnim = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(currentSong) {
        scaleAnim.animateTo(0.85f, spring(dampingRatio = 0.4f, stiffness = 300f))
        scaleAnim.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 300f))
    }

    LaunchedEffect(playing) {
        if (playing) {
            while (true) {
                rotation.animateTo(360f, animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)))
            }
        }
    }

    val imgPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { bgUri = it; wallpapers = wallpapers + it }
    }

    Box(Modifier.fillMaxSize()) {
        // Background layer
        if (bgUri != null) {
            AsyncImage(
                model = bgUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(60.dp),
                contentScale = ContentScale.Crop
            )
        } else if (currentSong?.picUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(ctx).data(currentSong!!.picUrl)
                    .crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(60.dp),
                contentScale = ContentScale.Crop
            )
        }
        // Gradient overlay
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xAA1A1A2E), Color(0xDD1A1A2E), Color(0xFF1A1A2E)))
        ))

        // Main content
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Top bar
            Row(Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, "设置", tint = Color.White)
                }
                Text("Melody", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showSearch = true }) {
                    Icon(Icons.Default.Search, "搜索", tint = Color.White)
                }
            }

            Spacer(Modifier.weight(0.3f))

            // Album art
            Box(Modifier.fillMaxWidth().aspectRatio(1f).padding(horizontal = 48.dp),
                contentAlignment = Alignment.Center) {
                Card(
                    Modifier.fillMaxSize().graphicsLayer {
                        scaleX = scaleAnim.value; scaleY = scaleAnim.value
                    },
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    if (currentSong?.picUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx).data(currentSong!!.picUrl)
                                .crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                                .graphicsLayer { rotationZ = rotation.value },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.fillMaxSize().background(Color(0xFF2D2D44)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MusicNote, null,
                                tint = Color.White.copy(0.3f),
                                modifier = Modifier.size(80.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.weight(0.2f))

            // Song info
            Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(currentSong?.name ?: "未播放", color = Color.White,
                    fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(currentSong?.artist ?: "选择一首歌吧", color = Color.White.copy(0.6f), fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Progress
            Slider(
                value = progress,
                onValueChange = { player.seekTo(it) },
                modifier = Modifier.padding(horizontal = 32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White, activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(0.2f))
            )

            // Controls
            Row(Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showPlaylist = true }) {
                    Icon(Icons.Default.List, "歌单", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = { player.prev() }) {
                    Icon(Icons.Default.SkipPrevious, "上一首", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                IconButton(onClick = { player.togglePlay() }, modifier = Modifier.size(64.dp)) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (playing) "暂停" else "播放",
                        tint = Color.White, modifier = Modifier.size(48.dp))
                }
                IconButton(onClick = { player.next() }) {
                    Icon(Icons.Default.SkipNext, "下一首", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                IconButton(onClick = { imgPicker.launch("image/*") }) {
                    Icon(Icons.Default.Wallpaper, "壁纸", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        // Search dialog
        if (showSearch) {
            AlertDialog(onDismissRequest = { showSearch = false },
                title = { Text("搜索网易云歌曲") },
                text = {
                    Column {
                        OutlinedTextField(searchQuery, { searchQuery = it },
                            label = { Text("歌曲名") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch {
                                searchResults = MusicApi.searchSong(searchQuery)
                            }
                        }) { Text("搜索") }
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(Modifier.height(200.dp)) {
                            items(searchResults) { song ->
                                Row(Modifier.fillMaxWidth().clickable {
                                    val list = listOf(song)
                                    player.setPlaylist(list)
                                    showSearch = false
                                }.padding(8.dp)) {
                                    Text("${song.name} - ${song.artist}",
                                        color = Color.White, maxLines = 1)
                                }
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showSearch = false }) { Text("关闭") } })
        }

        // Playlist sheet
        if (showPlaylist) {
            Box(Modifier.fillMaxSize().background(Color(0xEE1A1A2E)).clickable { showPlaylist = false }) {
                Card(Modifier.fillMaxWidth().fillMaxHeight(0.5f).align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D44))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("播放列表", color = Color.White,
                            fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn {
                            items(playlist) { song ->
                                Row(Modifier.fillMaxWidth()
                                    .clickable {
                                        val idx = playlist.indexOf(song)
                                        if (idx >= 0) player.playIndex(idx)
                                        showPlaylist = false
                                    }.padding(12.dp)) {
                                    Text("${song.name}", color = Color.White, fontSize = 15.sp)
                                    Spacer(Modifier.weight(1f))
                                    Text(song.artist, color = Color.White.copy(0.5f),
                                        fontSize = 13.sp, maxLines = 1)
                                }
                            }
                        }
                        
                        // Playlist import
                        var plId by remember { mutableStateOf("18182198856") }
                        OutlinedTextField(plId, { plId = it },
                            label = { Text("网易云歌单ID", color = Color.White.copy(0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch {
                                val songs = MusicApi.getPlaylist(plId)
                                if (songs.isNotEmpty()) player.setPlaylist(songs)
                            }
                        }) { Text("导入歌单") }
                    }
                }
            }
        }
    }
}
