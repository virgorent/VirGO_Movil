package mx.virgo.app.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
internal fun ClientDemoCatalog(
    screen: String,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    when (screen) {
        "explore" -> ClientDemoExplore(onNavigate)
        "favorites" -> ClientDemoFavorites(favorite, onFavorite, onNavigate)
        "venue" -> ClientDemoVenue(favorite, onFavorite, onNavigate)
        "providers" -> ClientDemoProviders(onNavigate)
        "provider" -> ClientDemoProvider(onNavigate)
        "compare" -> ClientDemoCompare(onNavigate)
        "recommended" -> ClientDemoRecommended(onNavigate)
    }
}

@Composable
private fun ClientDemoExplore(onNavigate: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var gardens by rememberSaveable { mutableStateOf(false) }
    val name = clientText(R.string.demo_client_venue_name)
    val second = clientText(R.string.demo_client_venue_second)
    DemoPage(clientText(R.string.demo_client_explore), clientText(R.string.demo_client_explore_intro)) {
        ClientDemoDisclaimer()
        DemoField(clientText(R.string.demo_client_search), query, { query = it })
        FilterChip(
            selected = gardens,
            onClick = { gardens = !gardens },
            label = { Text(clientText(R.string.demo_client_gardens)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        DemoSection(clientText(R.string.demo_client_spaces))
        DemoVenueMap(onNavigate)
        if (name.contains(query, ignoreCase = true)) {
            DemoCard(name, clientText(R.string.demo_client_venue_detail), onClick = { onNavigate("venue") })
        }
        if (!gardens && second.contains(query, ignoreCase = true)) {
            DemoCard(second, clientText(R.string.demo_client_second_detail), onClick = { onNavigate("venue") })
        }
        ClientDemoLink(R.string.demo_client_recommended, "recommended", onNavigate)
        ClientDemoLink(R.string.demo_client_providers, "providers", onNavigate)
    }
}

@Composable
private fun ClientDemoFavorites(
    favorite: Boolean,
    onFavorite: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var providers by rememberSaveable { mutableStateOf(false) }
    DemoPage(clientText(R.string.demo_client_favorites), clientText(R.string.demo_client_collection)) {
        ClientDemoDisclaimer()
        FilterChip(
            selected = providers,
            onClick = { providers = !providers },
            label = { Text(clientText(R.string.demo_client_providers)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        if (providers) {
            DemoCard(
                clientText(R.string.demo_client_provider_name),
                clientText(R.string.demo_client_provider_detail),
                onClick = { onNavigate("provider") },
            )
        } else if (favorite) {
            DemoCard(clientText(R.string.demo_client_venue_name), onClick = { onNavigate("venue") })
            DemoAction(clientText(R.string.demo_client_remove_favorite), onFavorite)
        } else {
            DemoNotice(clientText(R.string.demo_client_no_favorites))
            ClientDemoLink(R.string.demo_client_explore, "explore", onNavigate)
        }
    }
}

@Composable
private fun ClientDemoVenue(
    favorite: Boolean,
    onFavorite: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var date by rememberSaveable { mutableStateOf("") }
    var catering by rememberSaveable { mutableStateOf(false) }
    DemoPage(clientText(R.string.demo_client_venue_name), clientText(R.string.demo_client_venue_detail)) {
        ClientDemoDisclaimer()
        DemoVenueArtwork()
        DemoStats(
            listOf(
                clientText(R.string.demo_client_capacity) to clientText(R.string.demo_client_people_150),
                clientText(R.string.demo_client_rating) to clientText(R.string.demo_client_rating_value),
            ),
        )
        DemoNotice(clientText(R.string.demo_client_amenities))
        val favoriteLabel = if (favorite) R.string.demo_client_remove_favorite else R.string.demo_client_save_favorite
        DemoAction(clientText(favoriteLabel)) {
            onFavorite()
        }
        DemoField(clientText(R.string.demo_client_date), date, { date = it })
        FilterChip(
            selected = catering,
            onClick = { catering = !catering },
            label = { Text(clientText(R.string.demo_client_catering_extra)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        ClientDemoLink(R.string.demo_client_studio, "studio", onNavigate)
        ClientDemoLink(R.string.demo_client_quote_new, "quoteNew", onNavigate)
        ClientDemoLink(R.string.demo_client_reviews, "venueReviews", onNavigate)
    }
}

@Composable
private fun ClientDemoProviders(onNavigate: (String) -> Unit) {
    var music by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(false) }
    DemoPage(clientText(R.string.demo_client_providers), clientText(R.string.demo_client_compare_hint)) {
        ClientDemoDisclaimer()
        FilterChip(
            selected = music,
            onClick = { music = !music },
            label = { Text(clientText(R.string.demo_client_music)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        DemoCard(
            clientText(if (music) R.string.demo_client_music_name else R.string.demo_client_provider_name),
            clientText(if (music) R.string.demo_client_music_detail else R.string.demo_client_provider_detail),
            onClick = { onNavigate("provider") },
        )
        DemoCard(
            clientText(R.string.demo_client_provider_second),
            clientText(R.string.demo_client_provider_second_detail),
        )
        FilterChip(
            selected = selected,
            onClick = { selected = !selected },
            label = { Text(clientText(R.string.demo_client_select_comparison)) },
            modifier = Modifier.heightIn(min = ClientDemoTouchTarget),
        )
        if (selected) ClientDemoLink(R.string.demo_client_compare, "compare", onNavigate)
    }
}

@Composable
private fun ClientDemoProvider(onNavigate: (String) -> Unit) {
    var added by rememberSaveable { mutableStateOf(false) }
    DemoPage(clientText(R.string.demo_client_provider_name), clientText(R.string.demo_client_provider_detail)) {
        ClientDemoDisclaimer()
        DemoServiceArtwork()
        DemoNotice(clientText(R.string.demo_client_provider_description))
        DemoSection(clientText(R.string.demo_client_packages))
        DemoCard(
            clientText(R.string.demo_client_package_classic),
            clientText(R.string.demo_client_package_classic_detail),
        )
        DemoCard(
            clientText(R.string.demo_client_package_premium),
            clientText(R.string.demo_client_package_premium_detail),
        )
        DemoAction(clientText(R.string.demo_client_add_example)) { added = !added }
        if (added) DemoNotice(clientText(R.string.demo_client_added_example))
        ClientDemoLink(R.string.demo_client_quote_new, "quoteNew", onNavigate)
        ClientDemoLink(R.string.demo_client_messages, "chat", onNavigate)
    }
}

@Composable
private fun ClientDemoCompare(onNavigate: (String) -> Unit) {
    DemoPage(clientText(R.string.demo_client_compare), clientText(R.string.demo_client_comparison_sample)) {
        ClientDemoDisclaimer()
        Row(horizontalArrangement = Arrangement.spacedBy(ClientDemoComparisonSpacing)) {
            Text(clientText(R.string.demo_client_provider_short))
            Text(clientText(R.string.demo_client_provider_second))
        }
        DemoStats(
            listOf(
                clientText(R.string.demo_client_price) to clientText(R.string.demo_client_comparison_prices),
                clientText(R.string.demo_client_rating) to clientText(R.string.demo_client_comparison_ratings),
                clientText(R.string.demo_client_response) to clientText(R.string.demo_client_comparison_response),
            ),
        )
        DemoNotice(clientText(R.string.demo_client_comparison_verdict))
        ClientDemoLink(R.string.demo_client_view_provider, "provider", onNavigate)
    }
}

private val ClientDemoComparisonSpacing =
    androidx.compose.ui.unit
        .Dp(16f)

@Composable
private fun ClientDemoRecommended(onNavigate: (String) -> Unit) {
    DemoPage(clientText(R.string.demo_client_recommended), clientText(R.string.demo_client_event_summary)) {
        ClientDemoDisclaimer()
        DemoNotice(clientText(R.string.demo_client_recommendation_example))
        DemoSection(clientText(R.string.demo_client_spaces))
        DemoCard(
            clientText(R.string.demo_client_venue_name),
            clientText(R.string.demo_client_recommendation_reason),
            badge = clientText(R.string.demo_client_match),
            onClick = { onNavigate("venue") },
        )
        DemoSection(clientText(R.string.demo_client_providers))
        DemoCard(
            clientText(R.string.demo_client_provider_name),
            clientText(R.string.demo_client_provider_detail),
            onClick = { onNavigate("provider") },
        )
    }
}
