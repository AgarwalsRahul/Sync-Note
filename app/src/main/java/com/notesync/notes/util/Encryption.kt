package com.notesync.notes.util

import com.google.common.base.Charsets
import okio.Utf8
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.jvm.Throws

object Encryption {

    @Throws(Exception::class)
    fun encrypt(plaintext: ByteArray, key: String): ByteArray {
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8).copyOfRange(0,16), "AES")
        val iv = IvParameterSpec(key.toByteArray(kotlin.text.Charsets.UTF_8).copyOfRange(0,16))
        cipher.init(Cipher.ENCRYPT_MODE, keySpec,iv)
        return cipher.doFinal(plaintext)
    }
}