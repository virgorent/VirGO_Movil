package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OwnerVenueUseCasesTest {
    private class RecordingRepository : OwnerVenuesRepository {
        val calls = mutableListOf<String>()
        var publishResult: AppResult<VenueStatus> = AppResult.Success(VenueStatus.PUBLISHED)

        override suspend fun mine(): AppResult<List<OwnerVenue>> {
            calls += "mine"
            return AppResult.Success(emptyList())
        }

        override suspend fun create(draft: VenueDraft): AppResult<OwnerVenue> {
            calls += "create"
            return AppResult.Success(OwnerVenue("new", draft, VenueStatus.DRAFT))
        }

        override suspend fun update(
            id: String,
            draft: VenueDraft,
            includeServices: Boolean,
        ): AppResult<OwnerVenue> {
            calls += "update:$id:$includeServices"
            return AppResult.Success(OwnerVenue(id, draft, VenueStatus.DRAFT))
        }

        override suspend fun publish(id: String): AppResult<VenueStatus> {
            calls += "publish:$id"
            return publishResult
        }
    }

    private val repository = RecordingRepository()

    @Test
    fun saveCreatesOnlyWithoutId() =
        runTest {
            val created = SaveVenueDraftUseCase(repository)(null, VenueDraft())
            assertEquals("new", (created as AppResult.Success).value.id)
            assertEquals(listOf("create"), repository.calls)
        }

    @Test
    fun saveUpdatesWhenIdExists() =
        runTest {
            SaveVenueDraftUseCase(repository)("v1", VenueDraft(), includeServices = false)
            assertEquals(listOf("update:v1:false"), repository.calls)
        }

    @Test
    fun publishDelegatesAndPropagatesFailure() =
        runTest {
            val publish = PublishVenueUseCase(repository)
            assertEquals(AppResult.Success(VenueStatus.PUBLISHED), publish("v1"))
            repository.publishResult = AppResult.Failure(AppError.Forbidden)
            assertTrue((publish("v1") as AppResult.Failure).error is AppError.Forbidden)
            assertEquals(listOf("publish:v1", "publish:v1"), repository.calls)
        }

    @Test
    fun getMyVenuesReadsMine() =
        runTest {
            assertEquals(AppResult.Success(emptyList<OwnerVenue>()), GetMyVenuesUseCase(repository)())
            assertEquals(listOf("mine"), repository.calls)
        }
}
