package mx.virgo.app.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel
    @Inject
    constructor(
        private val repository: SessionRepository,
    ) : ViewModel() {
        val state: StateFlow<SessionUiState> = repository.state

        init {
            refresh()
            viewModelScope.launch {
                repository.expiration.collectLatest { expiresAt ->
                    if (expiresAt > 0) {
                        delay(repository.remainingMillis())
                        viewModelScope.launch { repository.refresh() }
                    }
                }
            }
        }

        fun refresh() {
            viewModelScope.launch { repository.refresh() }
        }

        fun logout() {
            viewModelScope.launch { repository.logout() }
        }
    }
