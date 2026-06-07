package com.techmomentum.wc2026.ui.decks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.components.PixarFilterChip

enum class DecksTab(val label: String) {
    COLLECTION("Collection"),
    SWAP_DECK("Swap deck"),
    TRADE("Trade"),
}

@Composable
fun DecksTabRow(
    selected: DecksTab,
    onSelect: (DecksTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(DecksTab.entries.toList()) { tab ->
            PixarFilterChip(
                label = tab.label,
                selected = selected == tab,
                onClick = { onSelect(tab) },
            )
        }
    }
}
