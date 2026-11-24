package mx.virgo.app.demo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoSheetActions(
    role: DemoRole,
    route: String,
    onNotice: (String) -> Unit,
) {
    var selected by rememberSaveable(role, route) { mutableStateOf<String?>(null) }
    val definitions = demoSheetsFor(role, route)
    val notice = stringResource(R.string.demo_sheet_scope)
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(DemoMetrics.compactSpacing),
    ) {
        definitions.forEach { sheet ->
            InputChip(
                selected = selected == sheet.id,
                onClick = { selected = sheet.id },
                label = { Text(stringResource(sheet.title)) },
                modifier = Modifier.heightIn(min = DemoMetrics.touchTarget),
            )
        }
    }
    definitions.firstOrNull { it.id == selected }?.let { sheet ->
        ModalBottomSheet(onDismissRequest = { selected = null }, sheetState = modalState) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(DemoMetrics.pagePadding),
                verticalArrangement = Arrangement.spacedBy(DemoMetrics.spacing),
            ) {
                Text(stringResource(sheet.title), style = MaterialTheme.typography.headlineSmall)
                DemoNotice(notice)
                DemoSheetContent(sheet.id) { onNotice(notice) }
                DemoAction(stringResource(R.string.demo_sheet_close)) { selected = null }
            }
        }
    }
}

@Composable
private fun DemoSheetContent(
    id: String,
    onConfirm: () -> Unit,
) {
    when (id) {
        "H01" -> DemoSheetsSearch(onConfirm)
        "H02" -> DemoSheetsVenue(onConfirm)
        "H03" -> DemoSheetsBudget(onConfirm)
        "H04" -> DemoSheetsEvent(onConfirm)
        "H05" -> DemoSheetsTeam(onConfirm)
        "H06" -> DemoSheetsProfile(onConfirm)
        "H07" -> DemoSheetsPreferences(onConfirm)
        "H08" -> DemoSheetsMethods(onConfirm)
        else -> DemoSheetsPlanningContent(id, onConfirm)
    }
}

@Composable
private fun DemoSheetsPlanningContent(
    id: String,
    onConfirm: () -> Unit,
) {
    when (id) {
        "H09" -> DemoSheetsPayment(onConfirm)
        "H10" -> DemoSheetsGuest(onConfirm)
        "H11" -> DemoSheetsTask(onConfirm)
        "H12" -> DemoSheetsReminder(onConfirm)
        "H13" -> DemoSheetsCollaborator(onConfirm)
        "H14" -> DemoSheetsBlock(onConfirm)
        "H15" -> DemoSheetsAgenda(onConfirm)
    }
}
