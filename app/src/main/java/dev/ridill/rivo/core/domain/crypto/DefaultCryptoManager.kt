package dev.ridill.rivo.core.domain.crypto

import android.security.keystore.KeyProperties
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DefaultCryptoManager : CryptoManager {

    private fun getEncryptCipher(password: String): Cipher = Cipher
        .getInstance(CryptoManager.TRANSFORMATION)
        .apply {
            init(Cipher.ENCRYPT_MODE, createKey(password))
        }

    private fun getDecryptCipher(password: String, iv: ByteArray): Cipher = Cipher
        .getInstance(CryptoManager.TRANSFORMATION)
        .apply {
            init(Cipher.DECRYPT_MODE, createKey(password), IvParameterSpec(iv))
        }

    private fun createKey(password: String): SecretKey {
        val factory = SecretKeyFactory.getInstance(CryptoManager.KEY_ALGORITHM)
        val keySpec = PBEKeySpec(
            password.toCharArray(),
            CryptoManager.SALT.toByteArray(),
            CryptoManager.ITERATION_COUNT,
            CryptoManager.KEY_LENGTH
        )
        val key = factory.generateSecret(keySpec)
        return SecretKeySpec(key.encoded, CryptoManager.ALGORITHM)
    }

    override fun encrypt(rawData: ByteArray, password: String): EncryptionResult {
        val cipher = getEncryptCipher(password)
        val encryptedData = cipher.doFinal(rawData)
        return EncryptionResult(
            data = encryptedData,
            iv = cipher.iv
        )
    }

    override fun decrypt(encryptedData: ByteArray, iv: ByteArray, password: String): ByteArray =
        getDecryptCipher(password, iv).doFinal(encryptedData)

    @OptIn(ExperimentalStdlibApi::class)
    override fun hash(message: String): String =
        MessageDigest.getInstance(KeyProperties.DIGEST_SHA256)
            .digest(message.toByteArray()).toHexString()

    @OptIn(ExperimentalStdlibApi::class)
    override fun areDigestsEqual(hash1: String?, hash2: String?): Boolean =
        MessageDigest.isEqual(hash1?.hexToByteArray(), hash2?.hexToByteArray())
}