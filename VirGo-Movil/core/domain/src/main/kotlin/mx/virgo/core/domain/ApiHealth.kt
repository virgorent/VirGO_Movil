package mx.virgo.core.domain

data class ApiHealth(
    val status: String,
    val latencyMs: Long,
)
