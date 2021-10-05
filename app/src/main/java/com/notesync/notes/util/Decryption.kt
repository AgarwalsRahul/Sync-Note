package com.notesync.notes.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.Exception
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Decryption {

    @RequiresApi(Build.VERSION_CODES.O)
    fun decrypt(cipherText: ByteArray, key: String): String {
        try {
            val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

            val keySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8).copyOfRange(0,16), "AES")
            val iv = IvParameterSpec(key.toByteArray(Charsets.UTF_8).copyOfRange(0,16))
            cipher.init(Cipher.DECRYPT_MODE, keySpec,iv)
            val decryptedText: ByteArray = cipher.doFinal(cipherText)
            return Base64.getDecoder().decode(decryptedText).toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}