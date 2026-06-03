package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PixarFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedBrush: Brush = Brush.horizontalGradient(
        listOf(Color(0xFF2A8F5E), Color(0xFF5BC48A)),
    ),
    selectedTextColor: Color = Color.White,
    unselectedFill: Color = Color(0xFFF5F8F6),
    unselectedBorder: Color = Color(0xFFD8E6DE),
    unselectedTextColor: Color = Color(0xFF2D4A3A),
) {
    Text(
        text = label,
        modifier = modifier
            .shadow(
                elevation = if (selected) 4.dp else 0.dp,
                shape = RoundedCornerShape(50),
                ambientColor = Color(0xFF2A8F5E).copy(alpha = 0.35f),
            )
            .clip(RoundedCornerShape(50))
            .then(
                if (selected) {
                    Modifier.background(brush = selectedBrush, shape = RoundedCornerShape(50))
                } else {
                    Modifier
                        .background(unselectedFill, RoundedCornerShape(50))
                        .border(1.dp, unselectedBorder, RoundedCornerShape(50))
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = if (selected) selectedTextColor else unselectedTextColor,
    )
}
