package com.techmomentum.wc2026.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.techmomentum.wc2026.ui.components.PixarOutlinedTextField
import com.techmomentum.wc2026.ui.components.PixarPrimaryButton
import com.techmomentum.wc2026.ui.components.rememberDismissKeyboardAction
import com.techmomentum.wc2026.ui.theme.AlbumPageStyle
import com.techmomentum.wc2026.ui.theme.darken

@Composable
fun AuthEmailCard(
    isSignUp: Boolean,
    displayName: String,
    email: String,
    confirmEmail: String,
    password: String,
    confirmPassword: String,
    error: String?,
    loading: Boolean,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onConfirmEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val dismissKeyboard = rememberDismissKeyboardAction()
    val submitWithKeyboardDismiss = {
        dismissKeyboard()
        onSubmit()
    }
    val submitKeyboardActions = KeyboardActions(onDone = { submitWithKeyboardDismiss() })

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
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (isSignUp) "Sign up with email" else "Sign in with email",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AlbumPageStyle.headerAccent.darken(0.1f),
        )
        Text(
            text = "Use email and password for a regular account.",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = AlbumPageStyle.bottomNavUnselectedIcon,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        if (isSignUp) {
            PixarOutlinedTextField(
                value = displayName,
                onValueChange = onDisplayNameChange,
                label = "Display name",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        PixarOutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        if (isSignUp) {
            PixarOutlinedTextField(
                value = confirmEmail,
                onValueChange = onConfirmEmailChange,
                label = "Confirm email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        PixarOutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done,
            ),
            keyboardActions = if (isSignUp) KeyboardActions.Default else submitKeyboardActions,
            modifier = Modifier.fillMaxWidth(),
        )
        if (isSignUp) {
            PixarOutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Confirm password",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = submitKeyboardActions,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        error?.let { AuthErrorPill(message = it) }
        Spacer(Modifier.height(4.dp))
        PixarPrimaryButton(
            text = if (isSignUp) "Create account" else "Sign in",
            onClick = submitWithKeyboardDismiss,
            enabled = !loading,
            loading = loading,
        )
        Text(
            text = if (isSignUp) {
                "Already have an account? Sign in"
            } else {
                "New collector? Sign up"
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        dismissKeyboard()
                        onToggleMode()
                    },
                ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = AlbumPageStyle.headerAccent,
            textAlign = TextAlign.Center,
        )
    }
}
