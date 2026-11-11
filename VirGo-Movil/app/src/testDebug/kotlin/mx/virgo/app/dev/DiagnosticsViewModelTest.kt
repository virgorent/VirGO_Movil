package mx.virgo.app.dev

import app.cash.turbine.test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mx.virgo.app.BuildConfig
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.ApiHealth
import mx.virgo.core.domain.CheckApiHealthUseCase
import mx.virgo.core.testing.FakeHealthRepository
import mx.virgo.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import mx.virgo.core.network.BuildConfig as NetworkBuildConfig

class DiagnosticsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val fakeRepository = FakeHealthRepository()

    private fun viewModel() = DiagnosticsViewModel(CheckApiHealthUseCase(fakeRepository))

    @Test
    fun initShowsLoadingThenSuccess() =
        runTest {
            val viewModel = viewModel()
            viewModel.state.test {
                val loading = awaitItem()
                assertTrue(loading.loading)
                advanceUntilIdle()
                val success = awaitItem()
                assertFalse(success.loading)
                assertTrue(success.apiAvailable)
                assertEquals(12L, success.latencyMs)
                assertEquals(BuildConfig.VERSION_NAME, success.versionName)
                assertEquals(NetworkBuildConfig.API_BASE_URL, success.apiBaseUrl)
                expectNoEvents()
            }
        }

    @Test
    fun initShowsLoadingThenError() =
        runTest {
            fakeRepository.result = AppResult.Failure(AppError.Network)
            val viewModel = viewModel()
            viewModel.state.test {
                val loading = awaitItem()
                assertTrue(loading.loading)
                advanceUntilIdle()
                val failed = awaitItem()
                assertFalse(failed.loading)
                assertFalse(failed.apiAvailable)
                assertEquals(AppError.Network, failed.error)
                assertNull(failed.latencyMs)
                expectNoEvents()
            }
        }

    @Test
    fun retryAfterErrorRecovers() =
        runTest {
            fakeRepository.result = AppResult.Failure(AppError.Network)
            val viewModel = viewModel()
            viewModel.state.test {
                awaitItem()
                advanceUntilIdle()
                assertEquals(AppError.Network, awaitItem().error)
                fakeRepository.result = AppResult.Success(ApiHealth(status = "Healthy", latencyMs = 12))
                viewModel.refresh()
                assertTrue(awaitItem().loading)
                advanceUntilIdle()
                val recovered = awaitItem()
                assertTrue(recovered.apiAvailable)
                assertNull(recovered.error)
            }
        }
}
