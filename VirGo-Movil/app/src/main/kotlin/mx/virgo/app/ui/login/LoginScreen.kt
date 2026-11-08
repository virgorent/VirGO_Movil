package mx.virgo.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.virgo.app.R
import mx.virgo.app.ui.auth.AuthDivider
import mx.virgo.app.ui.auth.AuthTextField
import mx.virgo.app.ui.auth.PasswordField
import mx.virgo.app.ui.auth.SocialAuthRow
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurple
import mx.virgo.core.designsystem.theme.VirGoPurpleDark
import mx.virgo.core.designsystem.theme.VirGoTheme

private val HeaderHeight = 280.dp
private val CardCornerRadius = 26.dp
private val SectionGap = 20.dp
private val FieldGap = 14.dp
private val BackButtonSize = 40.dp
private const val BACK_BUTTON_ALPHA = 0.85f
private const val KICKER_ALPHA = 0.9f

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onForgotPasswordClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Slate900,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LoginHeader(onBackClick = onBackClick)
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = CardCornerRadius, topEnd = CardCornerRadius),
                color = Color.White,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    LoginContent(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        onSubmit = { onLoginClick(email, password) },
                        onForgotPasswordClick = onForgotPasswordClick,
                    )
                    LoginFooter(onRegisterClick = onRegisterClick)
                }
            }
        }
    }
}

@Composable
private fun LoginHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(HeaderHeight)
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors = listOf(VirGoPurpleDark, Slate900),
                        ),
                ).padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = onBackClick,
                modifier =
                    Modifier
                        .size(BackButtonSize)
                        .background(
                            color = Color.White.copy(alpha = BACK_BUTTON_ALPHA),
                            shape = CircleShape,
                        ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = Slate900,
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.login_kicker),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = Color.White.copy(alpha = KICKER_ALPHA),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.login_hero_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun LoginContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Slate900,
        )
        Spacer(modifier = Modifier.height(SectionGap))
        SocialAuthRow()
        Spacer(modifier = Modifier.height(SectionGap))
        AuthDivider()
        Spacer(modifier = Modifier.height(SectionGap))
        AuthTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.field_email),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
        )
        Spacer(modifier = Modifier.height(FieldGap))
        PasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.field_password),
            imeAction = ImeAction.Done,
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            TextButton(onClick = onForgotPasswordClick) {
                Text(
                    text = stringResource(R.string.login_forgot),
                    style = MaterialTheme.typography.labelMedium,
                    color = VirGoPurple,
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        VirGoButton(onClick = onSubmit, text = stringResource(R.string.login_submit))
    }
}

@Composable
private fun LoginFooter(
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.register_no_account),
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
        )
        Spacer(modifier = Modifier.width(4.dp))
        TextButton(onClick = onRegisterClick) {
            Text(
                text = stringResource(R.string.register_switch_register),
                style = MaterialTheme.typography.labelLarge,
                color = VirGoPurple,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    VirGoTheme {
        LoginScreen()
    }
}
