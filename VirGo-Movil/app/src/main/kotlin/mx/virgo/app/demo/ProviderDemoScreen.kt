package mx.virgo.app.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mx.virgo.app.R

/** Presentation-only provider workspace; unavailable operations never invoke a backend. */
@Composable
fun ProviderDemoScreen(
    screen: String,
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    var quoted by rememberSaveable { mutableStateOf(false) }
    var scheduled by rememberSaveable { mutableStateOf(false) }
    when (screen) {
        "providerHome" -> ProviderHome(quoted, scheduled, onNavigate)
        "providerProfile" -> ProviderProfile(onNavigate, onNotice)
        "providerGallery" -> ProviderGallery(onNotice)
        "providerZones" -> ProviderZones(onNotice)
        "providerRequests" -> ProviderRequests(quoted, onNavigate)
        "providerRequest" ->
            ProviderRequest(
                quoted = quoted,
                scheduled = scheduled,
                onQuote = { quoted = true },
                onSchedule = {
                    scheduled = true
                    onNavigate("providerAgenda")
                },
                onNotice = onNotice,
            )
        "providerAgenda" -> ProviderAgenda(scheduled, onNavigate, onNotice)
        "providerReviews" -> ProviderReviews()
        "providerStats" -> ProviderStats()
        else -> error(screen)
    }
}

@Composable
private fun ProviderHome(
    quoted: Boolean,
    scheduled: Boolean,
    onNavigate: (String) -> Unit,
) {
    DemoPage(stringResource(R.string.demo_provider_home), stringResource(R.string.demo_provider_business)) {
        DemoNotice(stringResource(R.string.demo_provider_disclaimer))
        DemoStats(
            listOf(
                stringResource(R.string.demo_provider_new) to
                    stringResource(if (quoted) R.string.demo_provider_zero else R.string.demo_provider_one),
                stringResource(R.string.demo_provider_upcoming) to
                    stringResource(if (scheduled) R.string.demo_provider_two else R.string.demo_provider_one),
                stringResource(R.string.demo_provider_quotes) to
                    stringResource(if (quoted) R.string.demo_provider_one else R.string.demo_provider_zero),
            ),
        )
        DemoSection(stringResource(R.string.demo_provider_requests))
        DemoCard(
            stringResource(R.string.demo_provider_client),
            stringResource(R.string.demo_provider_event_summary),
            stringResource(if (quoted) R.string.demo_provider_replied else R.string.demo_provider_new),
            onClick = { onNavigate("providerRequest") },
        )
        DemoNavigationLink(stringResource(R.string.demo_provider_all_requests)) { onNavigate("providerRequests") }
        DemoSection(stringResource(R.string.demo_provider_agenda))
        DemoCard(
            stringResource(R.string.demo_provider_existing_event),
            stringResource(R.string.demo_provider_existing_detail),
            onClick = { onNavigate("providerAgenda") },
        )
        DemoSection(stringResource(R.string.demo_provider_manage))
        listOf(
            R.string.demo_provider_profile to "providerProfile",
            R.string.demo_provider_gallery to "providerGallery",
            R.string.demo_provider_zones to "providerZones",
            R.string.demo_provider_reviews to "providerReviews",
            R.string.demo_provider_stats to "providerStats",
        ).forEach { (label, route) -> DemoNavigationLink(stringResource(label)) { onNavigate(route) } }
    }
}

@Composable
private fun ProviderProfile(
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    val initialName = stringResource(R.string.demo_provider_business)
    val initialDescription = stringResource(R.string.demo_provider_description_sample)
    val initialPackage = stringResource(R.string.demo_provider_package_sample)
    val saved = stringResource(R.string.demo_provider_saved)
    val invalid = stringResource(R.string.demo_provider_profile_invalid)
    var name by rememberSaveable { mutableStateOf(initialName) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }
    var packageName by rememberSaveable { mutableStateOf(initialPackage) }
    val initialPrice = stringResource(R.string.demo_provider_price_raw)
    var price by rememberSaveable { mutableStateOf(initialPrice) }
    var perPerson by rememberSaveable { mutableStateOf(true) }
    DemoPage(stringResource(R.string.demo_provider_profile), stringResource(R.string.demo_provider_local_draft)) {
        DemoNotice(stringResource(R.string.demo_provider_disclaimer))
        DemoField(stringResource(R.string.demo_provider_name), name, { name = it })
        DemoCard(stringResource(R.string.demo_provider_category), stringResource(R.string.demo_provider_catering))
        DemoField(stringResource(R.string.demo_provider_description), description, { description = it })
        DemoSection(stringResource(R.string.demo_provider_services))
        DemoField(stringResource(R.string.demo_provider_package), packageName, { packageName = it })
        DemoField(stringResource(R.string.demo_provider_price), price, { price = it })
        ProviderChoice(stringResource(R.string.demo_provider_per_person), perPerson) { perPerson = true }
        ProviderChoice(stringResource(R.string.demo_provider_per_event), !perPerson) { perPerson = false }
        DemoAction(stringResource(R.string.demo_provider_save_draft)) {
            val valid =
                name.trim().length >= PROVIDER_NAME_MINIMUM &&
                    description.trim().length >= PROVIDER_DESCRIPTION_MINIMUM &&
                    packageName.isNotBlank() &&
                    (price.toIntOrNull() ?: 0) > 0
            onNotice(if (valid) saved else invalid)
        }
        DemoNavigationLink(stringResource(R.string.demo_provider_gallery)) { onNavigate("providerGallery") }
        DemoNavigationLink(stringResource(R.string.demo_provider_zones)) { onNavigate("providerZones") }
    }
}

@Composable
private fun ProviderGallery(onNotice: (String) -> Unit) {
    var showSample by rememberSaveable { mutableStateOf(true) }
    val changed = stringResource(R.string.demo_provider_gallery_changed)
    DemoPage(stringResource(R.string.demo_provider_gallery), stringResource(R.string.demo_provider_business)) {
        DemoNotice(stringResource(R.string.demo_provider_gallery_notice))
        if (showSample) {
            DemoServiceArtwork()
            DemoCard(
                stringResource(R.string.demo_provider_sample_one),
                stringResource(R.string.demo_provider_sample_description),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(PROVIDER_ACCENT_HEIGHT.dp)
                .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.shapes.large),
        )
        DemoCard(
            stringResource(R.string.demo_provider_sample_two),
            stringResource(R.string.demo_provider_sample_description),
        )
        DemoAction(
            stringResource(
                if (showSample) R.string.demo_provider_remove_sample else R.string.demo_provider_restore_sample,
            ),
        ) {
            showSample = !showSample
            onNotice(changed)
        }
        DemoNotice(stringResource(R.string.demo_provider_no_upload))
    }
}

@Composable
private fun ProviderZones(onNotice: (String) -> Unit) {
    var center by rememberSaveable { mutableStateOf(true) }
    var north by rememberSaveable { mutableStateOf(true) }
    var south by rememberSaveable { mutableStateOf(false) }
    val required = stringResource(R.string.demo_provider_zone_required)
    val saved = stringResource(R.string.demo_provider_saved)
    DemoPage(stringResource(R.string.demo_provider_zones), stringResource(R.string.demo_provider_zone_subtitle)) {
        DemoNotice(stringResource(R.string.demo_provider_zone_notice))
        ProviderChoice(stringResource(R.string.demo_provider_zone_center), center) {
            if (center && !north && !south) onNotice(required) else center = !center
        }
        ProviderChoice(stringResource(R.string.demo_provider_zone_north), north) {
            if (north && !center && !south) onNotice(required) else north = !north
        }
        ProviderChoice(stringResource(R.string.demo_provider_zone_south), south) {
            if (south && !center && !north) onNotice(required) else south = !south
        }
        DemoCard(
            stringResource(R.string.demo_provider_zone_conditions),
            stringResource(R.string.demo_provider_zone_conditions_detail),
        )
        DemoAction(stringResource(R.string.demo_provider_save_zones)) { onNotice(saved) }
    }
}

@Composable
private fun ProviderRequests(
    quoted: Boolean,
    onNavigate: (String) -> Unit,
) {
    var filter by rememberSaveable { mutableStateOf(0) }
    DemoPage(stringResource(R.string.demo_provider_requests), stringResource(R.string.demo_provider_synthetic)) {
        listOf(
            R.string.demo_provider_new,
            R.string.demo_provider_replied,
            R.string.demo_provider_all,
        ).forEachIndexed { index, label ->
            ProviderChoice(stringResource(label), filter == index) { filter = index }
        }
        val matchingStatus = if (quoted) 1 else 0
        if (filter == 2 || filter == matchingStatus) {
            DemoCard(
                stringResource(R.string.demo_provider_client),
                stringResource(R.string.demo_provider_event_summary),
                stringResource(if (quoted) R.string.demo_provider_replied else R.string.demo_provider_new),
                onClick = { onNavigate("providerRequest") },
            )
        } else {
            DemoNotice(stringResource(R.string.demo_provider_empty_requests))
        }
        DemoAction(stringResource(R.string.demo_provider_agenda)) { onNavigate("providerAgenda") }
    }
}

@Composable
private fun ProviderRequest(
    quoted: Boolean,
    scheduled: Boolean,
    onQuote: () -> Unit,
    onSchedule: () -> Unit,
    onNotice: (String) -> Unit,
) {
    val initialMessage = stringResource(R.string.demo_provider_quote_sample)
    val sent = stringResource(R.string.demo_provider_quote_simulated)
    val invalid = stringResource(R.string.demo_provider_quote_invalid)
    val initialAmount = stringResource(R.string.demo_provider_total_raw)
    var amount by rememberSaveable { mutableStateOf(initialAmount) }
    var message by rememberSaveable { mutableStateOf(initialMessage) }
    DemoPage(stringResource(R.string.demo_provider_request_detail), stringResource(R.string.demo_provider_client)) {
        DemoNotice(stringResource(R.string.demo_provider_disclaimer))
        DemoCard(
            stringResource(R.string.demo_provider_event_name),
            stringResource(R.string.demo_provider_event_detail),
            stringResource(if (quoted) R.string.demo_provider_replied else R.string.demo_provider_new),
        )
        DemoSection(stringResource(R.string.demo_provider_quote))
        DemoField(stringResource(R.string.demo_provider_total), amount, { amount = it })
        DemoField(stringResource(R.string.demo_provider_message), message, { message = it })
        DemoAction(stringResource(R.string.demo_provider_simulate_quote)) {
            if ((amount.toIntOrNull() ?: 0) > 0 && message.isNotBlank()) {
                onQuote()
                onNotice(sent)
            } else {
                onNotice(invalid)
            }
        }
        if (quoted) {
            DemoNotice(stringResource(R.string.demo_provider_schedule_notice))
            DemoAction(
                stringResource(
                    if (scheduled) R.string.demo_provider_view_agenda else R.string.demo_provider_simulate_schedule,
                ),
                onSchedule,
            )
        }
    }
}

@Composable
private fun ProviderAgenda(
    scheduled: Boolean,
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    val initialPlace = stringResource(R.string.demo_provider_place)
    val saved = stringResource(R.string.demo_provider_saved)
    var place by rememberSaveable { mutableStateOf(initialPlace) }
    var nextWeek by rememberSaveable { mutableStateOf(false) }
    DemoPage(stringResource(R.string.demo_provider_agenda), stringResource(R.string.demo_provider_synthetic)) {
        WorkspaceSummary("providerAgenda")
        ProviderChoice(stringResource(R.string.demo_provider_week_one), !nextWeek) { nextWeek = false }
        ProviderChoice(stringResource(R.string.demo_provider_week_two), nextWeek) { nextWeek = true }
        if (nextWeek) {
            DemoNotice(stringResource(R.string.demo_provider_free_week))
        } else {
            DemoCard(
                stringResource(R.string.demo_provider_existing_event),
                stringResource(R.string.demo_provider_existing_detail),
            )
            if (scheduled) {
                DemoCard(
                    stringResource(R.string.demo_provider_event_name),
                    stringResource(R.string.demo_provider_event_detail),
                    stringResource(R.string.demo_provider_scheduled),
                )
                DemoField(stringResource(R.string.demo_provider_place_label), place, { place = it })
                DemoAction(stringResource(R.string.demo_provider_save_agenda)) { onNotice(saved) }
            } else {
                DemoNotice(stringResource(R.string.demo_provider_agenda_hint))
            }
        }
        DemoAction(stringResource(R.string.demo_provider_requests)) { onNavigate("providerRequests") }
    }
}

@Composable
private fun ProviderReviews() {
    DemoPage(stringResource(R.string.demo_provider_reviews), stringResource(R.string.demo_provider_synthetic)) {
        DemoStats(
            listOf(
                stringResource(R.string.demo_provider_average) to stringResource(R.string.demo_provider_average_value),
                stringResource(R.string.demo_provider_reviews) to stringResource(R.string.demo_provider_two),
            ),
        )
        DemoSection(stringResource(R.string.demo_provider_rating_distribution))
        ProviderChart.Bar(stringResource(R.string.demo_provider_five_stars), PROVIDER_HALF)
        ProviderChart.Bar(stringResource(R.string.demo_provider_four_stars), PROVIDER_HALF)
        DemoCard(
            stringResource(R.string.demo_provider_review_one),
            stringResource(R.string.demo_provider_review_one_body),
            stringResource(R.string.demo_provider_five_stars),
        )
        DemoCard(
            stringResource(R.string.demo_provider_review_two),
            stringResource(R.string.demo_provider_review_two_body),
            stringResource(R.string.demo_provider_four_stars),
        )
        DemoNotice(stringResource(R.string.demo_provider_reviews_notice))
    }
}

@Composable
private fun ProviderStats() {
    var sixMonths by rememberSaveable { mutableStateOf(false) }
    DemoPage(stringResource(R.string.demo_provider_stats), stringResource(R.string.demo_provider_synthetic)) {
        ProviderChoice(stringResource(R.string.demo_provider_three_months), !sixMonths) { sixMonths = false }
        ProviderChoice(stringResource(R.string.demo_provider_six_months), sixMonths) { sixMonths = true }
        DemoStats(
            listOf(
                stringResource(R.string.demo_provider_visits) to
                    stringResource(
                        if (sixMonths) R.string.demo_provider_visits_six else R.string.demo_provider_visits_three,
                    ),
                stringResource(R.string.demo_provider_requests) to
                    stringResource(
                        if (sixMonths) R.string.demo_provider_requests_six else R.string.demo_provider_requests_three,
                    ),
                stringResource(R.string.demo_provider_accepted) to
                    stringResource(
                        if (sixMonths) R.string.demo_provider_accepted_six else R.string.demo_provider_accepted_three,
                    ),
            ),
        )
        DemoSection(stringResource(R.string.demo_provider_monthly))
        if (sixMonths) {
            ProviderChart.Bar(stringResource(R.string.demo_provider_june), PROVIDER_JUNE_FRACTION)
            ProviderChart.Bar(stringResource(R.string.demo_provider_july), PROVIDER_JULY_FRACTION)
            ProviderChart.Bar(stringResource(R.string.demo_provider_august), PROVIDER_HALF)
        }
        ProviderChart.Bar(stringResource(R.string.demo_provider_september), PROVIDER_SEPTEMBER_FRACTION)
        ProviderChart.Bar(stringResource(R.string.demo_provider_october), PROVIDER_OCTOBER_FRACTION)
        ProviderChart.Bar(stringResource(R.string.demo_provider_november), PROVIDER_NOVEMBER_FRACTION)
        DemoNotice(stringResource(R.string.demo_provider_stats_notice))
    }
}

private const val PROVIDER_NAME_MINIMUM = 3
private const val PROVIDER_DESCRIPTION_MINIMUM = 20
private const val PROVIDER_ACCENT_HEIGHT = 96
private const val PROVIDER_HALF = 0.5f
private const val PROVIDER_JUNE_FRACTION = 0.25f
private const val PROVIDER_JULY_FRACTION = 0.375f
private const val PROVIDER_SEPTEMBER_FRACTION = 0.625f
private const val PROVIDER_OCTOBER_FRACTION = 0.75f
private const val PROVIDER_NOVEMBER_FRACTION = 0.875f
