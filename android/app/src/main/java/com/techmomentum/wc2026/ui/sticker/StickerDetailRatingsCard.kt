package com.techmomentum.wc2026.ui.sticker

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.data.model.Player
import com.techmomentum.wc2026.data.model.PlayerRatings
import com.techmomentum.wc2026.data.model.isGoalkeeper
import com.techmomentum.wc2026.ui.components.AnimatedStatBar
import com.techmomentum.wc2026.ui.theme.TeamPalette
import com.techmomentum.wc2026.ui.theme.darken
import com.techmomentum.wc2026.ui.theme.lighten
import kotlinx.coroutines.delay

@Composable
fun StickerDetailRatingsCard(
    player: Player,
    palette: TeamPalette,
    modifier: Modifier = Modifier,
) {
    val ratings = player.ratings
    val isGk = player.isGoalkeeper()
    val stats = if (isGk) ratings.goalkeeperStats() else ratings.outfieldStats()
    val radarStats = stats.map { AttributeStat(it.first, it.second) }

    var animateOvr by remember(player.playerId) { mutableStateOf(false) }
    LaunchedEffect(player.playerId) {
        animateOvr = false
        delay(80)
        animateOvr = true
    }
    val displayedOvr by animateIntAsState(
        targetValue = if (animateOvr) ratings.overall else 0,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "ovr",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = palette.primary.copy(alpha = 0.4f),
                spotColor = palette.primary.copy(alpha = 0.4f),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.94f))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Player attributes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = palette.primary.darken(0.4f),
                    )
                    Text(
                        text = player.position,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.primary.darken(0.15f).copy(alpha = 0.8f),
                    )
                }
                OverallBadge(overall = displayedOvr, palette = palette)
            }

            PlayerAttributeRadarChart(
                stats = radarStats,
                palette = palette,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stats.chunked(2).forEachIndexed { rowIndex, row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEachIndexed { colIndex, (label, value) ->
                            val delayMs = (rowIndex * 2 + colIndex) * 70 + 200
                            AnimatedStatBar(
                                label = label,
                                value = value,
                                palette = palette,
                                modifier = Modifier.weight(1f),
                                animationDelayMs = delayMs,
                            )
                        }
                        if (row.size == 1) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallBadge(
    overall: Int,
    palette: TeamPalette,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .shadow(6.dp, CircleShape, ambientColor = palette.primary.copy(alpha = 0.5f))
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        palette.accentVivid.lighten(0.2f),
                        palette.primary,
                        palette.primary.darken(0.2f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = overall.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 28.sp,
            )
            Text(
                text = "OVR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

private fun PlayerRatings.outfieldStats(): List<Pair<String, Int>> = listOf(
    "PAC" to pace,
    "SHO" to shooting,
    "PAS" to passing,
    "DRI" to dribbling,
    "DEF" to defending,
    "PHY" to physical,
)

private fun PlayerRatings.goalkeeperStats(): List<Pair<String, Int>> = listOf(
    "DIV" to diving,
    "HAN" to handling,
    "KIC" to kicking,
    "REF" to reflexes,
    "SPD" to speed,
    "POS" to positioning,
)
