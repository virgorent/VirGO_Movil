package mx.virgo.app.ui.messages

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.GetQuotesUseCase
import mx.virgo.core.domain.Message
import mx.virgo.core.testing.FakeConversationsRepository
import mx.virgo.core.testing.FakeQuotesRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleConversation
import mx.virgo.core.testing.sampleQuote
import org.junit.Assert.assertEquals
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
class MessagesTest {
    @get:Rule
    val compose = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val conversations = FakeConversationsRepository()
    private val quotes = FakeQuotesRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))

    private fun viewModel() = MessagesViewModel(conversations, GetQuotesUseCase(quotes), session)

    @Test
    fun openingAConversationAndSendingAddsMyMessage() {
        conversations.conversations += sampleConversation()
        conversations.messages["c-1"] = mutableListOf(Message("m0", "Hola", null, mine = false))
        val vm = viewModel()
        vm.open(sampleConversation())
        vm.changeInput("  ¿Tienen fecha en mayo?  ")
        vm.send()
        val messages =
            vm.state.value.thread
                ?.messages
                .orEmpty()
        assertEquals(listOf("Hola", "¿Tienen fecha en mayo?"), messages.map { it.text })
        assertTrue(messages.last().mine)
        assertEquals("", vm.state.value.input)
    }

    @Test
    fun newMessageOffersQuotedVenuesAndOpensTheConversation() {
        quotes.quotes += sampleQuote()
        val vm = viewModel()
        vm.compose()
        assertEquals(
            listOf(sampleQuote().venueId),
            vm.state.value.composing
                ?.venues
                ?.map { it.first },
        )
        vm.compose(sampleQuote().venueId)
        vm.changeInput("Hola")
        vm.send()
        assertEquals(sampleQuote().venueId to "Hola", conversations.started.single())
        assertEquals(
            "c-${sampleQuote().venueId}",
            vm.state.value.thread
                ?.conversation
                ?.id,
        )
    }

    @Test
    fun failedSendKeepsTheText() {
        conversations.sendFailure = AppResult.Failure(AppError.Network)
        val vm = viewModel()
        vm.open(sampleConversation())
        vm.changeInput("Hola")
        vm.send()
        assertTrue(vm.state.value.sendFailed)
        assertEquals("Hola", vm.state.value.input)
    }

    @Test
    fun missingEndpointShowsSoon() {
        conversations.failure = AppResult.Failure(AppError.NotFound)
        assertEquals(InboxStatus.UNAVAILABLE, viewModel().state.value.status)
    }

    @Test
    fun inboxAndThreadRender() {
        val thread =
            ThreadState(sampleConversation(), listOf(Message("m", "Claro que sí", null, mine = false)), loading = false)
        compose.setContent {
            VirGoTheme {
                MessagesContent(
                    MessagesUiState(status = InboxStatus.READY, thread = thread),
                    MessagesActions(),
                )
            }
        }
        compose.onNodeWithText("Salón Las Palmas").assertIsDisplayed()
        compose.onNodeWithContentDescription("Salón: Claro que sí", substring = true).assertIsDisplayed()
    }
}
