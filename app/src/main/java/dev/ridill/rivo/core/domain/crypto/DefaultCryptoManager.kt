package dev.ridill.rivo.core.domain.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class DefaultCryptoManager : CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private fun getEncryptCipher(): Cipher = Cipher
        .getInstance(TRANSFORMATION)
        .apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }

    private fun getDecryptCipher(iv: ByteArray): Cipher = Cipher
        .getInstance(TRANSFORMATION)
        .apply {
            init(Cipher.DECRYPT_MODE, getOrCreateKey(), IvParameterSpec(iv))
        }

    private fun getOrCreateKey(): SecretKey =
        (keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: createKey()

    private fun createKey(): SecretKey = KeyGenerator.getInstance(ALGORITHM)
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
                    .build()
            )
        }.generateKey()

    override fun encrypt(rawData: ByteArray, password: String): EncryptionResult {
        val cipher = getEncryptCipher()
        val encryptedData = cipher.doFinal(rawData)
        return EncryptionResult(
            data = encryptedData,
            iv = cipher.iv
        )
    }

    override fun decrypt(encryptedData: EncryptionResult, password: String): ByteArray =
        getDecryptCipher(encryptedData.iv).doFinal(encryptedData.data)

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val ALIAS = "RivoKey"
    }
}