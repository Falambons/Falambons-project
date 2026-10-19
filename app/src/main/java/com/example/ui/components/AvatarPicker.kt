package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

val AVATAR_PRESETS = listOf(
    Pair(1, Icons.Default.Terminal),
    Pair(2, Icons.Default.Code),
    Pair(3, Icons.Default.SmartToy),
    Pair(4, Icons.Default.Memory),
    Pair(5, Icons.Default.DataObject),
    Pair(6, Icons.Default.Person)
)

val AVATAR_COLORS = listOf(
    Color(0xFF00E5FF), // Cyan
    Color(0xFFFF007F), // Neon Pink
    Color(0xFF22C55E), // Neon Green
    Color(0xFFA855F7), // Neon Purple
    Color(0xFFF59E0B), // Neon Amber
    Color(0xFF38BDF8)  // Sky Blue
)

@Composable
fun UserAvatar(
    avatarUri: String?,
    presetId: Int,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF161B22))
            .border(2.dp, AVATAR_COLORS.getOrElse(presetId - 1) { MaterialTheme.colorScheme.primary }, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUri.isNullOrEmpty()) {
            val context = androidx.compose.ui.platform.LocalContext.current
            AsyncImage(
                model = avatarUri,
                imageLoader = com.example.util.GifImageLoader.get(context),
                contentDescription = "Foto Profil (GIF/Gambar)",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val icon = AVATAR_PRESETS.find { it.first == presetId }?.second ?: Icons.Default.Terminal
            val color = AVATAR_COLORS.getOrElse(presetId - 1) { MaterialTheme.colorScheme.primary }
            Icon(
                imageVector = icon,
                contentDescription = "Avatar",
                tint = color,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

@Composable
fun AvatarPresetRow(
    selectedPresetId: Int,
    onSelectPreset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AVATAR_PRESETS.forEachIndexed { index, pair ->
            val isSelected = pair.first == selectedPresetId
            val color = AVATAR_COLORS.getOrElse(index) { Color.Cyan }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E222D))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) color else Color(0xFF334155),
                        shape = CircleShape
                    )
                    .clickable { onSelectPreset(pair.first) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = pair.second,
                    contentDescription = "Preset ${pair.first}",
                    tint = if (isSelected) color else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
