package mx.virgo.app.ui.guest

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import mx.virgo.app.R
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.GetVenuesUseCase
import mx.virgo.core.testing.FakeVenueRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h640dp")
class GuestAccessibilityTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun compactLightHomeFits() = assertHome(dark = false, compact = true)

    @Test
    fun compactDarkHomeFits() = assertHome(dark = true, compact = true)

    @Test
    fun compactLightPreviewFits() = assertPreview(dark = false, compact = true)

    @Test
    fun compactDarkPreviewFits() = assertPreview(dark = true, compact = true)

    @Test
    fun compactLightErrorCanRetry() = assertError(dark = false, compact = true)

    @Test
    fun compactDarkErrorCanRetry() = assertError(dark = true, compact = true)

    @Test
    @Config(qualifiers = "w393dp-h800dp")
    fun regularLightHomeFits() = assertHome(dark = false, compact = false)

    @Test
    @Config(qualifiers = "w393dp-h800dp")
    fun regularDarkHomeFits() = assertHome(dark = true, compact = false)

    @Test
    @Config(qualifiers = "w393dp-h800dp")
    fun regularLightPreviewFits() = assertPreview(dark = false, compact = false)

    @Test
    @Config(qualifiers = "w393dp-h800dp")
    fun regularDarkPreviewFits() = assertPreview(dark = true, compact = false)

    @Test
    @Config(qualifiers = "w393dp-h800dp")
    fun regularLightErrorCanRetry() = assertError(dark = false, compact = false)

    @Test
    @Config(qualifiers = "w393dp-h800dp")
    fun regularDarkErrorCanRetry() = assertError(dark = true, compact = false)

    @Test
    fun oversizedLabelsKeepEastAndWestCompactPinsSeparateAndTouchable() {
        val east =
            VenueSamples.first().copy(
                id = 100,
                name = "East sample",
                short = "Long label ".repeat(30),
                bearing = 90.0,
            )
        val west = east.copy(id = 101, name = "West sample", bearing = 270.0)
        var selected = NO_VENUE
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                VirGoTheme(darkTheme = true) {
                    GuestMap(
                        nearby = listOf(east, west),
                        radiusKm = 5,
                        selectedId = NO_VENUE,
                        onVenueClick = { selected = it },
                        onClearSelection = { selected = NO_VENUE },
                        modifier = Modifier.fillMaxWidth().height(360.dp).testTag("pin_map"),
                    )
                }
            }
        }
        val eastPin = compose.onNodeWithContentDescription(string(R.string.guest_pin_cd, east.name, east.kmText()))
        val westPin = compose.onNodeWithContentDescription(string(R.string.guest_pin_cd, west.name, west.kmText()))
        eastPin.assertIsDisplayed().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        westPin.assertIsDisplayed().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        val eastBounds = eastPin.getUnclippedBoundsInRoot()
        val westBounds = westPin.getUnclippedBoundsInRoot()
        val map = compose.onNodeWithTag("pin_map").getUnclippedBoundsInRoot()
        assertTrue("Pins must keep their opposite sides", westBounds.right <= eastBounds.left)
        assertTrue("West target must stay inside the map", westBounds.left >= map.left)
        assertTrue("East target must stay inside the map", eastBounds.right <= map.right)
        eastPin.performTouchInput { click(center) }
        compose.runOnIdle { assertEquals(east.id, selected) }
        westPin.performTouchInput { click(center) }
        compose.runOnIdle { assertEquals(west.id, selected) }
    }

    private fun assertHome(
        dark: Boolean,
        compact: Boolean,
    ) {
        val vm = showHome(dark, compact)
        assertFits(R.string.guest_brand, compact)
        assertFits(R.string.guest_login, compact)
        assertFits(R.string.guest_section_label, compact)
        assertFits(R.string.guest_location, compact)
        val ranges = listOf(2, 5, 10)
        ranges.forEach { radius ->
            val option = string(R.string.guest_range_option, radius)
            val node =
                compose.onNode(
                    hasText(option) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton),
                )
            if (compact) node.performScrollTo()
            node
                .assertIsDisplayed()
                .assertHeightIsAtLeast(48.dp)
                .assertWidthIsAtLeast(48.dp)
                .performClick()
            node.assertIsSelected()
            assertEquals(radius, vm.state.value.radiusKm)
            assertEquals(VenueSamples.count { it.distanceKm <= radius }, vm.state.value.nearby.size)
        }
        assertFits(R.string.guest_hint_touch, compact)
        if (compact) assertRegionsAreSeparated()
    }

    private fun assertPreview(
        dark: Boolean,
        compact: Boolean,
    ) {
        val vm = showHome(dark, compact)
        val venue = VenueSamples.first { it.id == 5 }
        compose.runOnIdle { vm.selectVenue(venue.id) }
        val title = compose.onNodeWithText(venue.name)
        if (compact) title.performScrollTo()
        title.assertIsDisplayed()
        assertFits(R.string.guest_preview_see_more, compact)
        assertFits(R.string.guest_preview_locked, compact)
        assertFits(R.string.sample_photo_credits, compact)
        val close = compose.onNodeWithContentDescription(string(R.string.guest_preview_close))
        if (compact) close.performScrollTo()
        close.assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp).performClick()
        compose.runOnIdle { assertEquals(NO_VENUE, vm.state.value.selectedVenueId) }
        assertFits(R.string.guest_hint_touch, compact)
    }

    private fun assertError(
        dark: Boolean,
        compact: Boolean,
    ) {
        val repository = FakeVenueRepository().apply { result = AppResult.Failure(AppError.Network) }
        val vm = showHome(dark, compact, repository)
        assertFits(R.string.guest_error_load, compact)
        compose
            .onNodeWithText(string(R.string.guest_error_load), useUnmergedTree = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        assertFits(R.string.guest_retry, compact)
        val initialCalls = repository.calls
        compose.runOnIdle { repository.result = AppResult.Success(emptyList()) }
        compose.onNodeWithText(string(R.string.guest_retry)).performClick()
        compose.waitForIdle()
        assertEquals(initialCalls + 1, repository.calls)
        assertFalse(vm.state.value.hasError)
        assertFits(R.string.guest_hint_touch, compact)
        if (compact) assertRegionsAreSeparated()
    }

    private fun showHome(
        dark: Boolean,
        compact: Boolean,
        repository: FakeVenueRepository = FakeVenueRepository(),
    ): GuestHomeViewModel {
        val vm = GuestHomeViewModel(GetVenuesUseCase(repository))
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = if (compact) 2f else 1f),
            ) {
                VirGoTheme(darkTheme = dark) {
                    GuestHomeScreen(onLoginClick = {}, onRegisterClick = {}, viewModel = vm)
                }
            }
        }
        compose.onRoot().assertWidthIsEqualTo(if (compact) 320.dp else 393.dp)
        compose.onRoot().assertHeightIsEqualTo(if (compact) 640.dp else 800.dp)
        return vm
    }

    private fun assertRegionsAreSeparated() {
        val header = compose.onNodeWithTag("guest_header").getUnclippedBoundsInRoot()
        val map = compose.onNodeWithTag("guest_map").getUnclippedBoundsInRoot()
        val footer = compose.onNodeWithTag("guest_bottom").getUnclippedBoundsInRoot()
        assertTrue("Header must not cover the map", header.bottom <= map.top)
        assertTrue("Footer must not cover the map", map.bottom <= footer.top)
    }

    private fun assertFits(
        resId: Int,
        scroll: Boolean,
    ) {
        val node = compose.onNodeWithText(string(resId))
        if (scroll) node.performScrollTo()
        node.assertIsDisplayed()
        val layouts = mutableListOf<TextLayoutResult>()
        compose
            .onNodeWithText(string(resId), useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { assertTrue(it(layouts)) }
        val layout = layouts.single()
        val message = "Guest text must not be truncated: ${string(resId)}"
        assertFalse(message, layout.didOverflowHeight)
        assertFalse(message, layout.multiParagraph.didExceedMaxLines)
        repeat(layout.lineCount) { line ->
            assertFalse(message, layout.isLineEllipsized(line))
            assertTrue(message, layout.getLineLeft(line) >= 0f)
            assertTrue(message, layout.getLineRight(line) <= layout.size.width)
        }
    }

    private fun string(
        resId: Int,
        vararg args: Any,
    ): String = ApplicationProvider.getApplicationContext<Context>().getString(resId, *args)
}
