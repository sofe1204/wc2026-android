package com.techmomentum.wc2026.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.techmomentum.wc2026.ui.theme.CardGold
import com.techmomentum.wc2026.ui.theme.darken
import java.io.File

@Composable
fun ProfileIdentityCard(
    profile: UserProfile?,
    email: String,
    avatarUploading: Boolean,
    onChangePhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    val fullName = profile?.let {
        listOf(it.firstName, it.lastName).filter { part -> part.isNotBlank() }.joinToString(" ")
    }.orEmpty().ifBlank { profile?.displayName.orEmpty() }
    val username = profile?.username.orEmpty()
    val initials = profileInitials(fullName.ifBlank { username })
    val photoModel = avatarModel(profile?.photoUrl.orEmpty())
    val frameBrush = Brush.linearGradient(
        listOf(
            CardGold.copy(alpha = 0.7f),
            AlbumPageStyle.headerAccent.copy(alpha = 0.45f),
            CardGold.copy(alpha = 0.55f),
        ),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.25f),
            )
            .clip(shape)
            .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
            .border(width = 2.dp, brush = frameBrush, shape = shape)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(84.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(6.dp, CircleShape, ambientColor = AlbumPageStyle.headerAccent.copy(alpha = 0.35f))
                    .clip(CircleShape)
                    .border(3.dp, frameBrush, CircleShape)
                    .clickable(enabled = !avatarUploading, onClick = onChangePhoto)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                AlbumPageStyle.headerAccentVivid,
                                AlbumPageStyle.headerAccent,
                            ),
                        ),
                    ),
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
                        .size(28.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(AlbumPageStyle.headerAccent)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable(onClick = onChangePhoto),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Change photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        Text(
            text = if (avatarUploading) "Uploading photo…" else "Tap photo to change",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = AlbumPageStyle.bottomNavUnselectedIcon,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (username.isNotBlank()) {
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = AlbumPageStyle.headerAccent.darken(0.1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (fullName.isNotBlank()) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AlbumPageStyle.bottomNavUnselectedLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (profile?.countryName?.isNotBlank() == true) {
                ProfileMetaChip(
                    text = "${countryFlagEmoji(profile.countryCode)} ${profile.countryName}",
                )
            }
            if (email.isNotBlank()) {
                ProfileMetaChip(text = email)
            }
        }

    }
}

private fun avatarModel(photoUrl: String): Any? {
    if (photoUrl.isBlank()) return null
    return if (photoUrl.startsWith("http", ignoreCase = true)) {
        photoUrl
    } else {
        File(photoUrl)
    }
}

@Composable
private fun ProfileMetaChip(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AlbumPageStyle.filterUnselectedFill)
            .border(1.dp, AlbumPageStyle.filterUnselectedBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = AlbumPageStyle.bottomNavUnselectedLabel,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
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
