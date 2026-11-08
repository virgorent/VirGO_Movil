package mx.virgo.app.ui.recover

private const val MIN_PASSWORD_LENGTH = 8
private const val CODE_VALID_MILLIS = CODE_MINUTES * 60L * 1000L

internal data class RecoverErrors(
    val expired: String,
    val wrongCode: String,
    val shortPassword: String,
    val mismatch: String,
)

internal fun recoverError(
    expectedCode: String,
    enteredCode: String,
    sentAt: Long,
    password: String,
    confirm: String,
    messages: RecoverErrors,
): String =
    when {
        System.currentTimeMillis() - sentAt > CODE_VALID_MILLIS -> messages.expired
        enteredCode != expectedCode -> messages.wrongCode
        password.length < MIN_PASSWORD_LENGTH -> messages.shortPassword
        password != confirm -> messages.mismatch
        else -> ""
    }
