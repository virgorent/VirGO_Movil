package mx.virgo.app.ui.finance

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.BudgetCategory
import mx.virgo.core.domain.BudgetPreference
import mx.virgo.core.domain.EventFinance
import mx.virgo.core.domain.ExternalPayment
import mx.virgo.core.domain.PaymentStatus
import mx.virgo.core.testing.FakeEventFinanceRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h800dp")
class FinanceTest {
    @get:Rule
    val compose = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeEventFinanceRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))
    private val today = LocalDate.parse("2026-11-27")

    private fun payments() =
        PaymentsViewModel(repository, session).also {
            it.today = { today }
            it.newId = { "nuevo" }
        }

    @Test
    fun applyingTheSuggestionSendsAllSixCategories() {
        val vm = BudgetViewModel(repository, session)
        vm.togglePreference(BudgetPreference.PREMIUM_FOOD)
        vm.edit(BudgetCategory.MUSIC, "9000")
        vm.apply()
        val sent = repository.savedPlans.single()
        assertEquals(BudgetCategory.entries.toSet(), sent.keys)
        assertEquals(BigDecimal("9000"), sent[BudgetCategory.MUSIC])
        assertEquals(SaveNotice.SAVED, vm.state.value.notice)
    }

    @Test
    fun backendWithoutTheFieldSaysSoon() {
        repository.saveFailure = AppResult.Failure(AppError.Validation(emptyMap()))
        val vm = BudgetViewModel(repository, session)
        vm.apply()
        assertEquals(SaveNotice.UNAVAILABLE, vm.state.value.notice)
    }

    @Test
    fun invalidPaymentIsNotSentAndValidOneIsAddedFirst() {
        val vm = payments()
        vm.updateForm(PaymentForm(concept = "", amount = "0"))
        vm.add()
        assertTrue(repository.savedPayments.isEmpty())
        vm.updateForm(PaymentForm(concept = "Anticipo DJ", amount = "3000.50"))
        vm.add()
        val saved = repository.savedPayments.single().single()
        assertEquals("nuevo", saved.id)
        assertEquals(BigDecimal("3000.50"), saved.amount)
        assertEquals(today, saved.paidAt)
        assertNull(vm.state.value.form)
    }

    @Test
    fun failedSaveRestoresTheList() {
        val old = ExternalPayment("p1", "Salón", BigDecimal("15000"), PaymentStatus.COMPLETED, today)
        repository.finance = EventFinance(sampleEvent(), emptyMap(), listOf(old))
        repository.saveFailure = AppResult.Failure(AppError.Network)
        val vm = payments()
        vm.remove("p1")
        assertEquals(
            listOf(old),
            vm.state.value.finance
                ?.payments,
        )
        assertEquals(SaveNotice.FAILED, vm.state.value.notice)
    }

    @Test
    fun budgetScreenShowsTotalsAndOverPlanAlert() {
        val finance = EventFinance(sampleEvent(), mapOf(BudgetCategory.VENUE to BigDecimal("160000")), emptyList())
        compose.setContent {
            VirGoTheme { BudgetContent(BudgetUiState(loading = false, finance = finance), FinanceLinks(), {}) }
        }
        compose.onNodeWithText("El plan supera tu presupuesto", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Presupuesto sugerido").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun paymentsWithoutEventOfferMyEvent() {
        compose.setContent { VirGoTheme { PaymentsContent(PaymentsUiState(loading = false), PaymentsActions()) } }
        compose.onNodeWithText("Ir a Mi Evento").assertIsDisplayed()
    }
}
