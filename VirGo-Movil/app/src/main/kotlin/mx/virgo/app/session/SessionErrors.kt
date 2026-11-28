package mx.virgo.app.session

import mx.virgo.core.common.AppError

/** Un 401 cierra la sesión (`030`); el resto lo maneja cada pantalla. */
suspend fun SessionRepository.endsSession(error: AppError): Boolean {
    if (error != AppError.Unauthorized) return false
    invalidate(SessionEndReason.REVOKED)
    return true
}
