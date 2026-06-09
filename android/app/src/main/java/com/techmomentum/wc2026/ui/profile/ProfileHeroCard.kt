package com.techmomentum.wc2026.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.techmomentum.wc2026.data.model.UserProfile
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import java.io.File

@Composable
fun ProfileHeroCard(
    profile: UserProfile?,
    email: String,
    avatarUploading: Boolean,
    onChangePhoto: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fullName = profile?.let {
        listOf(it.firstName, it.lastName).filter { part -> part.isNotBlank() }.joinToString(" ")
    }.orEmpty().ifBlank { profile?.displayName.orEmpty() }
    val username = profile?.username.orEmpty()
    val initials = profileInitials(fullName.ifBlank { username })
    val photoModel = avatarModel(profile?.photoUrl.orEmpty())
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.2f),
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        AlbumPageStyle.headerAccentVivid,
                        AlbumPageStyle.headerAccent,
                    ),
                ),
            )
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "COLLECTOR PROFILE",
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.95f),
                )
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable(enabled = !avatarUploading, onClick = onChangePhoto),
                        contentAlignment = Alignment.Center,
                    ) {
                        when {
                            avatarUploading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                )
                            }
                            photoModel != null -> {
                                AsyncImage(
                                    model = photoModel,
                                    contentDescription = "Profile photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            else -> {
                                Text(
                                    text = initials,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                    if (!avatarUploading) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable(onClick = onChangePhoto),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Change photo",
                                tint = AlbumPageStyle.headerAccent,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (username.isNotBlank()) {
                        Text(
                            text = "@$username",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (fullName.isNotBlank()) {
                        Text(
                            text = fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.92f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (profile?.countryName?.isNotBlank() == true) {
                        Text(
                            text = "${countryFlagEmoji(profile.countryCode)} ${profile.countryName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                    if (email.isNotBlank()) {
                        Text(
                            text = email,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Text(
                text = if (avatarUploading) "Uploading photo…" else "Tap avatar to update photo",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

private fun avatarModel(photoUrl: String): Any? {
    if (photoUrl.isBlank()) return null
    return if (photoUrl.startsWith("http", ignoreCase = true)) photoUrl else File(photoUrl)
}

private fun profileInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].first()}${parts[1].first()}".uppercase()
    }
}

private fun countryFlagEmoji(code: String): String {
    if (code.length != 2) return "🌍"
    val upper = code.uppercase()
    val first = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
    val second = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(first)) + String(Character.toChars(second))
}
