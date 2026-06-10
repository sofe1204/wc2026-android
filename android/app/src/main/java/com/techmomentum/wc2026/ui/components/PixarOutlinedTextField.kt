package com.techmomentum.wc2026.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle

@Composable
fun PixarOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(14.dp)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = singleLine,
        readOnly = readOnly,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AlbumPageStyle.filterUnselectedFill,
            unfocusedContainerColor = AlbumPageStyle.filterUnselectedFill,
            disabledContainerColor = AlbumPageStyle.filterUnselectedFill,
            focusedBorderColor = AlbumPageStyle.filterSelectedStart,
            unfocusedBorderColor = AlbumPageStyle.filterUnselectedBorder,
            focusedLabelColor = AlbumPageStyle.headerAccent,
            unfocusedLabelColor = AlbumPageStyle.bottomNavUnselectedIcon,
            cursorColor = AlbumPageStyle.headerAccent,
            focusedTextColor = AlbumPageStyle.bottomNavUnselectedLabel,
            unfocusedTextColor = AlbumPageStyle.bottomNavUnselectedLabel,
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
    )
}
