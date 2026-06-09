package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techmomentum.wc2026.ui.navigation.Routes
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val mainBottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Default.Home),
    BottomNavItem(Routes.ALBUM, "Album", Icons.Default.Collections),
    BottomNavItem(Routes.LEADERBOARD, "Ranks", Icons.Default.EmojiEvents),
    BottomNavItem(Routes.SLOT, "Slots", Icons.Default.SportsEsports),
    BottomNavItem(Routes.PROFILE, "Profile", Icons.Default.AccountCircle),
)

@Composable
fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = AlbumPageStyle.filterSelectedStart.copy(alpha = 0.22f),
                spotColor = AlbumPageStyle.filterSelectedStart.copy(alpha = 0.28f),
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Brush.verticalGradient(AlbumPageStyle.bottomBarGradient)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            mainBottomNavItems.forEach { item ->
                PixarBottomNavItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PixarBottomNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val iconTint = if (selected) Color.White else AlbumPageStyle.bottomNavUnselectedIcon
    val labelColor = if (selected) Color.White else AlbumPageStyle.bottomNavUnselectedLabel

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (selected) {
                    Modifier
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = AlbumPageStyle.filterSelectedStart.copy(alpha = 0.4f),
                        )
                        .background(
                            brush = AlbumPageStyle.bottomNavSelectedBrush,
                            shape = RoundedCornerShape(18.dp),
                        )
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = iconTint,
            modifier = Modifier.size(if (selected) 26.dp else 24.dp),
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = if (selected) 11.sp else 10.sp,
            ),
            color = labelColor,
        )
    }
}
