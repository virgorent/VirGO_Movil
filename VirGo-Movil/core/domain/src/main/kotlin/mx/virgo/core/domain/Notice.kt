package mx.virgo.core.domain

data class Notice(
    val id: String,
    val type: String?,
    val title: String,
    val body: String?,
    val isRead: Boolean,
    val createdAt: String?,
)
