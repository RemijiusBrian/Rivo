package dev.ridill.rivo.core.domain.crypto

import android.security.keystore.KeyProperties

interface CryptoManager {
    fun encrypt(rawData: ByteArray, password: String): EncryptionResult
    fun decrypt(encryptedData: ByteArray, iv: ByteArray, password: String): ByteArray
    fun hash(message: String): String
    fun areDigestsEqual(hash1: String?, hash2: String?): Boolean

    companion object {
        const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        const val SALT = "RivoSalt"
        const val ITERATION_COUNT = 65536
        const val KEY_LENGTH = 128
        const val KEY_ALGORITHM = "PBKDF2WithHmacSha256"
    }
}

data class EncryptionResult(
    val data: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionResult

        if (!data.contentEquals(other.data)) return false
        return iv.contentEquals(other.iv)
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}