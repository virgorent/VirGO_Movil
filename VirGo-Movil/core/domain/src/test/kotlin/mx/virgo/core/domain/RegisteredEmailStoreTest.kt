package mx.virgo.core.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisteredEmailStoreTest {
    private val store = RegisteredEmailStore

    @Test
    fun registersNewEmail() {
        val email = "new-${System.nanoTime()}@mail.com"
        assertTrue(store.register(email))
    }

    @Test
    fun rejectsDuplicateEmail() {
        val email = "dup-${System.nanoTime()}@mail.com"
        assertTrue(store.register(email))
        assertFalse(store.register(email))
    }

    @Test
    fun differentEmailsAreIndependent() {
        assertTrue(store.register("a-${System.nanoTime()}@mail.com"))
        assertTrue(store.register("b-${System.nanoTime()}@mail.com"))
    }
}
