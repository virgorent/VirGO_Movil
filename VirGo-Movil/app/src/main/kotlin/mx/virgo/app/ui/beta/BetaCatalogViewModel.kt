package mx.virgo.app.ui.beta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetVenuesUseCase
import mx.virgo.core.domain.Venue
import javax.inject.Inject

data class BetaCatalogState(
    val loading: Boolean = true,
    val failed: Boolean = false,
    val venues: List<Venue> = emptyList(),
)

@HiltViewModel
class BetaCatalogViewModel
    @Inject
    constructor(
        private val getVenues: GetVenuesUseCase,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(BetaCatalogState())
        val state = mutableState.asStateFlow()
        private var refreshStarted = false

        init {
            refresh()
        }

        fun refresh() {
            if (mutableState.value.loading && refreshStarted) return
            refreshStarted = true
            mutableState.value = BetaCatalogState()
            viewModelScope.launch {
                mutableState.value =
                    when (val result = getVenues()) {
                        is AppResult.Success -> BetaCatalogState(loading = false, venues = result.value)
                        is AppResult.Failure -> BetaCatalogState(loading = false, failed = true)
                    }
            }
        }
    }
