package mx.virgo.core.domain

enum class RecoveryError {
    EXPIRED,
    WRONG_CODE,
    SHORT_PASSWORD,
    MISMATCH,
}
