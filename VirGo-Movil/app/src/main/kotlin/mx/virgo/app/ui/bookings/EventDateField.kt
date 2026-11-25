package mx.virgo.app.ui.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.virgo.app.R
import mx.virgo.core.designsystem.components.VirGoOutlinedButton
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Fecha del evento con calendario que no deja elegir días pasados (009 RF-02, 008 RF-02). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventDateField(
    label: String,
    date: LocalDate?,
    errorText: String?,
    onDate: (LocalDate) -> Unit,
) {
    var picking by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        VirGoOutlinedButton(
            onClick = { picking = true },
            modifier =
                Modifier.fillMaxWidth().semantics {
                    contentDescription = label
                    if (errorText != null) error(errorText)
                },
        ) {
            Text(
                date?.let { stringResource(R.string.booking_date_value, formatDate(it)) }
                    ?: stringResource(R.string.booking_date_pick),
            )
        }
        errorText?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
    if (picking) {
        val todayUtc =
            LocalDate
                .now()
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = date?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli(),
                selectableDates =
                    object : SelectableDates {
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayUtc
                    },
            )
        DatePickerDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    picking = false
                }) { Text(stringResource(R.string.booking_date_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { picking = false }) { Text(stringResource(R.string.booking_date_cancel)) }
            },
        ) { DatePicker(state = pickerState) }
    }
}
