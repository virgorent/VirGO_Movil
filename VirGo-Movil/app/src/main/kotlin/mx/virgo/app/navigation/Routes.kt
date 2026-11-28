package mx.virgo.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import mx.virgo.app.R

@Serializable
data object GuestHomeRoute

@Serializable
data class RegisterRoute(
    val venueId: Int? = null,
)

/** 006: detalle público de un salón publicado. */
@Serializable
data class VenueDetailRoute(
    val venueId: String,
)

@Serializable
data object LoginRoute

@Serializable
data object RecoverRoute

@Serializable
data object HomeRoute

data class TopLevelDestination(
    val icon: ImageVector,
    val labelRes: Int,
)

val topLevelDestinations =
    listOf(
        TopLevelDestination(
            icon = Icons.Filled.Home,
            labelRes = R.string.home_tab,
        ),
    )
