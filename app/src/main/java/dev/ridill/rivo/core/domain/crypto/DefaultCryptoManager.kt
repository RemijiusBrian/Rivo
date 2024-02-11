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
        .getInstance(TRANSFORMATION)
        .apply {
            init(Cipher.ENCRYPT_MODE, createKey(password))
        }

    private fun getDecryptCipher(password: String, iv: ByteArray): Cipher = Cipher
        .getInstance(TRANSFORMATION)
        .apply {
            init(Cipher.DECRYPT_MODE, createKey(password), IvParameterSpec(iv))
        }

    private fun createKey(password: String): SecretKey {
        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val keySpec =
            PBEKeySpec(password.toCharArray(), SALT.toByteArray(), ITERATION_COUNT, KEY_LENGTH)
        val key = factory.generateSecret(keySpec)
        return SecretKeySpec(key.encoded, ALGORITHM)
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

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val SALT = "RivoSalt"
        private const val ITERATION_COUNT = 65536
        private const val KEY_LENGTH = 128
        private const val KEY_ALGORITHM = "PBKDF2WithHmacSha256"
        //        private const val ALIAS = "RivoKey"
    }
}