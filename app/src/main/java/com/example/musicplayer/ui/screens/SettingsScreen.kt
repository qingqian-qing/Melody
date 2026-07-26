package com.example.musicplayer.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var wallpapers by remember { mutableStateOf(listOf<Uri>()) }
    var selectedBg by remember { mutableStateOf<Uri?>(null) }

    val imgPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { wallpapers = wallpapers + it }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF1A1A2E)).statusBarsPadding()) {
        TopAppBar(
            title = { Text("设置", color = Color.White) },
            navigationIcon = { IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "返回", tint = Color.White)
            }},
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Column(Modifier.padding(16.dp)) {
            Text("自定义壁纸", color = Color.White,
                fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            
            if (wallpapers.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(wallpapers) { uri ->
                        AsyncImage(
                            model = uri, contentDescription = null,
                            modifier = Modifier.size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedBg = uri },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(onClick = { imgPicker.launch("image/*") }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("添加壁纸")
            }

            if (selectedBg != null) {
                Spacer(Modifier.height(8.dp))
                Text("已选择壁纸 ✓", color = Color(0xFFB39DDB), fontSize = 14.sp)
            }

            Spacer(Modifier.height(32.dp))

            Text("关于", color = Color.White,
                fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Melody - 简洁优雅的音乐播放器", color = Color.White.copy(0.6f))
            Text("支持网易云歌单导入", color = Color.White.copy(0.6f))
            Text("自定义壁纸 | 毛玻璃效果 | QQ弹弹动画",
                color = Color.White.copy(0.6f))
        }
    }
}
