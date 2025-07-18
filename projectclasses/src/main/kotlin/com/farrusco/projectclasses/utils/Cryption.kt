/*
 * Copyright (c) 2022. farrusco (jos dot farrusco at gmail dot com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.farrusco.projectclasses.utils

import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Cryption(transformation: String) {

    companion object {
        //var TRANSFORMATION_ASYMMETRIC = "RSA/ECB/PKCS1Padding"
        //var TRANSFORMATION_SYMMETRIC = "AES/CBC/PKCS7Padding"
        var TRANSFORMATION_DES = "DES/CBC/PKCS5Padding"
        const val IV_SEPARATOR = "]"
        private const val ALGORITHM = "AES"
    }

    private var keyValue: String? = null
    private val cipher: Cipher = Cipher.getInstance(transformation)

    private fun generateKey(secretKey: String): Key {
        if (keyValue == null) keyValue = secretKey
        if (keyValue!!.length>8){
            keyValue = secretKey.substring(0,8)
        }
        return SecretKeySpec(keyValue!!.toByteArray(), ALGORITHM)
    }

    fun encrypt(data: String, keystroke: String, useInitializationVector: Boolean = false): String {
        val key = generateKey(keystroke)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        var result = ""
        if (useInitializationVector) {
            val iv = cipher.iv
            val ivString = android.util.Base64.encodeToString(iv, android.util.Base64.DEFAULT)
            result = ivString + IV_SEPARATOR
        }

        val bytes = cipher.doFinal(data.toByteArray())
        result += android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)

        return result
    }

    fun decrypt(data: String, keystroke: String, useInitializationVector: Boolean = false): String {
        val encodedString: String
        val key = generateKey(keystroke)

        if (useInitializationVector) {
            val split = data.split(IV_SEPARATOR.toRegex())
            if (split.size != 2) throw IllegalArgumentException("Passed data is incorrect. There was no IV specified with it.")

            val ivString = split[0]
            encodedString = split[1]
            val ivSpec = IvParameterSpec(android.util.Base64.decode(ivString, android.util.Base64.DEFAULT))
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        } else {
            encodedString = data
            cipher.init(Cipher.DECRYPT_MODE, key)
        }

        val encryptedData = android.util.Base64.decode(encodedString, android.util.Base64.DEFAULT)
        val decodedData = cipher.doFinal(encryptedData)
        return String(decodedData)
    }

    @Suppress("unused")
    fun wrapKey(keyToBeWrapped: Key, keyToWrapWith: Key?): String {
        cipher.init(Cipher.WRAP_MODE, keyToWrapWith)
        val decodedData = cipher.wrap(keyToBeWrapped)
        return android.util.Base64.encodeToString(decodedData, android.util.Base64.DEFAULT)
    }

    @Suppress("unused")
    fun unWrapKey(wrappedKeyData: String, algorithm: String, wrappedKeyType: Int, keyToUnWrapWith: Key?): Key {
        val encryptedKeyData = android.util.Base64.decode(wrappedKeyData, android.util.Base64.DEFAULT)
        cipher.init(Cipher.UNWRAP_MODE, keyToUnWrapWith)
        return cipher.unwrap(encryptedKeyData, algorithm, wrappedKeyType)
    }
}

