package mx.virgo.core.datastore

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

internal object SessionCodec {
    private const val VERSION = 1

    fun encode(session: StoredSession): ByteArray =
        ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeInt(VERSION)
                output.writeUTF(session.accessToken)
                output.writeUTF(session.account.accountId)
                output.writeUTF(session.account.name)
                output.writeUTF(session.account.email)
                output.writeUTF(session.account.role)
                output.writeLong(session.issuedAtMillis)
                output.writeLong(session.expiresAtMillis)
            }
            bytes.toByteArray()
        }

    fun decode(bytes: ByteArray): StoredSession =
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            if (input.readInt() != VERSION) throw IOException("Invalid session version")
            val session =
                StoredSession(
                    accessToken = input.readUTF(),
                    account = StoredAccount(input.readUTF(), input.readUTF(), input.readUTF(), input.readUTF()),
                    issuedAtMillis = input.readLong(),
                    expiresAtMillis = input.readLong(),
                )
            if (input.available() != 0) throw IOException("Invalid session data")
            session
        }
}
