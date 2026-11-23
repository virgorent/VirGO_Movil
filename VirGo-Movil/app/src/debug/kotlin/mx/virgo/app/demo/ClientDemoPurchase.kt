package mx.virgo.app.demo

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import mx.virgo.app.R

@Composable
internal fun ClientDemoPurchase(
    screen: String,
    paid: Boolean,
    onPaid: () -> Unit,
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    when (screen) {
        "quoteNew" -> ClientDemoQuoteNew(onNavigate, onNotice)
        "quotes" -> ClientDemoQuotes(onNavigate)
        "quote" -> ClientDemoQuote(onNavigate)
        "booking" -> ClientDemoBooking(onNavigate)
        "payment" -> ClientDemoPayment(onPaid, onNavigate)
        "receipt" -> ClientDemoReceipt(paid, onNavigate)
        "bookings" -> ClientDemoBookings(paid, onNavigate)
        "manageBooking" -> ClientDemoManageBooking(onNavigate, onNotice)
    }
}

@Composable
private fun ClientDemoQuoteNew(
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    var date by rememberSaveable { mutableStateOf("") }
    var guests by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var extra by rememberSaveable { mutableStateOf(false) }
    val notice = clientText(R.string.demo_client_quote_saved)
    DemoPage(clientText(R.string.demo_client_quote_new), clientText(R.string.demo_client_venue_name)) {
        ClientDemoDisclaimer()
        DemoField(clientText(R.string.demo_client_date), date, { date = it })
        DemoField(clientText(R.string.demo_client_guests), guests, { guests = it })
        FilterChip(
            selected = extra,
            onClick = { extra = !extra },
            label = { Text(clientText(R.string.demo_client_catering_extra)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        DemoField(clientText(R.string.demo_client_message_optional), message, { message = it })
        DemoNotice(clientText(R.string.demo_client_quote_sample_hint))
        DemoAction(clientText(R.string.demo_client_simulate_request)) {
            onNotice(notice)
            onNavigate("quotes")
        }
    }
}

@Composable
private fun ClientDemoQuotes(onNavigate: (String) -> Unit) {
    var answered by rememberSaveable { mutableStateOf(true) }
    DemoPage(clientText(R.string.demo_client_quotes), clientText(R.string.demo_client_sample_inbox)) {
        ClientDemoDisclaimer()
        FilterChip(
            selected = answered,
            onClick = { answered = !answered },
            label = { Text(clientText(R.string.demo_client_answered)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        if (answered) {
            DemoCard(
                clientText(R.string.demo_client_venue_name),
                clientText(R.string.demo_client_quote_summary),
                badge = clientText(R.string.demo_client_answered),
                onClick = { onNavigate("quote") },
            )
        } else {
            DemoCard(
                clientText(R.string.demo_client_provider_name),
                clientText(R.string.demo_client_waiting_example),
            )
        }
        ClientDemoLink(R.string.demo_client_quote_new, "quoteNew", onNavigate)
    }
}

@Composable
private fun ClientDemoQuote(onNavigate: (String) -> Unit) {
    DemoPage(clientText(R.string.demo_client_quote), clientText(R.string.demo_client_quote_summary)) {
        ClientDemoDisclaimer()
        DemoCard(clientText(R.string.demo_client_venue_name), clientText(R.string.demo_client_event_summary))
        DemoSection(clientText(R.string.demo_client_breakdown))
        ClientDemoPriceBreakdown()
        DemoNotice(clientText(R.string.demo_client_quote_validity))
        ClientDemoLink(R.string.demo_client_request_changes, "chat", onNavigate)
        ClientDemoLink(R.string.demo_client_simulate_booking, "booking", onNavigate)
    }
}

@Composable
private fun ClientDemoBooking(onNavigate: (String) -> Unit) {
    var accepted by rememberSaveable { mutableStateOf(false) }
    DemoPage(clientText(R.string.demo_client_booking), clientText(R.string.demo_client_simulation)) {
        ClientDemoDisclaimer()
        DemoCard(clientText(R.string.demo_client_venue_name), clientText(R.string.demo_client_event_summary))
        DemoNotice(clientText(R.string.demo_client_booking_included))
        ClientDemoPriceBreakdown()
        DemoNotice(clientText(R.string.demo_client_deposit_detail))
        FilterChip(
            selected = accepted,
            onClick = { accepted = !accepted },
            label = { Text(clientText(R.string.demo_client_accept_simulation)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        if (accepted) ClientDemoLink(R.string.demo_client_continue_payment, "payment", onNavigate)
    }
}

@Composable
private fun ClientDemoPayment(
    onPaid: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var transfer by rememberSaveable { mutableStateOf(false) }
    var failed by rememberSaveable { mutableStateOf(false) }
    var pending by rememberSaveable { mutableStateOf(false) }
    DemoPage(clientText(R.string.demo_client_payment), clientText(R.string.demo_client_deposit_detail)) {
        ClientDemoDisclaimer()
        DemoNotice(clientText(R.string.demo_client_no_card_data))
        FilterChip(
            selected = transfer,
            onClick = { transfer = !transfer },
            label = { Text(clientText(R.string.demo_client_transfer_example)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        DemoCard(
            clientText(if (transfer) R.string.demo_client_transfer_example else R.string.demo_client_card_example),
            clientText(R.string.demo_client_no_bank),
        )
        if (failed) DemoNotice(clientText(R.string.demo_client_payment_failed))
        if (pending) DemoNotice(clientText(R.string.demo_client_payment_pending))
        DemoAction(clientText(R.string.demo_client_simulate_success)) {
            if (transfer) {
                pending = true
                failed = false
            } else {
                onPaid()
                onNavigate("receipt")
            }
        }
        DemoAction(clientText(R.string.demo_client_simulate_failure)) {
            failed = true
            pending = false
        }
    }
}

@Composable
private fun ClientDemoReceipt(
    paid: Boolean,
    onNavigate: (String) -> Unit,
) {
    DemoPage(clientText(R.string.demo_client_receipt), clientText(R.string.demo_client_demo_folio)) {
        ClientDemoDisclaimer()
        DemoNotice(clientText(if (paid) R.string.demo_client_paid_example else R.string.demo_client_unpaid_example))
        DemoCard(clientText(R.string.demo_client_venue_name), clientText(R.string.demo_client_event_summary))
        ClientDemoPriceBreakdown()
        DemoStats(
            listOf(
                clientText(R.string.demo_client_paid) to
                    clientText(if (paid) R.string.demo_client_deposit_amount else R.string.demo_client_zero),
                clientText(R.string.demo_client_balance) to
                    clientText(if (paid) R.string.demo_client_balance_amount else R.string.demo_client_total_amount),
            ),
        )
        DemoNotice(clientText(R.string.demo_client_receipt_not_valid))
        ClientDemoLink(R.string.demo_client_event, "event", onNavigate)
        ClientDemoLink(R.string.demo_client_bookings, "bookings", onNavigate)
    }
}

@Composable
private fun ClientDemoBookings(
    paid: Boolean,
    onNavigate: (String) -> Unit,
) {
    DemoPage(clientText(R.string.demo_client_bookings), clientText(R.string.demo_client_sample_history)) {
        ClientDemoDisclaimer()
        DemoCard(
            clientText(R.string.demo_client_venue_name),
            clientText(R.string.demo_client_demo_folio),
            badge = clientText(if (paid) R.string.demo_client_paid_example else R.string.demo_client_unpaid_example),
            onClick = { onNavigate("manageBooking") },
        )
        DemoSection(clientText(R.string.demo_client_previous))
        DemoCard(clientText(R.string.demo_client_venue_second), clientText(R.string.demo_client_past_booking))
        ClientDemoLink(R.string.demo_client_explore, "explore", onNavigate)
    }
}

@Composable
private fun ClientDemoManageBooking(
    onNavigate: (String) -> Unit,
    onNotice: (String) -> Unit,
) {
    var date by rememberSaveable { mutableStateOf("") }
    var cancelRequested by rememberSaveable { mutableStateOf(false) }
    var cancelled by rememberSaveable { mutableStateOf(false) }
    val rescheduled = clientText(R.string.demo_client_reschedule_notice)
    DemoPage(clientText(R.string.demo_client_manage_booking), clientText(R.string.demo_client_demo_folio)) {
        ClientDemoDisclaimer()
        DemoCard(clientText(R.string.demo_client_venue_name), clientText(R.string.demo_client_event_summary))
        DemoNotice(clientText(R.string.demo_client_cancellation_policy))
        if (cancelled) {
            DemoNotice(clientText(R.string.demo_client_cancelled_example))
        } else {
            DemoField(clientText(R.string.demo_client_new_date), date, { date = it })
            DemoAction(clientText(R.string.demo_client_simulate_reschedule)) { onNotice(rescheduled) }
            DemoAction(clientText(R.string.demo_client_simulate_cancel)) { cancelRequested = !cancelRequested }
            if (cancelRequested) {
                DemoNotice(clientText(R.string.demo_client_confirm_cancel_hint))
                DemoAction(clientText(R.string.demo_client_confirm_cancel)) { cancelled = true }
            }
        }
        ClientDemoLink(R.string.demo_client_receipt, "receipt", onNavigate)
    }
}

@Composable
internal fun ClientDemoPriceBreakdown() {
    DemoStats(
        listOf(
            clientText(R.string.demo_client_rental) to clientText(R.string.demo_client_rental_amount),
            clientText(R.string.demo_client_services) to clientText(R.string.demo_client_services_amount),
            clientText(R.string.demo_client_total) to clientText(R.string.demo_client_total_amount),
        ),
    )
}
