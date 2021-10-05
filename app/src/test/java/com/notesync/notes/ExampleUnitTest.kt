package com.notesync.notes

import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.*
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun sameUiniqueSecretKeyForSameUserIdAndPassword() {
        val password = "password"+"asdfjsahdasd15454c".toCharArray()
        var sk :String=""
        for(c in password){
            sk += (c.code).toString()
        }

        val uuid =UUID(sk.substring(0,6).toLong(),sk.substring(20,24).toLong()).toString()
        val uuid1 = UUID(sk.substring(0,6).toLong(),sk.substring(20,24).toLong()).toString()
        print(uuid)
        print(uuid1)
        assertTrue(uuid == uuid1)
    }
}