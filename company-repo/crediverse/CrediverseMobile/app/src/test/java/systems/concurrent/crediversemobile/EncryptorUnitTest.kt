package systems.concurrent.crediversemobile

import org.junit.Test

import org.junit.Assert.*
import systems.concurrent.crediversemobile.utils.Encryptor
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException

class EncryptorUnitTest {
    @Test
    fun failsToDecryptInvalidStrings() {
        val encryptor = Encryptor()
        try {
            // base64 of plaintext (not encrypted data)
            val ciphertext = encryptor.decrypt("c29tZSB0ZXh0IHBhZGRlZA==")
            assertEquals(ciphertext, "THROWS BadPaddingException")
        } catch (_: BadPaddingException) {
            assertEquals(true, true)
        }

        try {
            // base64 of plaintext (not encrypted data)
            val ciphertext = encryptor.decrypt("c29tZSB0ZXh0")
            assertEquals(ciphertext, "THROWS IllegalBlockSizeException")
        } catch (_: IllegalBlockSizeException) {
            assertEquals(true, true)
        }

        try {
            // base64 of plaintext (not encrypted data)
            val ciphertext = encryptor.decrypt("non-base-64-String")
            assertEquals(ciphertext, "THROWS IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            assertEquals(true, true)
        }
    }

    @Test
    fun failsToEncryptEmptyString() {
        val encryptor = Encryptor()
        val result = encryptor.encrypt("")
        assertEquals(result, null)
    }
}