package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.network.createFavoritesApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoritesRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: FavoritesRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = FavoritesRepositoryImpl(createFavoritesApi(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun favoritesResponseMapsFavoriteFields() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody(FAVORITES_BODY))

            val result = repository.getFavorites()

            val favorites = (result as AppResult.Success).value
            assertEquals(1, favorites.size)
            val favorite = favorites.first()
            assertEquals("venue", favorite.targetType)
            assertEquals("uuid-1", favorite.targetId)
            assertEquals("Casa VirGo", favorite.name)
            assertEquals("/api/v1/venues.png", favorite.photoUrl)
            assertEquals(4.5, favorite.rating!!, 0.0)
            assertEquals("/api/v1/favorites", server.takeRequest().path)
        }

    @Test
    fun addFavoriteSendsTheExpectedBody() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(201))

            val result =
                repository.addFavorite(
                    targetType = "venue",
                    targetId = "uuid-1",
                    name = "Casa VirGo",
                    photoUrl = null,
                    rating = 4.5,
                )

            assertTrue(result is AppResult.Success)
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/v1/favorites", request.path)
            val body = request.body.readUtf8()
            assertTrue(body.contains("\"targetType\":\"venue\""))
            assertTrue(body.contains("\"targetId\":\"uuid-1\""))
            assertTrue(body.contains("\"name\":\"Casa VirGo\""))
            assertTrue(body.contains("\"rating\":4.5"))
        }

    @Test
    fun removeFavoriteUsesDeleteWithTheIndexPath() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(204))

            val result = repository.removeFavorite(targetType = "venue", targetId = "uuid-1")

            assertTrue(result is AppResult.Success)
            val request = server.takeRequest()
            assertEquals("DELETE", request.method)
            assertEquals("/api/v1/favorites/venue/uuid-1", request.path)
        }

    @Test
    fun notFoundResponseMapsToNotFound() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404).setBody("""{"title":"Not Found","status":404}"""))

            val result = repository.getFavorites()

            assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
        }

    @Test
    fun addFavoriteConflictMapsToConflict() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(409).setBody("""{"title":"Conflict","status":409}"""))

            val result = repository.addFavorite("venue", "uuid-1", null, null, null)

            assertEquals(AppError.Conflict, (result as AppResult.Failure).error)
        }

    @Test
    fun noConnectionMapsToNetworkError() =
        runTest {
            server.shutdown()

            val result = repository.getFavorites()

            assertEquals(AppError.Network, (result as AppResult.Failure).error)
        }
}

private val FAVORITES_BODY =
    """
    {"data":[{"targetType":"venue","targetId":"uuid-1","name":"Casa VirGo",
    "photoUrl":"/api/v1/venues.png","rating":4.5}]}
    """.trimIndent()
