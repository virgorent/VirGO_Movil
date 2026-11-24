package mx.virgo.app.demo

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import mx.virgo.app.R
import mx.virgo.app.R.string as S

@Composable
internal fun OwnerPricing(notice: (String) -> Unit) {
    var base by ownerDemoInput(S.demo_owner_base_raw)
    var weekend by ownerDemoInput(S.demo_owner_weekend_raw)
    var high by ownerDemoInput(S.demo_owner_high_raw)
    val saved = stringResource(S.demo_owner_saved)
    val invalid = stringResource(S.demo_owner_invalid_price)
    DemoPage(stringResource(S.demo_owner_pricing), stringResource(S.demo_owner_venue)) {
        DemoNotice(stringResource(S.demo_owner_simulation))
        DemoField(stringResource(S.demo_owner_base), base, { base = it })
        DemoField(stringResource(S.demo_owner_weekend), weekend, { weekend = it })
        DemoField(stringResource(S.demo_owner_high), high, { high = it })
        DemoSection(stringResource(S.demo_owner_high_months))
        OwnerToggle(S.demo_owner_november)
        OwnerToggle(S.demo_owner_december)
        DemoSection(stringResource(S.demo_owner_price_preview))
        val amount = base.toIntOrNull() ?: 0
        val percentage = weekend.toIntOrNull() ?: 0
        DemoCard(stringResource(S.demo_owner_weekday), stringResource(S.demo_owner_mxn_hour, amount))
        DemoCard(
            stringResource(S.demo_owner_weekend_label),
            stringResource(S.demo_owner_mxn_hour, (amount * (1 + percentage / OWNER_PERCENT_SCALE.toDouble())).toInt()),
        )
        DemoCard(stringResource(S.demo_owner_furniture), stringResource(S.demo_owner_included))
        DemoAction(stringResource(S.demo_owner_save_rates)) {
            val valid =
                amount > 0 &&
                    weekend.toIntOrNull() in 0..OWNER_PERCENT_SCALE &&
                    high.toIntOrNull() in 0..OWNER_PERCENT_SCALE
            notice(if (valid) saved else invalid)
        }
    }
}

@Composable
internal fun OwnerSetups(notice: (String) -> Unit) {
    var banquet by ownerDemoInput(S.demo_owner_banquet_raw)
    var auditorium by ownerDemoInput(S.demo_owner_auditorium_raw)
    var cocktail by ownerDemoInput(S.demo_owner_cocktail_raw)
    val saved = stringResource(S.demo_owner_saved)
    val invalid = stringResource(S.demo_owner_invalid_capacity)
    DemoPage(stringResource(S.demo_owner_setups), stringResource(S.demo_owner_venue)) {
        DemoCard(stringResource(S.demo_owner_plan), stringResource(S.demo_owner_plan_detail))
        DemoField(stringResource(S.demo_owner_banquet), banquet, { banquet = it })
        DemoField(stringResource(S.demo_owner_auditorium), auditorium, { auditorium = it })
        DemoField(stringResource(S.demo_owner_cocktail), cocktail, { cocktail = it })
        DemoNotice(stringResource(S.demo_owner_setup_hint))
        DemoAction(stringResource(S.demo_owner_save)) {
            val valid =
                listOf(banquet, auditorium, cocktail).all {
                    it.toIntOrNull() in OWNER_MIN_CAPACITY..OWNER_MAX_CAPACITY
                }
            notice(if (valid) saved else invalid)
        }
    }
}

@Composable
internal fun OwnerRequests(go: (String) -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    DemoPage(stringResource(S.demo_owner_requests), stringResource(S.demo_owner_quotes)) {
        OwnerChoice(listOf(S.demo_owner_new, S.demo_owner_replied, S.demo_owner_all), tab) { tab = it }
        if (tab != 1) {
            DemoCard(
                stringResource(S.demo_owner_client),
                stringResource(S.demo_owner_request_detail),
                stringResource(S.demo_owner_new),
                { go("ownerRequest") },
            )
        }
        if (tab != 1) {
            DemoCard(
                stringResource(S.demo_owner_client_two),
                stringResource(S.demo_owner_request_two),
                stringResource(S.demo_owner_new),
                { go("ownerRequest") },
            )
        }
        if (tab != 0) {
            DemoCard(
                stringResource(S.demo_owner_client_three),
                stringResource(S.demo_owner_request_three),
                stringResource(S.demo_owner_replied),
                { go("ownerRequest") },
            )
        }
        DemoNotice(stringResource(S.demo_owner_request_fixed))
    }
}

@Composable
internal fun OwnerQuote(notice: (String) -> Unit) {
    val initial = stringResource(S.demo_owner_rental)
    val initialNote = stringResource(S.demo_owner_quote_note)
    var concept by rememberSaveable { mutableStateOf(initial) }
    var quantity by ownerDemoInput(S.demo_owner_quantity_raw)
    var price by ownerDemoInput(S.demo_owner_base_raw)
    var note by rememberSaveable { mutableStateOf(initialNote) }
    val sent = stringResource(S.demo_owner_quote_sent)
    val invalid = stringResource(S.demo_owner_invalid)
    DemoPage(stringResource(S.demo_owner_client), stringResource(S.demo_owner_reply_quote)) {
        DemoNotice(stringResource(S.demo_owner_simulation))
        DemoCard(stringResource(S.demo_owner_wedding), stringResource(S.demo_owner_request_detail))
        DemoSection(stringResource(S.demo_owner_your_quote))
        DemoField(stringResource(S.demo_owner_concept), concept, { concept = it })
        DemoField(stringResource(S.demo_owner_quantity), quantity, { quantity = it })
        DemoField(stringResource(S.demo_owner_unit_price), price, { price = it })
        val total = (quantity.toLongOrNull() ?: 0) * (price.toLongOrNull() ?: 0)
        DemoCard(stringResource(S.demo_owner_total), stringResource(S.demo_owner_mxn_total, total))
        DemoField(stringResource(S.demo_owner_note), note, { note = it })
        DemoNotice(stringResource(S.demo_owner_validity))
        DemoAction(stringResource(S.demo_owner_send_demo)) {
            val valid =
                concept.isNotBlank() &&
                    (quantity.toIntOrNull() ?: 0) > 0 &&
                    (price.toIntOrNull() ?: -1) >= 0
            notice(if (valid) sent else invalid)
        }
    }
}

@Composable
internal fun OwnerPayments() {
    var tab by rememberSaveable { mutableStateOf(0) }
    DemoPage(stringResource(S.demo_owner_payments), stringResource(S.demo_owner_simulation)) {
        DemoStats(
            listOf(
                stringResource(S.demo_owner_received) to stringResource(S.demo_owner_received_value),
                stringResource(S.demo_owner_pending) to stringResource(S.demo_owner_pending_value),
            ),
        )
        OwnerChoice(listOf(S.demo_owner_all, S.demo_owner_completed, S.demo_owner_pending, S.demo_owner_failed), tab) {
            tab =
                it
        }
        if (tab in 0..1) {
            DemoCard(
                stringResource(S.demo_owner_client),
                stringResource(S.demo_owner_payment_detail),
                stringResource(S.demo_owner_completed),
            )
        }
        if (tab == 0 || tab == 2) {
            DemoCard(
                stringResource(S.demo_owner_client_two),
                stringResource(S.demo_owner_payment_pending),
                stringResource(S.demo_owner_pending),
            )
        }
        if (tab == OWNER_FAILED_PAYMENT_TAB) DemoNotice(stringResource(S.demo_owner_no_failed))
    }
}

@Composable
internal fun OwnerStats() {
    var period by rememberSaveable { mutableStateOf(1) }
    val labels = stringArrayResource(R.array.demo_owner_stat_months)
    val periodMonths = if (period == 0) OWNER_SHORT_PERIOD_MONTHS else ownerDemoMonthlyVisits.size
    val values = ownerDemoMonthlyVisits.takeLast(periodMonths)
    DemoPage(stringResource(S.demo_owner_stats), stringResource(S.demo_owner_synthetic)) {
        OwnerChoice(listOf(S.demo_owner_three_months, S.demo_owner_six_months), period) { period = it }
        DemoStats(
            listOf(
                stringResource(S.demo_owner_visits) to stringResource(S.demo_owner_number, values.sum()),
                stringResource(S.demo_owner_requests) to stringResource(S.demo_owner_four),
                stringResource(S.demo_owner_bookings) to stringResource(S.demo_owner_two),
            ),
        )
        DemoSection(stringResource(S.demo_owner_interest))
        values.forEachIndexed { index, value ->
            Text(stringResource(S.demo_owner_chart_label, labels[labels.size - values.size + index], value))
            LinearProgressIndicator(progress = { value / ownerDemoMonthlyVisits.max().toFloat() })
        }
        DemoNotice(stringResource(S.demo_owner_conversion))
    }
}

@Composable
internal fun OwnerReviews(notice: (String) -> Unit) {
    var reply by rememberSaveable { mutableStateOf("") }
    var published by rememberSaveable { mutableStateOf(false) }
    val saved = stringResource(S.demo_owner_reply_saved)
    val invalid = stringResource(S.demo_owner_reply_invalid)
    DemoPage(stringResource(S.demo_owner_reviews), stringResource(S.demo_owner_review_count)) {
        DemoCard(
            stringResource(S.demo_owner_client),
            stringResource(S.demo_owner_review),
            stringResource(S.demo_owner_five_stars),
        )
        if (published) DemoCard(stringResource(S.demo_owner_your_reply), reply)
        DemoField(stringResource(S.demo_owner_your_reply), reply, {
            reply = it
            published = false
        })
        DemoAction(stringResource(S.demo_owner_reply_demo)) {
            if (reply.trim().length >= OWNER_MIN_REPLY_LENGTH) {
                published = true
                notice(saved)
            } else {
                notice(invalid)
            }
        }
        DemoCard(
            stringResource(S.demo_owner_client_three),
            stringResource(S.demo_owner_review_two),
            stringResource(S.demo_owner_four_stars),
        )
    }
}

@Composable
internal fun OwnerAllies() {
    DemoPage(stringResource(S.demo_owner_allies), stringResource(S.demo_owner_venue)) {
        DemoNotice(stringResource(S.demo_owner_allies_hint))
        OwnerToggle(S.demo_owner_ally_catering)
        OwnerToggle(S.demo_owner_ally_music)
        OwnerToggle(S.demo_owner_ally_flowers)
    }
}
