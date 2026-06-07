package com.techmomentum.wc2026.ui.album

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.techmomentum.wc2026.ui.components.PixarOutlinedTextField

@Composable
fun AlbumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    PixarOutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = "Search nations",
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                )
            }
        },
    )
}
