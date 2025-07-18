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
@file:Suppress("unused")

package com.farrusco.projectclasses.utils

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.net.UnknownHostException
import javax.crypto.SecretKey

class TCPIPSendReceive(
    private val password: String,
    private val ip: String,
    private val port: Int
) {
    var secretKey: SecretKey? = null
    fun send(mess: String): String {
        var socket: Socket? = null
        var dataOutputStream: DataOutputStream? = null
        var dataInputStream: DataInputStream? = null
        var rtn: String
        try {
            socket = Socket(ip, port)
            dataOutputStream = DataOutputStream(socket.getOutputStream())
            dataInputStream = DataInputStream(socket.getInputStream())
            try {
                dataOutputStream.writeUTF(Cryption(Cryption.TRANSFORMATION_DES).encrypt(mess, password,true))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            rtn = dataInputStream.readUTF()
        } catch (e: UnknownHostException) {
            rtn = "UnknownHostException"
            e.printStackTrace()
        } catch (e: IOException) {
            rtn = "IOException"
            e.printStackTrace()
        } finally {
            if (socket != null) {
                try {
                    socket.close()
                } catch (e: IOException) {
                    rtn = "IOException"
                    e.printStackTrace()
                }
            }
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close()
                } catch (e: IOException) {
                    rtn = "IOException"
                    e.printStackTrace()
                }
            }
            if (dataInputStream != null) {
                try {
                    dataInputStream.close()
                } catch (e: IOException) {
                    rtn = "IOException"
                    e.printStackTrace()
                }
            }
        }
        return rtn
    }
}