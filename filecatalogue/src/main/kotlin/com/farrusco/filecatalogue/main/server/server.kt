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
package com.farrusco.filecatalogue.main.server
/*

import com.farrusco.filecatalogue.business.Systeem
import com.farrusco.projectclasses.utils.Cryption
import com.farrusco.projectclasses.utils.StringUtils
import java.io.*
import java.net.ServerSocket
import java.net.Socket

object server {
    @JvmStatic
    fun main(args: Array<String>) {
        var systeem = Systeem(this)
        if (args.size != 5) {
            System.out.println(StringUtils.repeat("-", 50))
            println("wrong number of arguments")
            println("")
            println("Parm1 = secretkey")
            println("Parm2 = masterkey")
            println("Parm3 = portnumber listen to (example 8888)")
            println("Parm4 = directory")
            println("Parm5 = filename")
            System.out.println(StringUtils.repeat("-", 50))
            return
        }
        var serverSocket: ServerSocket? = null
        var socket: Socket? = null
        var dataInputStream: DataInputStream? = null
        var dataOutputStream: DataOutputStream? = null
        // ThisIsASecretKey MasterKey 8888 c:\temp output.xml
        val password = args[1]
        Cryption.keyValue = args[0].toByteArray().toString()
        var rtn = "Ok"
        var console = ""
        serverSocket = try {
            //ServerSocket(Integer.valueOf(args[2])
            ServerSocket(args[2].toInt())
            //System.out.println("Listening: " + args[2]);
        } catch (e: IOException) {
            // Auto-generated catch block
            e.printStackTrace()
            return
        }
        while (true) {
            try {
                rtn = "Ok"
                socket = serverSocket!!.accept()
                dataInputStream = DataInputStream(socket.getInputStream())
                dataOutputStream = DataOutputStream(
                    socket.getOutputStream()
                )
                console = "ip: " + socket.inetAddress
                var tmp: String
                tmp = try {
                    dataInputStream.readUTF()
                } catch (e1: EOFException) {
                    ""
                }

                //System.out.println("message: " + tmp);
                try {
                    tmp = Cryption.decrypt(tmp, systeem.cryptionMasterKey, systeem.cryptionSecretKey).toString()
                    //System.out.println("decrypt: " + tmp);
                    writeToFile(tmp, args[3], args[4])
                    println(console + " received data (" + tmp.length + " bytes)")
                } catch (e: Exception) {
                    rtn = "Server Exception"
                    println("$console error")
                }
                //System.out.println(rtn);
                dataOutputStream.writeUTF(rtn)
            } catch (e: IOException) {
                println("IOException")
                // Auto-generated catch block
                e.printStackTrace()
            } finally {
                if (socket != null) {
                    try {
                        socket.close()
                    } catch (e: IOException) {
                        println("IOException")
                        // Auto-generated catch block
                        e.printStackTrace()
                    }
                }
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close()
                    } catch (e: IOException) {
                        println("IOException")
                        // Auto-generated catch block
                        e.printStackTrace()
                    }
                }
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close()
                    } catch (e: IOException) {
                        println("IOException")
                        // Auto-generated catch block
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun writeToFile(
        xmlString: String, exportDirName: String,
        exportFileName: String
    ) {
        val tmp = """
            $xmlString
            
            """.trimIndent()
        val dir = File(exportDirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        try {
            val fos = FileOutputStream(
                exportDirName + "\\"
                        + exportFileName, true
            )
            fos.write(tmp.toByteArray())
            fos.close()
        } catch (ex: FileNotFoundException) {
            println("FileNotFoundException : $ex")
        } catch (ioe: IOException) {
            println("IOException : $ioe")
        }
    }
}*/
