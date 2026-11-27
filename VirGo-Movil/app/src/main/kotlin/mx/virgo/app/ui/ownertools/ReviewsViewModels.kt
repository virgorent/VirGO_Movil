package mx.virgo.app.ui.ownertools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.session.endsSession
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetMyVenuesUseCase
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.ReviewRules
import mx.virgo.core.domain.ReviewsRepository
import mx.virgo.core.domain.VenueReviews
import mx.virgo.core.domain.VenueStatus
import javax.inject.Inject

enum class ReviewsStatus {
    LOADING,
    READY,
    UNAVAILABLE,
    FAILED,
}

data class OwnerReviewsUiState(
    val venues: List<OwnerVenue> = emptyList(),
    val venueId: String? = null,
    val status: ReviewsStatus = ReviewsStatus.LOADING,
    val reviews: VenueReviews? = null,
    /** Reseña que se está respondiendo y su texto. */
    val replying: Pair<String, String>? = null,
    val sending: Boolean = false,
    val replyFailed: Boolean = false,
)

/** «Reseñas» del propietario con respuesta (`025` RF-05). */
@HiltViewModel
class OwnerReviewsViewModel
    @Inject
    constructor(
        private val getVenues: GetMyVenuesUseCase,
        private val repository: ReviewsRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(OwnerReviewsUiState())
        val state: StateFlow<OwnerReviewsUiState> = ui.asStateFlow()

        init {
            viewModelScope.launch {
                when (val result = getVenues()) {
                    is AppResult.Success -> {
                        val published = result.value.filter { it.status == VenueStatus.PUBLISHED }
                        ui.value = ui.value.copy(venues = published)
                        published.firstOrNull()?.let { select(it.id) }
                            ?: run { ui.value = ui.value.copy(status = ReviewsStatus.READY) }
                    }
                    is AppResult.Failure -> fail(result.error)
                }
            }
        }

        fun select(venueId: String) {
            ui.value = ui.value.copy(venueId = venueId, status = ReviewsStatus.LOADING, reviews = null, replying = null)
            viewModelScope.launch {
                when (val result = repository.venueReviews(venueId)) {
                    is AppResult.Success ->
                        if (ui.value.venueId ==
                            venueId
                        ) {
                            ui.value = ui.value.copy(status = ReviewsStatus.READY, reviews = result.value)
                        }
                    is AppResult.Failure -> fail(result.error)
                }
            }
        }

        /** `null` cierra el formulario de respuesta. */
        fun editReply(reply: Pair<String, String>?) {
            ui.value =
                ui.value.copy(
                    replying = reply?.let { it.first to it.second.take(ReviewRules.MAX_TEXT) },
                    replyFailed = false,
                )
        }

        fun sendReply() {
            val current = ui.value
            val (reviewId, text) = current.replying ?: return
            if (current.sending || text.isBlank()) return
            ui.value = current.copy(sending = true, replyFailed = false)
            viewModelScope.launch {
                when (val result = repository.reply(reviewId, text)) {
                    is AppResult.Success -> {
                        val reviews = ui.value.reviews
                        val updated =
                            reviews?.copy(
                                reviews =
                                    reviews.reviews.map {
                                        if (it.id ==
                                            reviewId
                                        ) {
                                            result.value
                                        } else {
                                            it
                                        }
                                    },
                            )
                        ui.value = ui.value.copy(sending = false, replying = null, reviews = updated)
                    }
                    is AppResult.Failure ->
                        if (!session.endsSession(result.error)) {
                            ui.value =
                                ui.value.copy(sending = false, replyFailed = true)
                        }
                }
            }
        }

        private suspend fun fail(error: AppError) {
            if (session.endsSession(error)) return
            ui.value =
                ui.value.copy(
                    status =
                        if (error ==
                            AppError.NotFound
                        ) {
                            ReviewsStatus.UNAVAILABLE
                        } else {
                            ReviewsStatus.FAILED
                        },
                )
        }
    }

/** Resultado de publicar una reseña del cliente (`019` RF-03 y RF-04). */
enum class ReviewOutcome {
    PUBLISHED,
    ALREADY,
    NOT_ALLOWED,
    UNAVAILABLE,
    FAILED,
}

data class ReviewFormState(
    val open: Boolean = false,
    val stars: Int = 0,
    val text: String = "",
    val sending: Boolean = false,
    val outcome: ReviewOutcome? = null,
) {
    val canPublish: Boolean get() = !sending && ReviewRules.valid(stars, text)
}

/** «Calificar el salón» desde el comprobante (`019`). */
@HiltViewModel
class ReviewFormViewModel
    @Inject
    constructor(
        private val repository: ReviewsRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(ReviewFormState())
        val state: StateFlow<ReviewFormState> = ui.asStateFlow()

        fun edit(form: ReviewFormState) {
            ui.value =
                form.copy(text = form.text.take(ReviewRules.MAX_TEXT), outcome = null, sending = ui.value.sending)
        }

        fun publish(
            venueId: String,
            bookingId: String,
        ) {
            val current = ui.value
            if (!current.canPublish) return
            ui.value = current.copy(sending = true, outcome = null)
            viewModelScope.launch {
                val outcome =
                    when (val result = repository.create(venueId, bookingId, current.stars, current.text)) {
                        is AppResult.Success -> ReviewOutcome.PUBLISHED
                        is AppResult.Failure ->
                            when {
                                session.endsSession(result.error) -> return@launch
                                result.error == AppError.Conflict -> ReviewOutcome.ALREADY
                                result.error == AppError.Forbidden -> ReviewOutcome.NOT_ALLOWED
                                result.error == AppError.NotFound -> ReviewOutcome.UNAVAILABLE
                                else -> ReviewOutcome.FAILED
                            }
                    }
                ui.value = ui.value.copy(sending = false, outcome = outcome, open = outcome == ReviewOutcome.FAILED)
            }
        }
    }
