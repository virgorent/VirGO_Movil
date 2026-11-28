package mx.virgo.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

class ProviderRulesTest {
    private val tuesday = LocalDate.parse("2027-05-11")

    private fun gig(
        name: String,
        start: String,
        end: String,
        date: LocalDate = tuesday,
    ) = Gig(id = name, eventName = name, date = date, start = LocalTime.parse(start), end = LocalTime.parse(end))

    @Test
    fun gigsThatCrossTheSameDayOverlapButTouchingOnesDoNot() {
        val boda = gig("Boda", "18:00", "22:00")
        val xv = gig("XV", "21:00", "23:00")
        val cena = gig("Cena", "22:00", "23:30")
        val otroDia = gig("Otro día", "18:00", "22:00", tuesday.plusDays(1))

        val overlaps = GigSchedule.overlaps(listOf(boda, xv, cena, otroDia))

        assertEquals(listOf(xv), overlaps[boda])
        assertEquals(listOf(boda, cena), overlaps[xv])
        assertEquals(listOf(xv), overlaps[cena])
        assertTrue(otroDia !in overlaps)
    }

    @Test
    fun weekStartsOnMondayAndKeepsOnlyItsGigs() {
        val monday = GigSchedule.weekStart(tuesday)
        assertEquals(LocalDate.parse("2027-05-10"), monday)
        val next = gig("Siguiente", "10:00", "12:00", monday.plusDays(7))
        assertEquals(
            listOf("Boda"),
            GigSchedule.inWeek(listOf(next, gig("Boda", "18:00", "22:00")), monday).map {
                it.eventName
            },
        )
    }

    @Test
    fun publishingNeedsEveryFieldAndAPricedPackage() {
        val complete =
            ProviderDraft(
                businessName = "Banquetes Lupita",
                category = ProviderCategory.CATERING,
                description = "Menús para bodas y eventos de empresa.",
                pricing = ProviderPricing.PER_PERSON,
                responseTime = "Menos de 2 h",
                packages = listOf(ProviderPackage(name = "Básico", price = BigDecimal("350"))),
                zones = setOf("Centro"),
            )
        assertEquals(emptySet<ProviderField>(), ProviderValidator.missing(complete))
        assertEquals(setOf(ProviderField.PACKAGES), ProviderValidator.missing(complete.copy(packages = emptyList())))
        assertEquals(
            setOf(ProviderField.PACKAGES),
            ProviderValidator.missing(
                complete.copy(packages = complete.packages + ProviderPackage(name = "Sin precio")),
            ),
        )
        assertEquals(setOf(ProviderField.DESCRIPTION), ProviderValidator.missing(complete.copy(description = "Corta")))
    }

    @Test
    fun conceptsStartFromPackagesAndRecalculateTheTotal() {
        val concepts =
            QuoteConcepts.fromPackages(
                listOf(ProviderPackage(name = "Paquete", price = BigDecimal("12000"))),
            )
        assertEquals(BigDecimal("12000"), QuoteConcepts.total(concepts))
        val changed = concepts + QuoteConcept("Mesero extra", 2, BigDecimal("800"))
        assertEquals(BigDecimal("13600"), QuoteConcepts.total(changed))
        assertEquals("1 × Paquete\n2 × Mesero extra", QuoteConcepts.summary(changed))
    }

    @Test
    fun monthlyStatsCountReceivedAndAcceptedWithConversion() {
        fun quote(
            status: QuoteStatus,
            created: String,
        ) = Quote("q-$created-$status", null, "", null, null, 0, null, status, null, null, null, created)
        val today = LocalDate.parse("2027-05-20")
        val quotes =
            listOf(
                quote(QuoteStatus.ACCEPTED, "2027-05-02T10:00:00Z"),
                quote(QuoteStatus.SENT, "2027-05-03T10:00:00Z"),
                quote(QuoteStatus.ANSWERED, "2027-05-04T10:00:00Z"),
                quote(QuoteStatus.REJECTED, "2027-05-05T10:00:00Z"),
                quote(QuoteStatus.ACCEPTED, "2027-03-01T10:00:00Z"),
            )

        val stats = ProviderMonthStats.from(quotes, 3, today)

        assertEquals(
            listOf(YearMonth.of(2027, 3), YearMonth.of(2027, 4), YearMonth.of(2027, 5)),
            stats.map { it.month },
        )
        assertEquals(4, stats.last().received)
        assertEquals(25, stats.last().conversion)
        assertEquals(0, stats[1].conversion)
    }
}
