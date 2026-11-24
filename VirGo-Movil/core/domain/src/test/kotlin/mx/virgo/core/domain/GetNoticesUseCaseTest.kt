package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetNoticesUseCaseTest {
    private class FakeNoticesRepository(
        var result: AppResult<List<Notice>> = AppResult.Success(emptyList()),
    ) : NoticesRepository {
        var calls = 0

        override suspend fun getNotices(): AppResult<List<Notice>> {
            calls += 1
            return result
        }
    }

    private val unread =
        Notice(
            id = "n-1",
            type = "booking",
            title = "Reserva confirmada",
            body = "Casa VirGo te espera",
            isRead = false,
            createdAt = "2026-11-18T10:00:00Z",
        )

    @Test
    fun invokeReturnsTheRepositoryNotices() =
        runTest {
            val repository = FakeNoticesRepository(result = AppResult.Success(listOf(unread)))
            val useCase = GetNoticesUseCase(repository)

            val result = useCase()

            assertEquals(AppResult.Success(listOf(unread)), result)
            assertEquals(1, repository.calls)
        }

    @Test
    fun invokePropagatesRepositoryFailure() =
        runTest {
            val repository = FakeNoticesRepository(result = AppResult.Failure(AppError.NotFound))
            val useCase = GetNoticesUseCase(repository)

            val result = useCase()

            assertEquals(AppResult.Failure(AppError.NotFound), result)
        }

    @Test
    fun noticeComparesAllFieldsAndKeepsReadState() {
        val same =
            Notice(
                id = "n-1",
                type = "booking",
                title = "Reserva confirmada",
                body = "Casa VirGo te espera",
                isRead = false,
                createdAt = "2026-11-18T10:00:00Z",
            )

        assertEquals(unread, same)
        assertEquals(unread.hashCode(), same.hashCode())
        assertFalse(unread.isRead)
        assertTrue(unread.copy(isRead = true).isRead)
        assertEquals(false, unread == same.copy(title = "Otro aviso"))
    }
}
