package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

/** Categorías del catálogo de proveedores (`011` RF-01); `wire` es el valor del esquema `providers`. */
enum class ProviderCategory(
    val wire: String,
) {
    CATERING("Catering"),
    MUSIC("Música"),
    PHOTO("Fotografía"),
    DECOR("Decoración"),
    FURNITURE("Mobiliario"),
    ;

    companion object {
        fun fromWire(value: String?): ProviderCategory? = entries.firstOrNull { it.wire == value }
    }
}

/** Cobro por persona o por evento (`026` RF-01). */
enum class ProviderPricing(
    val wire: String,
) {
    PER_PERSON("per_person"),
    PER_EVENT("per_event"),
    ;

    companion object {
        fun fromWire(value: String?): ProviderPricing? = entries.firstOrNull { it.wire == value }
    }
}

data class ProviderPackage(
    val id: String? = null,
    val name: String = "",
    val price: BigDecimal? = null,
    val includes: String = "",
)

/** Perfil público de un proveedor (esquema `providers`). */
data class Provider(
    val id: String,
    val businessName: String,
    val category: ProviderCategory?,
    val description: String,
    val pricing: ProviderPricing?,
    val responseTime: String,
    val packages: List<ProviderPackage>,
    val zones: List<String>,
    val coverUrl: String?,
    val gallery: List<String>,
    val published: Boolean,
    val ratingAverage: Double,
    val ratingCount: Int,
) {
    /** Precio del paquete más barato; `null` si no hay paquetes con precio. */
    val priceFrom: BigDecimal? get() = packages.mapNotNull { it.price }.minOrNull()
}

/** Lo que el proveedor captura en su perfil (`026` RF-01). */
data class ProviderDraft(
    val businessName: String = "",
    val category: ProviderCategory? = null,
    val description: String = "",
    val pricing: ProviderPricing? = null,
    val responseTime: String = "",
    val packages: List<ProviderPackage> = emptyList(),
    val zones: Set<String> = emptySet(),
)

enum class ProviderField {
    BUSINESS_NAME,
    CATEGORY,
    DESCRIPTION,
    PRICING,
    RESPONSE_TIME,
    PACKAGES,
    ZONES,
}

/** Qué falta para publicar (`026` RF-02). */
object ProviderValidator {
    const val MIN_DESCRIPTION = 20

    fun missing(draft: ProviderDraft): Set<ProviderField> =
        buildSet {
            if (draft.businessName.isBlank()) add(ProviderField.BUSINESS_NAME)
            if (draft.category == null) add(ProviderField.CATEGORY)
            if (draft.description.trim().length < MIN_DESCRIPTION) add(ProviderField.DESCRIPTION)
            if (draft.pricing == null) add(ProviderField.PRICING)
            if (draft.responseTime.isBlank()) add(ProviderField.RESPONSE_TIME)
            val validPackages =
                draft.packages.filter {
                    it.name.isNotBlank() &&
                        (it.price ?: BigDecimal.ZERO) > BigDecimal.ZERO
                }
            if (validPackages.isEmpty() || validPackages.size != draft.packages.size) add(ProviderField.PACKAGES)
            if (draft.zones.isEmpty()) add(ProviderField.ZONES)
        }
}

fun Provider.toDraft(): ProviderDraft =
    ProviderDraft(businessName, category, description, pricing, responseTime, packages, zones.toSet())

/** Servicio en la agenda del proveedor (esquema `gigs`). */
data class Gig(
    val id: String? = null,
    val eventName: String = "",
    val clientName: String = "",
    val place: String = "",
    val date: LocalDate? = null,
    val start: LocalTime? = null,
    val end: LocalTime? = null,
    val amount: BigDecimal? = null,
    val quoteId: String? = null,
)

object GigSchedule {
    /** Ids de los servicios que se cruzan con otro el mismo día (`027` RF-04); el fin es exclusivo. */
    fun overlaps(gigs: List<Gig>): Map<Gig, List<Gig>> {
        val timed = gigs.filter { it.date != null && it.start != null && it.end != null && it.start < it.end }
        return timed
            .associateWith { gig ->
                timed.filter { other ->
                    other !== gig && other.date == gig.date && other.start!! < gig.end!! && gig.start!! < other.end!!
                }
            }.filterValues { it.isNotEmpty() }
    }

    /** Lunes de la semana de [date]. */
    fun weekStart(date: LocalDate): LocalDate = date.minusDays((date.dayOfWeek.value - 1).toLong())

    fun inWeek(
        gigs: List<Gig>,
        monday: LocalDate,
    ): List<Gig> =
        gigs
            .filter { gig -> gig.date?.let { !it.isBefore(monday) && it.isBefore(monday.plusDays(WEEK_DAYS)) } == true }
            .sortedWith(compareBy({ it.date }, { it.start }))

    fun upcoming(
        gigs: List<Gig>,
        today: LocalDate,
        limit: Int = UPCOMING,
    ): List<Gig> =
        gigs
            .filter { gig -> gig.date?.let { !it.isBefore(today) } == true }
            .sortedWith(compareBy({ it.date }, { it.start }))
            .take(limit)

    private const val WEEK_DAYS = 7L
    private const val UPCOMING = 3
}

/** Tablero del proveedor (`027` RF-01), calculado con sus cotizaciones y su agenda. */
data class ProviderDashboard(
    val newRequests: Int,
    val answered: Int,
    val accepted: Int,
    val upcoming: List<Gig>,
) {
    companion object {
        fun from(
            quotes: List<Quote>,
            gigs: List<Gig>,
            today: LocalDate,
        ): ProviderDashboard =
            ProviderDashboard(
                newRequests = quotes.count { it.status == QuoteStatus.SENT },
                answered = quotes.count { it.status == QuoteStatus.ANSWERED },
                accepted = quotes.count { it.status == QuoteStatus.ACCEPTED },
                upcoming = GigSchedule.upcoming(gigs, today),
            )
    }
}

/** Un concepto de la respuesta a una solicitud (`028` RF-02). */
data class QuoteConcept(
    val name: String = "",
    val quantity: Int = 1,
    val unitPrice: BigDecimal = BigDecimal.ZERO,
) {
    val subtotal: BigDecimal get() = unitPrice.multiply(BigDecimal(quantity))
}

object QuoteConcepts {
    fun fromPackages(packages: List<ProviderPackage>): List<QuoteConcept> =
        packages.map { QuoteConcept(it.name, 1, it.price ?: BigDecimal.ZERO) }

    fun total(concepts: List<QuoteConcept>): BigDecimal =
        concepts.fold(BigDecimal.ZERO) { sum, concept -> sum + concept.subtotal }

    /** Resumen que acompaña la nota, para que el cliente vea el desglose. */
    fun summary(concepts: List<QuoteConcept>): String =
        concepts.filter { it.name.isNotBlank() }.joinToString("\n") { "${it.quantity} × ${it.name}" }
}

/** Mes de las estadísticas del proveedor (`029` RF-02). */
data class ProviderMonthStats(
    val month: java.time.YearMonth,
    val received: Int,
    val accepted: Int,
) {
    /** Porcentaje entero; 0 sin solicitudes. */
    val conversion: Int get() = if (received == 0) 0 else accepted * PERCENT / received

    companion object {
        private const val PERCENT = 100

        fun from(
            quotes: List<Quote>,
            months: Int,
            today: LocalDate,
        ): List<ProviderMonthStats> {
            val current = java.time.YearMonth.from(today)
            return (months - 1 downTo 0).map { back ->
                val month = current.minusMonths(back.toLong())
                val inMonth = quotes.filter { quote -> quote.createdMonth() == month }
                ProviderMonthStats(month, inMonth.size, inMonth.count { it.status == QuoteStatus.ACCEPTED })
            }
        }

        private fun Quote.createdMonth(): java.time.YearMonth? =
            createdAt
                ?.take(
                    DATE_LENGTH,
                )?.let { runCatching { java.time.YearMonth.from(LocalDate.parse(it)) }.getOrNull() }

        private const val DATE_LENGTH = 10
    }
}

interface ProvidersRepository {
    suspend fun catalog(category: ProviderCategory? = null): AppResult<List<Provider>>

    suspend fun provider(id: String): AppResult<Provider>

    suspend fun reviews(providerId: String): AppResult<VenueReviews>

    /** `null` si el proveedor aún no tiene perfil (`404` de `GET /providers/me`). */
    suspend fun mine(): AppResult<Provider?>

    suspend fun create(draft: ProviderDraft): AppResult<Provider>

    suspend fun update(draft: ProviderDraft): AppResult<Provider>

    suspend fun publish(): AppResult<Provider>

    suspend fun gigs(): AppResult<List<Gig>>

    suspend fun saveGig(gig: Gig): AppResult<Gig>
}
