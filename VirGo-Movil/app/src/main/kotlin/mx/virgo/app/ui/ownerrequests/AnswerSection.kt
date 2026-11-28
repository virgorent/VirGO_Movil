package mx.virgo.app.ui.ownerrequests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.app.ui.quotes.QuoteDetailBody
import mx.virgo.core.designsystem.components.ErrorBanner
import mx.virgo.core.designsystem.components.VirGoButton
import mx.virgo.core.designsystem.components.VirGoCard
import mx.virgo.core.designsystem.components.VirGoRow
import mx.virgo.core.designsystem.components.VirGoTextField
import mx.virgo.core.designsystem.theme.VirGoTone
import mx.virgo.core.domain.AnswerField
import mx.virgo.core.domain.AnswerProblem
import mx.virgo.core.domain.Quote
import mx.virgo.core.domain.QuoteAnswerValidator
import mx.virgo.core.domain.QuoteStatus

/** P8: la solicitud y, si sigue «Enviada», el formulario de respuesta (024 RF-03–RF-07). */
@Composable
internal fun AnswerSection(
    quote: Quote,
    detail: RequestDetailState,
    actions: OwnerRequestsActions,
    answerHelper: (@Composable (AnswerForm, (AnswerForm) -> Unit) -> Unit)? = null,
) {
    QuoteDetailBody(quote, onBook = null, ownerView = true)
    if (detail.justAnswered) {
        VirGoCard {
            VirGoRow(
                title = stringResource(R.string.answer_sent),
                icon = Icons.Outlined.CheckCircle,
                tone = VirGoTone.GREEN,
                standalone = false,
            )
        }
    }
    detail.error?.let { ErrorBanner(message = stringResource(answerErrorText(it))) }
    if (quote.status != QuoteStatus.SENT) return
    Text(
        stringResource(R.string.answer_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() },
    )
    val form = detail.form
    answerHelper?.invoke(form, actions.onForm)
    AnswerInput(
        label = stringResource(R.string.answer_total),
        value = form.total,
        errorText = errorFor(AnswerField.TOTAL, detail.fieldErrors),
        hint = stringResource(R.string.answer_total_hint),
        keyboardType = KeyboardType.Decimal,
    ) { actions.onForm(form.copy(total = it)) }
    AnswerInput(
        label = stringResource(R.string.answer_deposit),
        value = form.deposit,
        errorText = errorFor(AnswerField.DEPOSIT, detail.fieldErrors),
        keyboardType = KeyboardType.Decimal,
    ) { actions.onForm(form.copy(deposit = it)) }
    AnswerInput(
        label = stringResource(R.string.answer_message),
        value = form.message,
        errorText = errorFor(AnswerField.MESSAGE, detail.fieldErrors),
        hint =
            stringResource(R.string.answer_message_count, form.message.length, QuoteAnswerValidator.MAX_MESSAGE),
        singleLine = false,
    ) { actions.onForm(form.copy(message = it)) }
    VirGoButton(
        onClick = actions.onSend,
        enabled = !detail.sending,
        text = stringResource(if (detail.sending) R.string.answer_sending else R.string.answer_send),
    )
}

@Composable
private fun AnswerInput(
    label: String,
    value: String,
    errorText: String?,
    hint: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        VirGoTextField(
            value = value,
            onValueChange = onValueChange,
            isError = errorText != null,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier =
                Modifier.semantics {
                    contentDescription = label
                    if (errorText != null) error(errorText)
                },
        )
        val note = errorText ?: hint
        if (note != null) {
            Text(
                note,
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (errorText !=
                        null
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}

@Composable
private fun errorFor(
    field: AnswerField,
    errors: Map<AnswerField, AnswerProblem>,
): String? =
    when (errors[field]) {
        null -> null
        AnswerProblem.REQUIRED -> stringResource(R.string.answer_error_total_required)
        AnswerProblem.ABOVE_TOTAL -> stringResource(R.string.answer_error_deposit_above)
        AnswerProblem.TOO_LONG -> stringResource(R.string.answer_error_message_long, QuoteAnswerValidator.MAX_MESSAGE)
        AnswerProblem.INVALID ->
            stringResource(
                if (field ==
                    AnswerField.TOTAL
                ) {
                    R.string.answer_error_total_invalid
                } else {
                    R.string.answer_error_deposit_invalid
                },
            )
    }

private fun answerErrorText(error: AnswerError): Int =
    when (error) {
        AnswerError.NETWORK -> R.string.answer_error_network
        AnswerError.NOT_FOUND -> R.string.answer_error_not_found
        AnswerError.FORBIDDEN -> R.string.answer_error_forbidden
        AnswerError.ALREADY_ANSWERED -> R.string.answer_error_already
        AnswerError.REJECTED -> R.string.answer_error_rejected
        AnswerError.SERVER -> R.string.answer_error_server
    }
