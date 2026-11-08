package mx.virgo.app.ui.recover

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mx.virgo.app.LocalSnackbarHost
import mx.virgo.app.R
import mx.virgo.app.ui.auth.AuthTextField
import mx.virgo.app.ui.auth.EmailPattern
import mx.virgo.app.ui.auth.PasswordField
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.theme.ErrorRed
import mx.virgo.core.designsystem.theme.Slate400
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurple
import mx.virgo.core.designsystem.theme.VirGoTheme
import kotlin.random.Random

internal const val CODE_MINUTES = 15
private const val CODE_LENGTH = 6
private const val CODE_MIN = 100000
private const val CODE_MAX = 1000000

private val KickerColor = VirGoPurple
private val BackButtonSize = 40.dp
private const val BACK_BUTTON_ALPHA = 0.85f
private val SectionGap = 20.dp
private val FieldGap = 14.dp
private val HeaderGap = 12.dp
private val HintGap = 8.dp

@Composable
fun RecoverScreen(
    onDone: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var codeSent by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var demoCode by rememberSaveable { mutableStateOf("") }
    var sentAt by rememberSaveable { mutableStateOf(0L) }
    var code by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var stepError by rememberSaveable { mutableStateOf("") }

    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val errors =
        RecoverErrors(
            expired = stringResource(R.string.recover_error_expired),
            wrongCode = stringResource(R.string.recover_error_wrong_code),
            shortPassword = stringResource(R.string.recover_error_password),
            mismatch = stringResource(R.string.recover_error_mismatch),
        )

    fun sendCode() {
        demoCode = Random.nextInt(CODE_MIN, CODE_MAX).toString()
        sentAt = System.currentTimeMillis()
        codeSent = true
        stepError = ""
        val message = context.getString(R.string.recover_sent_demo, demoCode)
        scope.launch { snackbarHost.showSnackbar(message) }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        RecoverBackButton(onClick = onBackClick)
        if (!codeSent) {
            RecoverEmailStep(
                email = email,
                onEmailChange = { email = it },
                onSend = ::sendCode,
            )
        } else {
            RecoverCodeStep(
                email = email,
                code = code,
                onCodeChange = { code = it.filter(Char::isDigit).take(CODE_LENGTH) },
                password = password,
                onPasswordChange = { password = it },
                confirm = confirm,
                onConfirmChange = { confirm = it },
                stepError = stepError,
                onResend = ::sendCode,
                onSave = {
                    stepError = recoverError(demoCode, code, sentAt, password, confirm, errors)
                    if (stepError.isEmpty()) {
                        val title = context.getString(R.string.recover_success_title)
                        val body = context.getString(R.string.recover_success_body)
                        scope.launch { snackbarHost.showSnackbar("$title — $body") }
                        onDone()
                    }
                },
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
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emailValid = EmailPattern.matches(email.trim())

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
    stepError: String,
    onResend: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            if (stepError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(FieldGap))
                Text(
                    text = stepError,
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

@Preview(showBackground = true)
@Composable
fun RecoverScreenPreview() {
    VirGoTheme {
        RecoverScreen()
    }
}
