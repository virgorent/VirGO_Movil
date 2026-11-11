package mx.virgo.app.dev

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.virgo.app.BuildConfig
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.CheckApiHealthUseCase
import javax.inject.Inject
import mx.virgo.core.network.BuildConfig as NetworkBuildConfig

data class DiagnosticsUiState(
    val versionName: String = "",
    val apiBaseUrl: String = "",
    val loading: Boolean = false,
    val apiAvailable: Boolean = false,
    val latencyMs: Long? = null,
    val error: AppError? = null,
)

@HiltViewModel
class DiagnosticsViewModel
    @Inject
    constructor(
        private val checkHealth: CheckApiHealthUseCase,
    ) : ViewModel() {
        private val _state =
            MutableStateFlow(
                DiagnosticsUiState(
                    versionName = BuildConfig.VERSION_NAME,
                    apiBaseUrl = NetworkBuildConfig.API_BASE_URL,
                ),
            )
        val state: StateFlow<DiagnosticsUiState> = _state.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            _state.update { it.copy(loading = true, error = null, apiAvailable = false, latencyMs = null) }
            viewModelScope.launch {
                when (val result = checkHealth()) {
                    is AppResult.Success ->
                        _state.update {
                            it.copy(loading = false, apiAvailable = true, latencyMs = result.value.latencyMs)
                        }
                    is AppResult.Failure ->
                        _state.update { it.copy(loading = false, apiAvailable = false, error = result.error) }
                }
            }
        }
    }
