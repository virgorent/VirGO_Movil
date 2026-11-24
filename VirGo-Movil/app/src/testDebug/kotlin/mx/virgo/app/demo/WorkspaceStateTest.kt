package mx.virgo.app.demo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceStateTest {
    @Test
    fun everySheetStoresItsRecordInSharedState() {
        val workspace = WorkspaceViewModel()
        WorkspaceSection.entries.forEach { section ->
            val record = WorkspaceRecord(name = section.name, summary = "Local", amount = 1.0)
            assertTrue(workspace.confirm(section, record))
            assertEquals(record, workspace.uiState.value.latest(section))
        }
    }

    @Test
    fun accountsAndRolesNeverShareRecordsFavoritesOrExamplePayments() {
        val accountA = WorkspaceViewModel()
        val accountB = WorkspaceViewModel()
        val ownerA = WorkspaceViewModel()
        accountA.initializeProfile("Cuenta A", "a@example.invalid")
        accountA.confirm(WorkspaceSection.EVENT, WorkspaceRecord(name = "Evento A"))
        accountA.toggleFavorite()
        accountA.markExamplePaid()
        listOf(accountB, ownerA).forEach { workspace ->
            assertTrue(
                workspace.uiState.value.records
                    .isEmpty(),
            )
            assertNull(workspace.uiState.value.accountProfile)
            assertTrue(workspace.uiState.value.favorite)
            assertFalse(workspace.uiState.value.paid)
        }
    }

    @Test
    fun profileSeedIsIdempotentAndNeverOverwritesLocalEdits() {
        val workspace = WorkspaceViewModel()
        workspace.initializeProfile("Nombre de cuenta", "account@example.invalid")
        workspace.confirm(WorkspaceSection.PROFILE, WorkspaceRecord(name = "Alias local"))
        workspace.initializeProfile("Otro nombre", "other@example.invalid")
        assertEquals(
            "Nombre de cuenta",
            workspace.uiState.value.accountProfile
                ?.name,
        )
        assertEquals(
            "account@example.invalid",
            workspace.uiState.value.accountProfile
                ?.email,
        )
        assertEquals(
            "Alias local",
            workspace.uiState.value
                .latest(WorkspaceSection.PROFILE)
                ?.name,
        )
    }

    @Test
    fun clearingRemovesAllAccountDataAndAllowsNewInitialization() {
        val workspace = WorkspaceViewModel()
        workspace.initializeProfile("Primera", "first@example.invalid")
        workspace.confirm(WorkspaceSection.PAYMENTS, WorkspaceRecord(name = "Pago", amount = 1.0))
        workspace.markExamplePaid()
        workspace.toggleFavorite()
        workspace.clear()
        assertEquals(WorkspaceUiState(), workspace.uiState.value)
        workspace.initializeProfile("Segunda", "second@example.invalid")
        assertEquals(
            "Segunda",
            workspace.uiState.value.accountProfile
                ?.name,
        )
    }

    @Test
    fun boundariesRejectNonFiniteAndInvalidAmountsWithoutChangingState() {
        val workspace = WorkspaceViewModel()
        val invalid =
            listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.0, -1.0, OVER_LIMIT_AMOUNT)
        invalid.forEach { amount ->
            assertFalse(workspace.confirm(WorkspaceSection.PAYMENTS, WorkspaceRecord(name = "Pago", amount = amount)))
        }
        assertTrue(
            workspace.uiState.value.records
                .isEmpty(),
        )
        assertTrue(workspace.confirm(WorkspaceSection.PAYMENTS, WorkspaceRecord(name = "Pago", amount = 1_000_000.0)))
    }

    @Test
    fun inputAndCalendarLimitsAreEnforcedAtTheStateBoundary() {
        val workspace = WorkspaceViewModel()
        assertTrue(workspace.confirm(WorkspaceSection.PROFILE, WorkspaceRecord(name = "a".repeat(INPUT_LIMIT))))
        val snapshot = workspace.uiState.value
        assertFalse(workspace.confirm(WorkspaceSection.PROFILE, WorkspaceRecord(name = "a".repeat(INPUT_LIMIT + 1))))
        assertFalse(workspace.confirm(WorkspaceSection.EVENT, WorkspaceRecord(day = 0)))
        assertFalse(workspace.confirm(WorkspaceSection.EVENT, WorkspaceRecord(day = SHEET_MONTH_DAYS + 1)))
        assertFalse(workspace.confirm(WorkspaceSection.BLOCKS, WorkspaceRecord(day = 2, endDay = 1)))
        assertFalse(workspace.confirm(WorkspaceSection.EVENT, WorkspaceRecord(guests = SHEET_MAX_GUESTS + 1)))
        assertEquals(snapshot, workspace.uiState.value)
    }

    @Test
    fun editingPrimaryValuesReplacesThemWhileListsAccumulateAndSnapshotsStayUnchanged() {
        val workspace = WorkspaceViewModel()
        workspace.confirm(WorkspaceSection.EVENT, WorkspaceRecord(name = "Primero"))
        val previous = workspace.uiState.value
        workspace.confirm(WorkspaceSection.EVENT, WorkspaceRecord(name = "Segundo"))
        workspace.confirm(WorkspaceSection.GUESTS, WorkspaceRecord(name = "Invitado uno"))
        workspace.confirm(WorkspaceSection.GUESTS, WorkspaceRecord(name = "Invitado dos"))
        assertEquals("Primero", previous.latest(WorkspaceSection.EVENT)?.name)
        assertEquals(
            1,
            workspace.uiState.value.records[WorkspaceSection.EVENT]
                ?.size,
        )
        assertEquals(
            2,
            workspace.uiState.value.records[WorkspaceSection.GUESTS]
                ?.size,
        )
        assertNotNull(workspace.uiState.value.latest(WorkspaceSection.GUESTS))
    }
}

private const val INPUT_LIMIT = 500
private const val OVER_LIMIT_AMOUNT = 1_000_001.0
