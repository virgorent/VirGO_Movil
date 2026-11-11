package mx.virgo.app.ui.recover

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import mx.virgo.app.LocalSnackbarHost
import mx.virgo.app.R
import mx.virgo.app.ui.auth.AuthTextField
import mx.virgo.app.ui.auth.PasswordField
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.theme.ErrorRed
import mx.virgo.core.designsystem.theme.Slate400
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurple
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.CODE_MINUTES
import mx.virgo.core.domain.GenerateRecoveryCodeUseCase
import mx.virgo.core.domain.RecoveryError
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateRecoveryUseCase

private val KickerColor = VirGoPurple
private val BackButtonSize = 40.dp
private const val BACK_BUTTON_ALPHA = 0.85f
private val SectionGap = 20.dp
private val FieldGap = 14.dp
private val HeaderGap = 12.dp
private val HintGap = 8.dp

@Composable
fun RecoverScreen(
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: RecoverViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val sentTemplate = stringResource(R.string.recover_sent_demo)
    val successTitle = stringResource(R.string.recover_success_title)
    val successBody = stringResource(R.string.recover_success_body)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RecoverEvent.Sent ->
                    scope.launch {
                        val message = sentTemplate.format(event.code)
                        snackbarHost.showSnackbar(message)
                    }
                RecoverEvent.Done -> {
                    scope.launch {
                        snackbarHost.showSnackbar("$successTitle — $successBody")
                    }
                    onDone()
                }
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        RecoverBackButton(onClick = onBackClick)
        if (!state.codeSent) {
            RecoverEmailStep(
                email = state.email,
                emailValid = state.emailValid,
                onEmailChange = viewModel::onEmailChange,
                onSend = { viewModel.sendCode() },
            )
        } else {
            RecoverCodeStep(
                email = state.email,
                code = state.code,
                onCodeChange = viewModel::onCodeChange,
                password = state.password,
                onPasswordChange = viewModel::onPasswordChange,
                confirm = state.confirm,
                onConfirmChange = viewModel::onConfirmChange,
                stepError = state.stepError,
                onResend = { viewModel.sendCode() },
                onSave = { viewModel.save() },
            )
        }
    }
}

@Composable
private fun RecoverBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier =
            modifier
                .padding(start = 20.dp, top = 16.dp)
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
}

@Composable
private fun RecoverEmailStep(
    email: String,
    emailValid: Boolean,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        RecoverHeader(
            kicker = stringResource(R.string.recover_kicker),
            title = stringResource(R.string.recover_title),
        )
        Spacer(modifier = Modifier.height(SectionGap))
        Text(
            text = stringResource(R.string.recover_lead),
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
        )
        Spacer(modifier = Modifier.height(SectionGap))
        AuthTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.field_email),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                ),
        )
        Spacer(modifier = Modifier.height(HintGap))
        Text(
            text = stringResource(R.string.recover_code_hint, CODE_MINUTES),
            style = MaterialTheme.typography.labelSmall,
            color = Slate400,
        )
        Spacer(modifier = Modifier.weight(1f))
        VirGoButton(
            onClick = onSend,
            enabled = emailValid,
            text = stringResource(R.string.recover_send),
        )
    }
}

@Composable
private fun RecoverHeader(
    kicker: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(HeaderGap))
        Text(
            text = kicker,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                ),
            color = KickerColor,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Slate900,
        )
    }
}

@Composable
private fun RecoverCodeStep(
    email: String,
    code: String,
    onCodeChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirm: String,
    onConfirmChange: (String) -> Unit,
    stepError: RecoveryError?,
    onResend: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorText =
        when (stepError) {
            RecoveryError.EXPIRED -> stringResource(R.string.recover_error_expired)
            RecoveryError.WRONG_CODE -> stringResource(R.string.recover_error_wrong_code)
            RecoveryError.SHORT_PASSWORD -> stringResource(R.string.recover_error_password)
            RecoveryError.MISMATCH -> stringResource(R.string.recover_error_mismatch)
            null -> ""
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            RecoverHeader(kicker = email, title = stringResource(R.string.recover_title_step2))
            Spacer(modifier = Modifier.height(SectionGap))
            RecoverPasswordFields(
                code = code,
                onCodeChange = onCodeChange,
                password = password,
                onPasswordChange = onPasswordChange,
                confirm = confirm,
                onConfirmChange = onConfirmChange,
            )
            if (errorText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(FieldGap))
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.labelMedium,
                    color = ErrorRed,
                )
            }
            Spacer(modifier = Modifier.height(HintGap))
            TextButton(onClick = onResend) {
                Text(
                    text = stringResource(R.string.recover_resend),
                    style = MaterialTheme.typography.labelLarge,
                    color = VirGoPurple,
                )
            }
        }
        Spacer(modifier = Modifier.height(HintGap))
        VirGoButton(
            onClick = onSave,
            text = stringResource(R.string.recover_save),
        )
    }
}

@Composable
private fun RecoverPasswordFields(
    code: String,
    onCodeChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirm: String,
    onConfirmChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AuthTextField(
            value = code,
            onValueChange = onCodeChange,
            label = stringResource(R.string.field_code),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Next,
                ),
        )
        Spacer(modifier = Modifier.height(FieldGap))
        PasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.field_new_password),
            imeAction = ImeAction.Next,
        )
        Spacer(modifier = Modifier.height(FieldGap))
        PasswordField(
            value = confirm,
            onValueChange = onConfirmChange,
            label = stringResource(R.string.field_confirm_password),
            imeAction = ImeAction.Done,
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun RecoverScreenPreview() {
    VirGoTheme {
        RecoverScreen(
            viewModel =
                RecoverViewModel(
                    ValidateEmailUseCase(),
                    GenerateRecoveryCodeUseCase(),
                    ValidateRecoveryUseCase(),
                ),
        )
    }
}
