package mx.virgo.app.demo

import androidx.annotation.StringRes
import mx.virgo.app.R

enum class DemoRole(
    @param:StringRes val label: Int,
    val home: String,
    val tabs: List<String>,
) {
    CLIENT(R.string.demo_role_client, "explore", listOf("explore", "favorites", "studio", "event", "profile")),
    OWNER(
        R.string.demo_role_owner,
        "ownerHome",
        listOf("ownerHome", "ownerCalendar", "ownerRequests", "messages", "profile"),
    ),
    PROVIDER(
        R.string.demo_role_provider,
        "providerHome",
        listOf("providerHome", "providerAgenda", "providerRequests", "messages", "profile"),
    ),
}

data class DemoDestination(
    val route: String,
    @param:StringRes val title: Int,
)

object DemoDestinations {
    val shared =
        listOf(
            DemoDestination("profile", R.string.demo_title_profile),
            DemoDestination("messages", R.string.demo_title_messages),
            DemoDestination("chat", R.string.demo_title_chat),
            DemoDestination("notifications", R.string.demo_title_notifications),
            DemoDestination("help", R.string.demo_title_help),
        )

    val client =
        listOf(
            DemoDestination("explore", R.string.demo_title_explore),
            DemoDestination("favorites", R.string.demo_title_favorites),
            DemoDestination("venue", R.string.demo_title_venue),
            DemoDestination("studio", R.string.demo_title_studio),
            DemoDestination("event", R.string.demo_title_event),
            DemoDestination("providers", R.string.demo_title_providers),
            DemoDestination("provider", R.string.demo_title_provider),
            DemoDestination("compare", R.string.demo_title_compare),
            DemoDestination("quoteNew", R.string.demo_title_quote_new),
            DemoDestination("quotes", R.string.demo_title_quotes),
            DemoDestination("quote", R.string.demo_title_quote),
            DemoDestination("booking", R.string.demo_title_booking),
            DemoDestination("payment", R.string.demo_title_payment),
            DemoDestination("receipt", R.string.demo_title_receipt),
            DemoDestination("bookings", R.string.demo_title_bookings),
            DemoDestination("manageBooking", R.string.demo_title_manage_booking),
            DemoDestination("budget", R.string.demo_title_budget),
            DemoDestination("budgetAI", R.string.demo_title_budget_ai),
            DemoDestination("payments", R.string.demo_title_payments),
            DemoDestination("guests", R.string.demo_title_guests),
            DemoDestination("tasks", R.string.demo_title_tasks),
            DemoDestination("timeline", R.string.demo_title_timeline),
            DemoDestination("reminders", R.string.demo_title_reminders),
            DemoDestination("share", R.string.demo_title_share),
            DemoDestination("invitation", R.string.demo_title_invitation),
            DemoDestination("recommended", R.string.demo_title_recommended),
            DemoDestination("venueReviews", R.string.demo_title_venue_reviews),
            DemoDestination("review", R.string.demo_title_review),
            DemoDestination("assistant", R.string.demo_title_assistant),
        )

    val owner =
        listOf(
            DemoDestination("ownerHome", R.string.demo_title_home),
            DemoDestination("ownerVenues", R.string.demo_title_owner_venues),
            DemoDestination("venueEditor", R.string.demo_title_venue_editor),
            DemoDestination("ownerCalendar", R.string.demo_title_calendar),
            DemoDestination("ownerPricing", R.string.demo_title_pricing),
            DemoDestination("ownerSetups", R.string.demo_title_setups),
            DemoDestination("ownerRequests", R.string.demo_title_requests),
            DemoDestination("ownerRequest", R.string.demo_title_request),
            DemoDestination("ownerPayments", R.string.demo_title_payments),
            DemoDestination("ownerStats", R.string.demo_title_stats),
            DemoDestination("ownerReviews", R.string.demo_title_reviews),
            DemoDestination("ownerAllies", R.string.demo_title_allies),
        )

    val provider =
        listOf(
            DemoDestination("providerHome", R.string.demo_title_home),
            DemoDestination("providerProfile", R.string.demo_title_provider_profile),
            DemoDestination("providerGallery", R.string.demo_title_gallery),
            DemoDestination("providerZones", R.string.demo_title_zones),
            DemoDestination("providerRequests", R.string.demo_title_requests),
            DemoDestination("providerRequest", R.string.demo_title_request),
            DemoDestination("providerAgenda", R.string.demo_title_agenda),
            DemoDestination("providerReviews", R.string.demo_title_reviews),
            DemoDestination("providerStats", R.string.demo_title_stats),
        )

    fun forRole(role: DemoRole): List<DemoDestination> =
        shared +
            when (role) {
                DemoRole.CLIENT -> client
                DemoRole.OWNER -> owner
                DemoRole.PROVIDER -> provider
            }

    fun allows(
        role: DemoRole,
        route: String,
    ): Boolean = forRole(role).any { it.route == route }

    fun title(
        role: DemoRole,
        route: String,
    ): Int = forRole(role).first { it.route == route }.title
}
