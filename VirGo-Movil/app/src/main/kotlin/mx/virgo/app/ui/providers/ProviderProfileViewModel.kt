package mx.virgo.app.ui.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Provider
import mx.virgo.core.domain.ProviderDraft
import mx.virgo.core.domain.ProviderField
import mx.virgo.core.domain.ProviderPackage
import mx.virgo.core.domain.ProviderValidator
import mx.virgo.core.domain.ProvidersRepository
import mx.virgo.core.domain.toDraft
import javax.inject.Inject

/** Callbacks del perfil del proveedor (`026`), agrupados para probar el contenido sin `ViewModel`. */
class ProviderProfileActions(
    val onDraft: (ProviderDraft) -> Unit = {},
    val onAddPackage: () -> Unit = {},
    val onRemovePackage: (Int) -> Unit = {},
    val onPackage: (Int, ProviderPackage) -> Unit = { _, _ -> },
    /** `true` publica (RF-03); `false` solo guarda. */
    val onSubmit: (publish: Boolean) -> Unit = {},
    val onRetry: () -> Unit = {},
)

/** Agrega o quita una zona de servicio. */
internal fun ProviderDraft.toggleZone(zone: String): ProviderDraft =
    copy(
        zones =
            if (zone in
                zones
            ) {
                zones - zone
            } else {
                zones + zone
            },
    )

/** Resultado del último guardado o publicación (`026`). */
enum class ProfileNotice {
    SAVED,
    PUBLISHED,
    UNAVAILABLE,
    FAILED,
}

data class ProviderProfileUiState(
    val load: Load = Load.LOADING,
    val provider: Provider? = null,
    val draft: ProviderDraft = ProviderDraft(),
    val missing: Set<ProviderField> = emptySet(),
    val saving: Boolean = false,
    val notice: ProfileNotice? = null,
) {
    val published: Boolean get() = provider?.published == true
}

/** «Mi perfil» del proveedor (`026` RF-01–RF-04). */
@HiltViewModel
class ProviderProfileViewModel
    @Inject
    constructor(
        private val repository: ProvidersRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(ProviderProfileUiState())
        val state: StateFlow<ProviderProfileUiState> = ui.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            ui.value = ui.value.copy(load = Load.LOADING)
            viewModelScope.launch {
                when (val result = repository.mine()) {
                    is AppResult.Success ->
                        ui.value =
                            ui.value.copy(
                                load = Load.READY,
                                provider = result.value,
                                draft = result.value?.toDraft() ?: ProviderDraft(),
                            )
                    is AppResult.Failure -> session.loadFor(result.error)?.let { ui.value = ui.value.copy(load = it) }
                }
            }
        }

        fun edit(draft: ProviderDraft) {
            val missing = ui.value.missing.intersect(ProviderValidator.missing(draft))
            ui.value = ui.value.copy(draft = draft, missing = missing, notice = null)
        }

        fun addPackage() = edit(ui.value.draft.let { it.copy(packages = it.packages + ProviderPackage()) })

        fun removePackage(index: Int) =
            edit(ui.value.draft.let { it.copy(packages = it.packages.filterIndexed { i, _ -> i != index }) })

        fun editPackage(
            index: Int,
            item: ProviderPackage,
        ) = edit(
            ui.value.draft.let {
                it.copy(
                    packages =
                        it.packages.mapIndexed { i, old ->
                            if (i ==
                                index
                            ) {
                                item
                            } else {
                                old
                            }
                        },
                )
            },
        )

        /** Guarda sin publicar: crea el perfil la primera vez (`POST`) y luego edita (`PATCH`). */
        fun save() = submit(publish = false)

        /** Valida todo (RF-02), guarda y publica (RF-03). */
        fun publish() {
            val missing = ProviderValidator.missing(ui.value.draft)
            if (missing.isNotEmpty()) {
                ui.value = ui.value.copy(missing = missing, notice = null)
                return
            }
            submit(publish = true)
        }

        private fun submit(publish: Boolean) {
            val current = ui.value
            if (current.saving) return
            ui.value = current.copy(saving = true, notice = null)
            viewModelScope.launch {
                val saved =
                    if (current.provider == null) repository.create(current.draft) else repository.update(current.draft)
                val result = if (publish && saved is AppResult.Success) repository.publish() else saved
                when (result) {
                    is AppResult.Success ->
                        ui.value =
                            ui.value.copy(
                                saving = false,
                                provider = result.value,
                                notice = if (publish) ProfileNotice.PUBLISHED else ProfileNotice.SAVED,
                            )
                    is AppResult.Failure -> {
                        val load = session.loadFor(result.error) ?: return@launch
                        val notice = if (load == Load.UNAVAILABLE) ProfileNotice.UNAVAILABLE else ProfileNotice.FAILED
                        ui.value = ui.value.copy(saving = false, notice = notice)
                    }
                }
            }
        }
    }
