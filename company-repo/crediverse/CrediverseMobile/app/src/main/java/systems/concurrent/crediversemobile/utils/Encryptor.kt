package systems.concurrent.crediversemobile.utils

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.*

// ref: https://www.baeldung.com/java-aes-encryption-decryption

class Encryptor(
    private val algorithm: String = "AES/CBC/PKCS5Padding",
    private val key: SecretKeySpec = SecretKeySpec("cdf.2b1!5_2c2e^9".toByteArray(), "AES"),
    private val iv: IvParameterSpec = IvParameterSpec(ByteArray(16))
) {

    fun encrypt(inputText: String): String? {
        return completeEncrypt(algorithm, inputText, key, iv)
    }

    @Throws(Exception::class)
    fun decrypt(cipherText: String): String? {
        return completeDecrypt(algorithm, cipherText, key, iv)
    }

    private fun completeDecrypt(
        algorithm: String, cipherText: String, key: SecretKeySpec, iv: IvParameterSpec
    ): String? {
        if (cipherText.isEmpty()) return null

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText))
        return String(plainText)
    }

    private fun completeEncrypt(
        algorithm: String, inputText: String, key: SecretKeySpec, iv: IvParameterSpec
    ): String? {
        if (inputText.isEmpty()) return null

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val cipherText = cipher.doFinal(inputText.toByteArray())
        return Base64.getEncoder().encodeToString(cipherText)
    }
}