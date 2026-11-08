package mx.virgo.app.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.virgo.app.LocalSnackbarHost
import mx.virgo.app.R
import mx.virgo.app.ui.auth.AuthDivider
import mx.virgo.app.ui.auth.AuthTextField
import mx.virgo.app.ui.auth.EmailPattern
import mx.virgo.app.ui.auth.PasswordField
import mx.virgo.app.ui.auth.SocialAuthRow
import mx.virgo.app.ui.guest.VenueSample
import mx.virgo.app.ui.guest.VenueSamples
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.theme.ErrorRed
import mx.virgo.core.designsystem.theme.Slate100
import mx.virgo.core.designsystem.theme.Slate400
import mx.virgo.core.designsystem.theme.Slate500
import mx.virgo.core.designsystem.theme.Slate900
import mx.virgo.core.designsystem.theme.VirGoPurple
import mx.virgo.core.designsystem.theme.VirGoTheme

private const val MIN_NAME_LENGTH = 3
private const val MIN_PASSWORD_LENGTH = 8
private const val PHONE_LENGTH = 10

private val CardOverlap = 26.dp
private val CardCornerRadius = 26.dp
private val SectionGap = 14.dp
private val FieldGap = 10.dp
private val BenefitsPill = RoundedCornerShape(20.dp)
private val ErrorPillShape = RoundedCornerShape(10.dp)
private val ErrorPillBg = Color(0xFFFDECEC)

/** HU-26: registros recientes de esta sesión para detectar correo duplicado (demo). */
private val registeredEmails = mutableSetOf<String>()

@Composable
fun RegisterScreen(
    venueId: Int?,
    onRegisterSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val venue = VenueSamples.firstOrNull { it.id == venueId }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(RegisterRole.CLIENT.name) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var formError by rememberSaveable { mutableStateOf("") }

    RegisterFlow(
        venue = venue,
        name = name,
        onNameChange = { name = it },
        email = email,
        onEmailChange = { email = it },
        phone = phone,
        onPhoneChange = { phone = it.filter(Char::isDigit).take(PHONE_LENGTH) },
        password = password,
        onPasswordChange = { password = it },
        role = role,
        onRoleChange = { role = it },
        submitted = submitted,
        onSubmitting = { submitted = true },
        formError = formError,
        onFormError = { formError = it },
        onRegisterSuccess = onRegisterSuccess,
        onLoginClick = onLoginClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun RegisterFlow(
    venue: VenueSample?,
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    role: String,
    onRoleChange: (String) -> Unit,
    submitted: Boolean,
    onSubmitting: () -> Unit,
    formError: String,
    onFormError: (String) -> Unit,
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedRole = RegisterRole.valueOf(role)
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val duplicateError = stringResource(R.string.register_duplicate_email)
    val greeting = stringResource(R.string.register_success_greeting, name.trim().substringBefore(' '))
    val successBody = stringResource(R.string.register_success_body)

    val formValid =
        name.trim().length >= MIN_NAME_LENGTH &&
            EmailPattern.matches(email.trim()) &&
            phone.length == PHONE_LENGTH &&
            password.length >= MIN_PASSWORD_LENGTH

    val onSubmit = {
        onSubmitting()
        onFormError("")
        when {
            !formValid -> Unit
            !registeredEmails.add(email.trim()) -> onFormError(duplicateError)
            else -> {
                scope.launch { snackbarHost.showSnackbar("$greeting — $successBody") }
                onRegisterSuccess()
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState()),
    ) {
        RegisterHero(venue = venue, onBackClick = onBackClick)
        RegisterCard(
            name = name,
            onNameChange = onNameChange,
            email = email,
            onEmailChange = onEmailChange,
            phone = phone,
            onPhoneChange = onPhoneChange,
            password = password,
            onPasswordChange = onPasswordChange,
            role = selectedRole,
            onRoleChange = { onRoleChange(it.name) },
            submitted = submitted,
            formError = formError,
            onSubmit = onSubmit,
            onLoginClick = onLoginClick,
        )
    }
}

@Composable
private fun RegisterCard(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    role: RegisterRole,
    onRoleChange: (RegisterRole) -> Unit,
    submitted: Boolean,
    formError: String,
    onSubmit: () -> Unit,
    onLoginClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .offset(y = -CardOverlap)
                .fillMaxWidth(),
        shape = RoundedCornerShape(topStart = CardCornerRadius, topEnd = CardCornerRadius),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Slate900,
            )
            Spacer(modifier = Modifier.height(SectionGap))
            RegisterBenefits()
            Spacer(modifier = Modifier.height(SectionGap))
            SocialAuthRow()
            Spacer(modifier = Modifier.height(SectionGap))
            AuthDivider()
            Spacer(modifier = Modifier.height(SectionGap))
            RolePicker(selected = role, onSelected = onRoleChange)
            RoleNote(role = role)
            Spacer(modifier = Modifier.height(SectionGap))
            RegisterForm(
                name = name,
                onNameChange = onNameChange,
                email = email,
                onEmailChange = onEmailChange,
                phone = phone,
                onPhoneChange = onPhoneChange,
                password = password,
                onPasswordChange = onPasswordChange,
                submitted = submitted,
                formError = formError,
                onSubmit = onSubmit,
            )
            Spacer(modifier = Modifier.height(SectionGap))
            RegisterFooter(onLoginClick = onLoginClick)
        }
    }
}

@Composable
private fun RegisterBenefits(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BenefitChip(
            icon = Icons.Default.Tag,
            text = stringResource(R.string.register_benefit_prices),
        )
        BenefitChip(
            icon = Icons.Default.AutoAwesome,
            text = stringResource(R.string.register_benefit_ai),
        )
        BenefitChip(
            icon = Icons.Default.CalendarMonth,
            text = stringResource(R.string.register_benefit_event),
        )
    }
}

@Composable
private fun RowScope.BenefitChip(
    icon: ImageVector,
    text: String,
) {
    Row(
        modifier =
            Modifier
                .weight(1f)
                .clip(BenefitsPill)
                .background(Slate100)
                .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VirGoPurple,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = VirGoPurple,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun RegisterForm(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    submitted: Boolean,
    formError: String,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        RegisterContactFields(
            name = name,
            onNameChange = onNameChange,
            email = email,
            onEmailChange = onEmailChange,
            phone = phone,
            onPhoneChange = onPhoneChange,
            submitted = submitted,
        )
        Spacer(modifier = Modifier.height(FieldGap))
        PasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.field_password),
            error = stringResource(R.string.error_password_min),
            showError = submitted && password.length < MIN_PASSWORD_LENGTH,
            imeAction = ImeAction.Done,
        )
        if (formError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(FieldGap))
            Text(
                text = formError,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(ErrorPillBg, ErrorPillShape)
                        .padding(horizontal = 11.dp, vertical = 9.dp),
                style = MaterialTheme.typography.labelMedium,
                color = ErrorRed,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        VirGoButton(onClick = onSubmit, text = stringResource(R.string.register_submit))
    }
}

@Composable
private fun RegisterContactFields(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    submitted: Boolean,
    modifier: Modifier = Modifier,
) {
    val nameError = stringResource(R.string.error_name)
    val emailError = stringResource(R.string.error_email)
    val phoneError = stringResource(R.string.error_phone)

    Column(modifier = modifier) {
        AuthTextField(
            value = name,
            onValueChange = onNameChange,
            label = stringResource(R.string.field_name),
            error = nameError,
            showError = submitted && name.trim().length < MIN_NAME_LENGTH,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
        )
        Spacer(modifier = Modifier.height(FieldGap))
        AuthTextField(
            value = email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.field_email),
            error = emailError,
            showError = submitted && !EmailPattern.matches(email.trim()),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
        )
        Spacer(modifier = Modifier.height(FieldGap))
        AuthTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = stringResource(R.string.field_phone),
            error = phoneError,
            showError = submitted && phone.length != PHONE_LENGTH,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
            leadingIcon = {
                Text(
                    text = stringResource(R.string.field_phone_prefix),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500,
                )
            },
        )
    }
}

@Composable
private fun RegisterFooter(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.register_have_account),
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
        )
        Spacer(modifier = Modifier.width(4.dp))
        TextButton(onClick = onLoginClick) {
            Text(
                text = stringResource(R.string.register_switch_login),
                style = MaterialTheme.typography.labelLarge,
                color = VirGoPurple,
                fontWeight = FontWeight.Bold,
            )
        }
    }
    Text(
        text = stringResource(R.string.register_terms),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelSmall,
        color = Slate400,
        textAlign = TextAlign.Center,
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    VirGoTheme {
        RegisterScreen(venueId = null)
    }
}
