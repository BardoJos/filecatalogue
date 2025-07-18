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

import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

class CSVFileReader(filename: String, seperator: String) {
    private var fileName: String = filename
    private var seperatorX: String = ";"
    private var storeValues = ArrayList<String>()
    fun readFile() {
        var record: String
        var br:BufferedReader? = null
        try {
            storeValues.clear() //just in case this is the second call of the
            // ReadFile Method./
            if (File(fileName).isFile){
                br = BufferedReader(FileReader(fileName))
                //StringTokenizer st = null;
                while (br.readLine().also { record = it } != null) {
                    // System.out.println(record);
                    storeValues.add(record)
                }
            }
        } catch (_: FileNotFoundException) {
            //e.printStackTrace()
        }
        catch (_: Exception) {
            //e.printStackTrace()
        }
        br?.close()
    }

    val fileValuesSplit: ArrayList<Any>
        get() {
            val arrFile = ArrayList<Any>()
            var arrRecord: ArrayList<String?>
            var inQuotes = false
            val comma = "[COMMA]"
            for (i in storeValues.indices) {
                arrRecord = ArrayList()
                val rec = storeValues[i]
                val cleanedCsv = StringBuilder()
                for (x in rec.indices) {
                    if (rec[x] == '\"') {
                        inQuotes = !inQuotes
                    } else {
                        if (inQuotes && rec.substring(x, x + 1) == seperatorX) cleanedCsv.append(
                            comma
                        ) else cleanedCsv.append(
                            rec[x]
                        )
                    }
                }
                val values = cleanedCsv.toString().split(seperatorX).toTypedArray()
                for (x in values.indices) {
                    arrRecord.add(values[x].replace(comma, seperatorX))
                }
                arrFile.add(arrRecord)
            }
            return arrFile
        }

    init {
        if (seperator != "") seperatorX = seperator
    }
}