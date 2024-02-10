package dev.ridill.rivo.core.domain.crypto

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DefaultCryptoManagerTest {

    private lateinit var password: String
    private lateinit var salt: String

    @Before
    fun setUp() {
        password = "Test Password"
        salt = "Test Salt"
    }

    @Test
    fun generateKeyWithSamePasswordTwice_bothKeysSame() {
        val factory1 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keySpec1 = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 65536, 128)
        val key1 = factory1.generateSecret(keySpec1).encoded
        val secretKey1 = SecretKeySpec(key1, "AES") as SecretKey
        println("Key1 - ${secretKey1.encoded.decodeToString()}")
        println("Key1 Alg - ${secretKey1.algorithm}")

        val factory2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val keySpec2 = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 65536, 128)
        val key2 = factory2.generateSecret(keySpec2).encoded
        val secretKey2 = SecretKeySpec(key2, "AES")
        println("Key2 - ${secretKey2.encoded.decodeToString()}")
        println("Key2 Alg - ${secretKey2.algorithm}")

        assertThat(key1).isEqualTo(key2)
    }
}