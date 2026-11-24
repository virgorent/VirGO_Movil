package mx.virgo.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.virgo.app.LocalSnackbarHost
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import mx.virgo.core.designsystem.components.VirGoTextField

private val FieldErrorTopPadding = 6.dp
private const val LARGE_FONT_SCALE = 1.5f

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String = "",
    showError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = modifier) {
        VirGoTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = label,
            isError = showError && error.isNotEmpty(),
            keyboardOptions = keyboardOptions,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            modifier =
                Modifier.semantics {
                    contentDescription = label
                    if (showError && error.isNotEmpty()) error(error)
                },
        )
        if (showError && error.isNotEmpty()) {
            Text(
                text = error,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                modifier =
                    Modifier.semantics { liveRegion = LiveRegionMode.Polite }.padding(
                        top = FieldErrorTopPadding,
                        start = 16.dp,
                    ),
            )
        }
    }
}

@Composable
internal fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String = "",
    showError: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    AuthTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        error = error,
        showError = showError,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction,
            ),
        visualTransformation =
            if (visible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
        trailingIcon = {
            IconButton(
                onClick = { visible = !visible },
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Icon(
                    imageVector =
                        if (visible) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                    contentDescription =
                        stringResource(
                            if (visible) {
                                R.string.field_hide_password
                            } else {
                                R.string.field_show_password
                            },
                        ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
internal fun AuthDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = stringResource(R.string.auth_divider),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
internal fun SocialAuthRow(modifier: Modifier = Modifier) {
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val googleMessage =
        stringResource(R.string.common_soon_title) + ": " + stringResource(R.string.common_soon_google)
    val appleMessage =
        stringResource(R.string.common_soon_title) + ": " + stringResource(R.string.common_soon_apple)

    val onGoogleClick: () -> Unit = { scope.launch { snackbarHost.showSnackbar(googleMessage) } }
    val onAppleClick: () -> Unit = { scope.launch { snackbarHost.showSnackbar(appleMessage) } }
    if (LocalDensity.current.fontScale >= LARGE_FONT_SCALE) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SocialAuthButton(R.string.social_google, onGoogleClick, Modifier.fillMaxWidth())
            SocialAuthButton(R.string.social_apple, onAppleClick, Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SocialAuthButton(R.string.social_google, onGoogleClick, Modifier.weight(1f))
            SocialAuthButton(R.string.social_apple, onAppleClick, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SocialAuthButton(
    labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VirGoOutlinedButton(onClick = onClick, modifier = modifier) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
