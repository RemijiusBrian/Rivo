package dev.ridill.rivo.core.domain.crypto

interface CryptoManager {
    fun encrypt(rawData: ByteArray, password: String): ByteArray
    fun decrypt(encryptedData: ByteArray, password: String): ByteArray
}