package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.team.collectibleShadow
import com.techmomentum.wc2026.ui.theme.AnimePink
import com.techmomentum.wc2026.ui.theme.CardGold

@Composable
fun PackCard(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    val shadowColor = if (count > 0) CardGold else AnimePink.copy(alpha = 0.6f)
    val transition = rememberInfiniteTransition(label = "packPulse")
    val animatedScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "packPulseScale",
    )
    val pulseScale = if (count > 0) animatedScale else 1f

    Box(
        modifier = modifier
            .scale(pulseScale)
            .fillMaxWidth()
            .collectibleShadow(
                color = shadowColor,
                elevation = if (count > 0) 12.dp else 8.dp,
                shape = shape,
            )
            .clip(shape)
            .background(Brush.linearGradient(listOf(AnimePink, CardGold))),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                    ),
                ),
        )
        Text(
            text = "⚽",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            fontSize = 28.sp,
            color = Color.White.copy(alpha = 0.2f),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text(
                text = "Sticker packs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.92f),
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Text(
                text = if (count == 1) "unopened pack" else "unopened packs",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}
