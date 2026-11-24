package mx.virgo.app.demo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.virgo.app.R.string as S

@Composable
fun OwnerDemoScreen(
    screen: String,
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    when (screen) {
        "ownerHome" -> OwnerHome(onNavigate)
        "ownerVenues" -> OwnerVenues(onNavigate)
        "venueEditor" -> OwnerEditor(onNotice)
        "ownerCalendar" -> OwnerCalendar(onNotice)
        "ownerPricing" -> OwnerPricing(onNotice)
        "ownerSetups" -> OwnerSetups(onNotice)
        "ownerRequests" -> OwnerRequests(onNavigate)
        "ownerRequest" -> OwnerQuote(onNotice)
        "ownerPayments" -> OwnerPayments()
        "ownerStats" -> OwnerStats()
        "ownerReviews" -> OwnerReviews(onNotice)
        "ownerAllies" -> OwnerAllies()
        else -> error(screen)
    }
}

@Composable
private fun OwnerHome(go: (String) -> Unit) {
    DemoPage(stringResource(S.demo_owner_hello), stringResource(S.demo_owner_panel)) {
        DemoNotice(stringResource(S.demo_owner_simulation))
        DemoStats(
            listOf(
                stringResource(S.demo_owner_visits) to stringResource(S.demo_owner_visits_value),
                stringResource(S.demo_owner_new_requests) to stringResource(S.demo_owner_two),
                stringResource(S.demo_owner_bookings) to stringResource(S.demo_owner_two),
                stringResource(S.demo_owner_received) to stringResource(S.demo_owner_received_value),
            ),
        )
        DemoSection(stringResource(S.demo_owner_upcoming))
        DemoCard(
            stringResource(S.demo_owner_client),
            stringResource(S.demo_owner_booking_detail),
            stringResource(S.demo_owner_confirmed),
            { go("ownerCalendar") },
        )
        DemoCard(
            stringResource(S.demo_owner_client_two),
            stringResource(S.demo_owner_booking_two),
            stringResource(S.demo_owner_confirmed),
            { go("ownerCalendar") },
        )
        DemoSection(stringResource(S.demo_owner_recent))
        DemoCard(
            stringResource(S.demo_owner_request_notice),
            stringResource(S.demo_owner_request_detail),
            onClick = { go("ownerRequests") },
        )
        listOf(
            S.demo_owner_venues to "ownerVenues",
            S.demo_owner_calendar to "ownerCalendar",
            S.demo_owner_requests to "ownerRequests",
            S.demo_owner_payments to "ownerPayments",
            S.demo_owner_stats to "ownerStats",
            S.demo_owner_reviews to "ownerReviews",
        ).forEach { (label, route) -> DemoNavigationLink(stringResource(label)) { go(route) } }
    }
}

@Composable
private fun OwnerVenues(go: (String) -> Unit) {
    DemoPage(stringResource(S.demo_owner_venues), stringResource(S.demo_owner_catalog)) {
        DemoVenueArtwork()
        DemoCard(
            stringResource(S.demo_owner_venue),
            stringResource(S.demo_owner_venue_detail),
            stringResource(S.demo_owner_published),
            { go("venueEditor") },
        )
        DemoCard(
            stringResource(S.demo_owner_venue_two),
            stringResource(S.demo_owner_venue_two_detail),
            stringResource(S.demo_owner_draft),
            { go("venueEditor") },
        )
        DemoNotice(stringResource(S.demo_owner_no_photos))
        listOf(
            S.demo_owner_register to "venueEditor",
            S.demo_owner_pricing to "ownerPricing",
            S.demo_owner_setups to "ownerSetups",
            S.demo_owner_allies to "ownerAllies",
        ).forEach { (label, route) -> DemoNavigationLink(stringResource(label)) { go(route) } }
    }
}

@Composable
private fun OwnerEditor(notice: (String) -> Unit) {
    var step by rememberSaveable { mutableStateOf(0) }
    val initialName = stringResource(S.demo_owner_venue)
    val initialDescription = stringResource(S.demo_owner_description_value)
    val initialAddress = stringResource(S.demo_owner_address_value)
    var name by rememberSaveable { mutableStateOf(initialName) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }
    var address by rememberSaveable { mutableStateOf(initialAddress) }
    var capacity by ownerDemoInput(S.demo_owner_banquet_raw)
    var price by ownerDemoInput(S.demo_owner_base_raw)
    val saved = stringResource(S.demo_owner_saved)
    val invalid = stringResource(S.demo_owner_invalid)
    DemoPage(stringResource(S.demo_owner_edit), stringResource(S.demo_owner_step, step + 1)) {
        LinearProgressIndicator(progress = { (step + 1) / OWNER_EDITOR_STEP_COUNT.toFloat() })
        when (step) {
            0 -> {
                DemoField(stringResource(S.demo_owner_name), name, { name = it })
                DemoField(stringResource(S.demo_owner_description), description, { description = it })
                DemoField(stringResource(S.demo_owner_address), address, { address = it })
                OwnerChoice(listOf(S.demo_owner_zone, S.demo_owner_zone_two))
                DemoNotice(stringResource(S.demo_owner_no_photos))
            }
            1 -> {
                DemoField(stringResource(S.demo_owner_capacity), capacity, { capacity = it })
                DemoField(stringResource(S.demo_owner_base), price, { price = it })
                OwnerChoice(listOf(S.demo_owner_hour, S.demo_owner_day))
                OwnerChoice(listOf(S.demo_owner_wedding, S.demo_owner_corporate))
                OwnerToggle(S.demo_owner_parking)
                OwnerToggle(S.demo_owner_furniture)
            }
            else -> {
                DemoCard(name, description)
                OwnerEditorSummary(address, capacity, price)
                DemoNotice(stringResource(S.demo_owner_simulation))
            }
        }
        if (step > 0) DemoAction(stringResource(S.demo_owner_previous)) { step-- }
        if (step < 2) {
            DemoAction(stringResource(S.demo_owner_next)) {
                val valid = ownerEditorValid(name, description, address, capacity, price)
                if (valid) step++ else notice(invalid)
            }
        } else {
            DemoAction(stringResource(S.demo_owner_save_draft)) { notice(saved) }
            DemoAction(stringResource(S.demo_owner_publish_demo)) { notice(saved) }
        }
    }
}

@Composable
private fun OwnerEditorSummary(
    address: String,
    capacity: String,
    price: String,
) {
    DemoCard(
        stringResource(S.demo_owner_summary),
        stringResource(S.demo_owner_summary_value, address, capacity, price),
    )
}

private fun ownerEditorValid(
    name: String,
    description: String,
    address: String,
    capacity: String,
    price: String,
): Boolean =
    name.trim().length >= OWNER_MIN_NAME_LENGTH &&
        description.trim().length >= OWNER_MIN_DESCRIPTION_LENGTH &&
        address.trim().length >= OWNER_MIN_ADDRESS_LENGTH &&
        (capacity.toIntOrNull() ?: 0) >= OWNER_MIN_CAPACITY &&
        (price.toIntOrNull() ?: 0) > 0

@Composable
internal fun OwnerChoice(
    options: List<Int>,
    selected: Int = -1,
    change: ((Int) -> Unit)? = null,
) {
    var local by rememberSaveable { mutableStateOf(0) }
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(OWNER_CHIP_GAP.dp),
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = (if (selected < 0) local else selected) == index,
                onClick = {
                    local = index
                    change?.invoke(index)
                },
                label = { Text(stringResource(label)) },
                modifier = Modifier.heightIn(min = OWNER_TOUCH_TARGET.dp),
            )
        }
    }
}

@Composable
internal fun OwnerToggle(label: Int) {
    var enabled by rememberSaveable { mutableStateOf(false) }
    FilterChip(
        selected = enabled,
        onClick = { enabled = !enabled },
        label = { Text(stringResource(label), style = MaterialTheme.typography.bodyLarge) },
        modifier = Modifier.heightIn(min = OWNER_TOUCH_TARGET.dp),
    )
}
