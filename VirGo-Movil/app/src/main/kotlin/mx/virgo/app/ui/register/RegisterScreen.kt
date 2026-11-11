package mx.virgo.app.ui.register

import android.annotation.SuppressLint
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import mx.virgo.app.LocalSnackbarHost
import mx.virgo.app.R
import mx.virgo.app.ui.auth.AuthDivider
import mx.virgo.app.ui.auth.AuthTextField
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
import mx.virgo.core.domain.ValidateEmailUseCase
import mx.virgo.core.domain.ValidateRegisterFormUseCase

private val CardOverlap = 26.dp
private val CardCornerRadius = 26.dp
private val SectionGap = 14.dp
private val FieldGap = 10.dp
private val BenefitsPill = RoundedCornerShape(20.dp)
private val ErrorPillShape = RoundedCornerShape(10.dp)
private val ErrorPillBg = Color(0xFFFDECEC)

@Composable
fun RegisterScreen(
    venueId: Int?,
    modifier: Modifier = Modifier,
    onRegisterSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val venue = VenueSamples.firstOrNull { it.id == venueId }
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val greeting =
        stringResource(R.string.register_success_greeting, state.name.trim().substringBefore(' '))
    val successBody = stringResource(R.string.register_success_body)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterEvent.Registered -> {
                    scope.launch { snackbarHost.showSnackbar("$greeting — $successBody") }
                    onRegisterSuccess()
                }
            }
        }
    }

    RegisterFlow(
        venue = venue,
        name = state.name,
        onNameChange = viewModel::onNameChange,
        email = state.email,
        onEmailChange = viewModel::onEmailChange,
        phone = state.phone,
        onPhoneChange = viewModel::onPhoneChange,
        password = state.password,
        onPasswordChange = viewModel::onPasswordChange,
        role = state.role,
        onRoleChange = viewModel::onRoleChange,
        showNameError = state.submitted && !state.nameValid,
        showEmailError = state.submitted && !state.emailValid,
        showPhoneError = state.submitted && !state.phoneValid,
        showPasswordError = state.submitted && !state.passwordValid,
        formError =
            when (state.formError) {
                RegisterFormError.DUPLICATE -> stringResource(R.string.register_duplicate_email)
                null -> ""
            },
        onSubmit = viewModel::submit,
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
    role: RegisterRole,
    onRoleChange: (RegisterRole) -> Unit,
    showNameError: Boolean,
    showEmailError: Boolean,
    showPhoneError: Boolean,
    showPasswordError: Boolean,
    formError: String,
    onSubmit: () -> Unit,
    onLoginClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            role = role,
            onRoleChange = onRoleChange,
            showNameError = showNameError,
            showEmailError = showEmailError,
            showPhoneError = showPhoneError,
            showPasswordError = showPasswordError,
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
    showNameError: Boolean,
    showEmailError: Boolean,
    showPhoneError: Boolean,
    showPasswordError: Boolean,
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
                showNameError = showNameError,
                showEmailError = showEmailError,
                showPhoneError = showPhoneError,
                showPasswordError = showPasswordError,
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
    showNameError: Boolean,
    showEmailError: Boolean,
    showPhoneError: Boolean,
    showPasswordError: Boolean,
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
            showNameError = showNameError,
            showEmailError = showEmailError,
            showPhoneError = showPhoneError,
        )
        Spacer(modifier = Modifier.height(FieldGap))
        PasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.field_password),
            error = stringResource(R.string.error_password_min),
            showError = showPasswordError,
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
    showNameError: Boolean,
    showEmailError: Boolean,
    showPhoneError: Boolean,
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
            showError = showNameError,
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
            showError = showEmailError,
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
            showError = showPhoneError,
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

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    VirGoTheme {
        RegisterScreen(
            venueId = null,
            viewModel =
                RegisterViewModel(ValidateRegisterFormUseCase(ValidateEmailUseCase())),
        )
    }
}
