package mx.virgo.app.demo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDestinationsTest {
    @Test
    fun inventoryContains55UniqueAuthenticatedDestinations() {
        val all = DemoDestinations.shared + DemoDestinations.client + DemoDestinations.owner + DemoDestinations.provider
        assertEquals(EXPECTED_TOTAL_DESTINATIONS, all.size)
        assertEquals(all.size, all.map { it.route }.toSet().size)
        assertEquals(EXPECTED_CLIENT_DESTINATIONS, DemoDestinations.client.size)
        assertEquals(EXPECTED_OWNER_DESTINATIONS, DemoDestinations.owner.size)
        assertEquals(EXPECTED_PROVIDER_DESTINATIONS, DemoDestinations.provider.size)
    }

    @Test
    fun everyRoleHasItsHomeTabsAndSharedDestinations() {
        DemoRole.entries.forEach { role ->
            assertTrue(DemoDestinations.allows(role, role.home))
            role.tabs.forEach { assertTrue(DemoDestinations.allows(role, it)) }
            DemoDestinations.shared.forEach { assertTrue(DemoDestinations.allows(role, it.route)) }
        }
    }

    @Test
    fun crossRoleDestinationsAndUnknownRoutesAreDenied() {
        assertFalse(DemoDestinations.allows(DemoRole.CLIENT, "ownerHome"))
        assertFalse(DemoDestinations.allows(DemoRole.OWNER, "providerProfile"))
        assertFalse(DemoDestinations.allows(DemoRole.PROVIDER, "payment"))
        DemoRole.entries.forEach {
            assertFalse(DemoDestinations.allows(it, "unknown"))
            assertFalse(DemoDestinations.allows(it, "login"))
        }
    }

    @Test
    fun historyPreservesBackOrderAndDoesNotDuplicateCurrentDestination() {
        val history = pushDemoRoute("explore", "venue")
        assertEquals("explore|venue", history)
        assertEquals(history, pushDemoRoute(history, "venue"))
        val paymentHistory = pushDemoRoute(history, "payment")
        assertEquals(history, paymentHistory.substringBeforeLast("|"))
    }

    @Test
    fun historyIsBoundedForSavedInstanceState() {
        var history = "explore"
        repeat(HISTORY_STRESS_COUNT) { history = pushDemoRoute(history, "destination$it") }
        assertEquals(EXPECTED_HISTORY_LIMIT, history.split("|").size)
        assertEquals("destination99", history.substringAfterLast("|"))
    }

    @Test
    fun fifteenSheetsAreUniqueAndOnlyAttachToAllowedRoleDestinations() {
        assertEquals(EXPECTED_SHEETS, demoSheetDefinitions.size)
        assertEquals(EXPECTED_SHEETS, demoSheetDefinitions.map { it.id }.toSet().size)
        demoSheetDefinitions.forEach { sheet ->
            sheet.roles.forEach { role ->
                assertTrue(DemoDestinations.allows(role, sheet.route))
                assertTrue(sheet in demoSheetsFor(role, sheet.route))
            }
        }
        assertTrue(demoSheetsFor(DemoRole.CLIENT, "ownerCalendar").isEmpty())
        assertTrue(demoSheetsFor(DemoRole.OWNER, "providerAgenda").isEmpty())
    }
}

private const val EXPECTED_TOTAL_DESTINATIONS = 55
private const val EXPECTED_CLIENT_DESTINATIONS = 29
private const val EXPECTED_OWNER_DESTINATIONS = 12
private const val EXPECTED_PROVIDER_DESTINATIONS = 9
private const val HISTORY_STRESS_COUNT = 100
private const val EXPECTED_HISTORY_LIMIT = 50
private const val EXPECTED_SHEETS = 15
