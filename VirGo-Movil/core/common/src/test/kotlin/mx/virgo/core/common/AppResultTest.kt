package mx.virgo.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResultTest {
    @Test
    fun successKeepsItsValue() {
        val result: AppResult<Int> = AppResult.Success(7)
        assertTrue(result is AppResult.Success)
        assertEquals(7, (result as AppResult.Success).value)
    }

    @Test
    fun failureKeepsItsError() {
        val result: AppResult<Int> = AppResult.Failure(AppError.Unauthorized)
        assertTrue(result is AppResult.Failure)
        assertEquals(AppError.Unauthorized, (result as AppResult.Failure).error)
    }

    @Test
    fun equalValuesAreEqual() {
        assertEquals(AppResult.Success("a"), AppResult.Success("a"))
        assertEquals(AppError.Server(traceId = "t1"), AppError.Server(traceId = "t1"))
        assertEquals(
            AppError.Validation(fields = mapOf("email" to listOf("invalid"))),
            AppError.Validation(fields = mapOf("email" to listOf("invalid"))),
        )
    }

    @Test
    fun differentValuesAreNotEqual() {
        assertNotEquals(AppResult.Success(1), AppResult.Success(2))
        assertNotEquals(AppError.Server(traceId = "t1"), AppError.Server(traceId = "t2"))
        assertNotEquals(AppError.Network, AppError.Timeout)
    }
}
