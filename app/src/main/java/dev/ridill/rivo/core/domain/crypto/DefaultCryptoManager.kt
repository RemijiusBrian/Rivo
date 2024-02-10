package dev.ridill.rivo.core.domain.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class DefaultCryptoManager : CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

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


    private fun createKey(password: String): SecretKey = KeyGenerator.getInstance(ALGORITHM)
        .apply {
            init(
                KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
                SecureRandom.getInstance(ALGORITHM).apply {
                    setSeed(password.toByteArray())
                }
            )
        }.generateKey()

    override fun encrypt(rawData: ByteArray, password: String): ByteArray {
        TODO("Not yet implemented")
    }

    override fun decrypt(encryptedData: ByteArray, password: String): ByteArray {
        TODO("Not yet implemented")
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val ALIAS = "RivoKey"
    }
}