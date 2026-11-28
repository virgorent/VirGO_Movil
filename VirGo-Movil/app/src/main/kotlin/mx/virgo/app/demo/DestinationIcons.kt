package mx.virgo.app.demo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.ChecklistRtl
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector

/** Ícono de Material por destino (035 plan §6.2), equivalente a los `lucide` del prototipo. */
@Suppress("CyclomaticComplexMethod")
internal fun destinationIcon(route: String): ImageVector =
    when (route) {
        "explore" -> Icons.Outlined.Explore
        "favorites" -> Icons.Outlined.FavoriteBorder
        "studio", "budgetAI", "assistant" -> Icons.Outlined.AutoAwesome
        "event", "timeline" -> Icons.Outlined.Event
        "ownerCalendar", "providerAgenda", "reminders" -> Icons.Outlined.CalendarMonth
        "profile", "providerProfile" -> Icons.Outlined.Person
        "messages", "chat" -> Icons.AutoMirrored.Outlined.Chat
        "notifications" -> Icons.Outlined.Notifications
        "help" -> Icons.AutoMirrored.Outlined.HelpOutline
        "quoteNew", "quotes", "quote" -> Icons.Outlined.RequestQuote
        "booking", "bookings", "manageBooking" -> Icons.Outlined.EventAvailable
        "payment", "receipt", "payments", "ownerPayments" -> Icons.Outlined.Payments
        "budget" -> Icons.Outlined.Savings
        "guests", "invitation" -> Icons.Outlined.Groups
        "tasks" -> Icons.Outlined.ChecklistRtl
        "share" -> Icons.Outlined.Share
        "venue", "ownerVenues", "venueEditor", "recommended", "compare" -> Icons.Outlined.Storefront
        "providers", "provider", "ownerAllies" -> Icons.Outlined.Handshake
        "ownerRequests", "ownerRequest", "providerRequests", "providerRequest" -> Icons.Outlined.Inbox
        "ownerStats", "providerStats" -> Icons.Outlined.BarChart
        "venueReviews", "review", "ownerReviews", "providerReviews" -> Icons.Outlined.Star
        "ownerPricing" -> Icons.Outlined.Tune
        "ownerSetups" -> Icons.Outlined.TableRestaurant
        "providerGallery" -> Icons.Outlined.PhotoLibrary
        "providerZones" -> Icons.Outlined.Place
        "ownerHome", "providerHome" -> Icons.Outlined.Home
        else -> Icons.Outlined.Celebration
    }
