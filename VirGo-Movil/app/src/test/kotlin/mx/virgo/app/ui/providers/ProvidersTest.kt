package mx.virgo.app.ui.providers

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.ui.ownerrequests.AnswerForm
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.Gig
import mx.virgo.core.domain.Provider
import mx.virgo.core.domain.ProviderCategory
import mx.virgo.core.domain.ProviderDraft
import mx.virgo.core.domain.ProviderField
import mx.virgo.core.domain.ProviderPackage
import mx.virgo.core.domain.ProviderPricing
import mx.virgo.core.domain.ProvidersRepository
import mx.virgo.core.domain.VenueReviews
import mx.virgo.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h800dp")
class ProvidersTest {
    @get:Rule
    val compose = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeProviders()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2027-05-10T00:00:00Z"), ZoneOffset.UTC))

    @Test
    fun catalogFiltersByCategoryAndComparesAtMostThree() {
        repository.catalog =
            listOf(
                provider("p-1", ProviderCategory.MUSIC),
                provider("p-2", ProviderCategory.MUSIC),
                provider("p-3", ProviderCategory.PHOTO),
                provider("p-4", ProviderCategory.CATERING),
            )
        val vm = ProvidersViewModel(repository, session)
        vm.selectCategory(ProviderCategory.MUSIC)
        assertEquals(
            listOf("p-1", "p-2"),
            vm.state.value.visible
                .map { it.id },
        )

        listOf("p-1", "p-2", "p-3", "p-4").forEach(vm::toggleCompare)
        assertEquals(listOf("p-1", "p-2", "p-3"), vm.state.value.compare)
        vm.showCompare(true)
        assertTrue(vm.state.value.comparing)
        vm.toggleCompare("p-2")
        vm.toggleCompare("p-3")
        assertFalse("con uno solo ya no hay comparación", vm.state.value.comparing)
    }

    @Test
    fun missingEndpointSaysComingSoon() {
        repository.failure = AppError.NotFound
        val vm = ProvidersViewModel(repository, session)
        compose.setContent { VirGoTheme { ProvidersCatalogContent(vm.state.value, ProvidersCatalogActions()) } }
        compose.onNodeWithText("Los proveedores llegarán pronto.").assertIsDisplayed()
    }

    @Test
    fun compareHighlightsTheBestRated() {
        compose.setContent {
            VirGoTheme {
                CompareTable(
                    listOf(provider("p-1", ProviderCategory.MUSIC, 4.1), provider("p-2", ProviderCategory.MUSIC, 4.8)),
                )
            }
        }
        compose.onNodeWithText("Mejor calificado").assertIsDisplayed()
        compose.onNodeWithText("4.8 (3)").assertIsDisplayed()
    }

    @Test
    fun publishingWithoutPackagesIsBlockedAndSaysWhy() {
        val vm = ProviderProfileViewModel(repository, session)
        vm.edit(
            ProviderDraft(
                businessName = "DJ Norte",
                category = ProviderCategory.MUSIC,
                description = "Música para bodas y graduaciones.",
                pricing = ProviderPricing.PER_EVENT,
                responseTime = "Menos de 2 h",
                zones = setOf("Centro"),
            ),
        )
        vm.publish()
        assertEquals(setOf(ProviderField.PACKAGES), vm.state.value.missing)
        assertEquals(0, repository.publishCalls)

        compose.setContent { VirGoTheme { ProviderProfileContent(vm.state.value, ProviderProfileActions()) } }
        compose.onNodeWithText("Agrega al menos un paquete con nombre y precio.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun conceptsFillTheAnswerTotal() {
        var form = AnswerForm()
        compose.setContent {
            VirGoTheme {
                ConceptsCard(listOf(ProviderPackage(name = "Básico", price = BigDecimal("12000"))), form) { form = it }
            }
        }
        compose.onNodeWithText("Usar este total").performClick()
        compose.runOnIdle {
            assertEquals("12000", form.total)
            assertEquals("1 × Básico", form.message)
        }
    }

    @Test
    fun overlappingGigsAreFlagged() {
        val monday = LocalDate.parse("2027-05-10")
        val day = monday.plusDays(1)
        val state =
            ProviderAgendaUiState(
                load = Load.READY,
                monday = monday,
                gigs =
                    listOf(
                        Gig("g-1", "Boda", date = day, start = LocalTime.of(18, 0), end = LocalTime.of(22, 0)),
                        Gig("g-2", "XV años", date = day, start = LocalTime.of(21, 0), end = LocalTime.of(23, 0)),
                    ),
            )
        compose.setContent { VirGoTheme { ProviderAgendaContent(state, ProviderAgendaActions()) } }
        compose.onNodeWithText("Se empalma con XV años", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Se empalma con Boda", substring = true).assertIsDisplayed()
    }

    private fun provider(
        id: String,
        category: ProviderCategory,
        rating: Double = 0.0,
    ) = Provider(
        id = id,
        businessName = "Proveedor $id",
        category = category,
        description = "Descripción del proveedor $id.",
        pricing = ProviderPricing.PER_EVENT,
        responseTime = "Menos de 2 h",
        packages = listOf(ProviderPackage(name = "Básico", price = BigDecimal("9000"))),
        zones = listOf("Centro"),
        coverUrl = null,
        gallery = emptyList(),
        published = true,
        ratingAverage = rating,
        ratingCount = if (rating > 0) 3 else 0,
    )
}

private class FakeProviders : ProvidersRepository {
    var catalog = emptyList<Provider>()
    var failure: AppError? = null
    var publishCalls = 0

    private fun <T> answer(value: T): AppResult<T> = failure?.let { AppResult.Failure(it) } ?: AppResult.Success(value)

    override suspend fun catalog(category: ProviderCategory?) = answer(catalog)

    override suspend fun provider(id: String) = answer(catalog.first { it.id == id })

    override suspend fun reviews(providerId: String) = answer(VenueReviews(0.0, 0, emptyList()))

    override suspend fun mine(): AppResult<Provider?> = answer(null)

    override suspend fun create(draft: ProviderDraft) = answer(catalog.first())

    override suspend fun update(draft: ProviderDraft) = answer(catalog.first())

    override suspend fun publish(): AppResult<Provider> {
        publishCalls++
        return answer(catalog.first())
    }

    override suspend fun gigs() = answer(emptyList<Gig>())

    override suspend fun saveGig(gig: Gig) = answer(gig)
}
