package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class AiRulesTest {
    @Test
    fun tablesFollowThePrototypeLayouts() {
        assertEquals(listOf(20, 20, 17), TableLayout.entries.map { it.tables(200) })
        assertEquals(listOf(170, 200, 150), TableLayout.entries.map { it.capacity(200) })
    }

    @Test
    fun staffUsesTheRulesWithMinimums() {
        assertEquals(
            mapOf(StaffRole.WAITERS to 7, StaffRole.SECURITY to 2, StaffRole.CLEANING to 3),
            EventPlanningRules.staff(200),
        )
        assertEquals(
            mapOf(StaffRole.WAITERS to 2, StaffRole.SECURITY to 1, StaffRole.CLEANING to 1),
            EventPlanningRules.staff(10),
        )
    }

    @Test
    fun decorationIsTwelvePercentOfTheBudget() {
        assertEquals(BigDecimal("18000"), EventPlanningRules.decorationBudget(BigDecimal("150000")))
    }

    @Test
    fun assistantSendsOnlyTheLastTenMessagesTrimmed() =
        runTest {
            var sent: Pair<String, Int>? = null
            val repository =
                object : AiRepository {
                    override suspend fun ideas(request: IdeasRequest) = AppResult.Success(emptyList<DecorIdea>())

                    override suspend fun ask(
                        message: String,
                        eventId: String?,
                        history: List<ChatMessage>,
                    ): AppResult<AssistantReply> {
                        sent = message to history.size
                        return AppResult.Success(AssistantReply("ok", uncertain = false))
                    }
                }
            val history = List(15) { ChatMessage("m$it", fromUser = it % 2 == 0) }
            AskAssistantUseCase(repository)("  ¿Cuántos meseros?  ", null, history)
            assertEquals("¿Cuántos meseros?" to 10, sent)
        }
}
