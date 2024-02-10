package dev.ridill.rivo.core.domain.crypto

interface CryptoManager {
    fun encrypt(rawData: ByteArray, password: String): EncryptionResult
    fun decrypt(encryptedData: EncryptionResult, password: String): ByteArray
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