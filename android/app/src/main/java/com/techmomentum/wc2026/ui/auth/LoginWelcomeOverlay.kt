package com.techmomentum.wc2026.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.components.AppLogo
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.CardGold
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

private const val WELCOME_DURATION_MS = 2_400L

@Composable
fun LoginWelcomeOverlay(
    title: String,
    subtitle: String,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backdropAlpha = remember { Animatable(0f) }
    val cardScale = remember { Animatable(0.72f) }
    val cardAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val particles = remember {
        List(14) { index ->
            WelcomeParticle(
                emoji = listOf("⚽", "✨", "🏆", "🎴", "⭐", "🎉")[index % 6],
                angle = Random.nextFloat() * 360f,
                distance = 80f + Random.nextFloat() * 120f,
                delayMs = 120 + index * 40,
            )
        }
    }
    val particleProgress = remember { particles.map { Animatable(0f) } }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch {
                backdropAlpha.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
            }
            launch {
                delay(80)
                cardAlpha.animateTo(1f, tween(280))
                cardScale.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                )
            }
            launch {
                delay(420)
                textAlpha.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
            }
            launch {
                particles.forEachIndexed { index, particle ->
                    launch {
                        delay(particle.delayMs.toLong())
                        particleProgress[index].animateTo(
                            1f,
                            tween(900, easing = FastOutSlowInEasing),
                        )
                    }
                }
            }
        }
        delay(WELCOME_DURATION_MS)
        backdropAlpha.animateTo(0f, tween(280))
        cardAlpha.animateTo(0f, tween(220))
        textAlpha.animateTo(0f, tween(180))
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(backdropAlpha.value)
            .background(Color.Black.copy(alpha = 0.42f)),
        contentAlignment = Alignment.Center,
    ) {
        particles.forEachIndexed { index, particle ->
            val progress = particleProgress[index].value
            val radians = Math.toRadians(particle.angle.toDouble())
            val dx = (kotlin.math.cos(radians) * particle.distance * progress).toFloat()
            val dy = (kotlin.math.sin(radians) * particle.distance * progress).toFloat()
            Text(
                text = particle.emoji,
                fontSize = (18 + index % 3 * 4).sp,
                modifier = Modifier
                    .offset { IntOffset(dx.roundToInt(), dy.roundToInt()) }
                    .alpha((1f - progress).coerceIn(0f, 1f))
                    .scale(0.8f + progress * 0.5f),
            )
        }

        Column(
            modifier = Modifier
                .scale(cardScale.value)
                .alpha(cardAlpha.value)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(AlbumPageStyle.pageFrameGradient))
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppLogo(size = 120.dp)
            Text(
                text = title,
                modifier = Modifier
                    .padding(top = 18.dp)
                    .alpha(textAlpha.value),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = AlbumPageStyle.headerAccent,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .alpha(textAlpha.value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AlbumPageStyle.bottomNavUnselectedIcon,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "🎉",
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(36.dp)
                    .alpha(textAlpha.value),
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = "✨",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 72.dp, end = 36.dp)
                .alpha(textAlpha.value * 0.9f),
            fontSize = 32.sp,
            color = CardGold,
        )
        Text(
            text = "🏆",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 88.dp, start = 32.dp)
                .alpha(textAlpha.value * 0.85f),
            fontSize = 30.sp,
        )
    }
}

private data class WelcomeParticle(
    val emoji: String,
    val angle: Float,
    val distance: Float,
    val delayMs: Int,
)
