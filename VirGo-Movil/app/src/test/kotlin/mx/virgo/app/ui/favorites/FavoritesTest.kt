package mx.virgo.app.ui.favorites

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.Favorite
import mx.virgo.core.domain.FavoritesRepository
import mx.virgo.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h800dp")
class FavoritesTest {
    @get:Rule
    val compose = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeFavorites()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-27T00:00:00Z"), ZoneOffset.UTC))

    @Test
    fun listShowsOnlyVenues() {
        repository.list += listOf(venue("v-1", "Salón Las Palmas"), Favorite("provider", "p-1", "DJ", null, null))
        val vm = FavoritesViewModel(repository, session)
        assertEquals(FavoritesStatus.READY, vm.state.value.status)
        assertEquals(
            listOf("v-1"),
            vm.state.value.favorites
                .map { it.targetId },
        )
    }

    @Test
    fun missingEndpointSaysComingSoon() {
        repository.getFailure = AppResult.Failure(AppError.NotFound)
        val vm = FavoritesViewModel(repository, session)
        assertEquals(FavoritesStatus.UNAVAILABLE, vm.state.value.status)
    }

    @Test
    fun otherErrorAllowsRetry() {
        repository.getFailure = AppResult.Failure(AppError.Network)
        val vm = FavoritesViewModel(repository, session)
        assertEquals(FavoritesStatus.FAILED, vm.state.value.status)
        repository.getFailure = null
        vm.refresh()
        assertEquals(FavoritesStatus.READY, vm.state.value.status)
    }

    @Test
    fun removeDropsItAndFailureKeepsIt() {
        repository.list += listOf(venue("v-1", "A"), venue("v-2", "B"))
        val vm = FavoritesViewModel(repository, session)
        vm.remove("v-1")
        assertEquals(
            listOf("v-2"),
            vm.state.value.favorites
                .map { it.targetId },
        )
        repository.removeFailure = AppResult.Failure(AppError.Network)
        vm.remove("v-2")
        assertEquals(
            listOf("v-2"),
            vm.state.value.favorites
                .map { it.targetId },
        )
        assertTrue(vm.state.value.removeFailed)
        assertNull(vm.state.value.removing)
    }

    @Test
    fun heartIsHiddenWithoutBackendAndTogglesWithIt() {
        repository.getFailure = AppResult.Failure(AppError.NotFound)
        val hidden = FavoriteHeartViewModel(repository, session)
        hidden.load("v-1")
        assertNull(hidden.state.value.favorite)

        repository.getFailure = null
        val heart = FavoriteHeartViewModel(repository, session)
        heart.load("v-1")
        assertEquals(false, heart.state.value.favorite)
        heart.toggle("Salón Las Palmas")
        assertEquals(true, heart.state.value.favorite)
        assertFalse(heart.state.value.busy)
        assertEquals(listOf("add v-1 Salón Las Palmas"), repository.calls)
    }

    @Test
    fun heartRevertsWhenSavingFails() {
        val heart = FavoriteHeartViewModel(repository, session)
        heart.load("v-1")
        repository.addFailure = AppResult.Failure(AppError.Network)
        heart.toggle("A")
        assertEquals(false, heart.state.value.favorite)
        assertTrue(heart.state.value.failed)
    }

    @Test
    fun screenListsAndRemoves() {
        val removed = mutableListOf<String>()
        compose.setContent {
            VirGoTheme {
                FavoritesContent(
                    FavoritesUiState(FavoritesStatus.READY, listOf(venue("v-1", "Salón Las Palmas"))),
                    onRemove = { removed += it },
                    onRetry = {},
                )
            }
        }
        compose.onNodeWithText("Salón Las Palmas").assertIsDisplayed()
        compose.onNodeWithContentDescription("Salón Las Palmas", substring = true).performClick()
        assertEquals(listOf("v-1"), removed)
    }

    @Test
    fun screenSaysComingSoonWithoutEndpoint() {
        compose.setContent {
            VirGoTheme { FavoritesContent(FavoritesUiState(FavoritesStatus.UNAVAILABLE), onRemove = {}, onRetry = {}) }
        }
        compose.onNodeWithText("Favoritos llegará pronto.").assertIsDisplayed()
    }

    private fun venue(
        id: String,
        name: String,
    ) = Favorite("venue", id, name, null, 4.5)
}

private class FakeFavorites : FavoritesRepository {
    val list = mutableListOf<Favorite>()
    val calls = mutableListOf<String>()
    var getFailure: AppResult.Failure? = null
    var addFailure: AppResult.Failure? = null
    var removeFailure: AppResult.Failure? = null

    override suspend fun getFavorites(): AppResult<List<Favorite>> = getFailure ?: AppResult.Success(list.toList())

    override suspend fun addFavorite(
        targetType: String,
        targetId: String,
        name: String?,
        photoUrl: String?,
        rating: Double?,
    ): AppResult<Unit> {
        calls += "add $targetId $name"
        return addFailure ?: AppResult.Success(Unit)
    }

    override suspend fun removeFavorite(
        targetType: String,
        targetId: String,
    ): AppResult<Unit> {
        calls += "remove $targetId"
        return removeFailure ?: AppResult.Success(Unit)
    }
}
