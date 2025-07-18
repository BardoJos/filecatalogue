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
package com.farrusco.projectclasses.databases

import java.io.IOException

class XmlBuilder {

    private val openXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
    private val sb: StringBuilder = StringBuilder()
    fun start(dbName: String?) {
        sb.append(openXml)
        sb.append(DB_OPEN).append(dbName).append(CLOSE_WITH_TICK)
    }

    @Throws(IOException::class)
    fun end(): String {
        sb.append(DB_CLOSE)
        return sb.toString()
    }

    fun openTable(tableName: String?) {
        sb.append(TABLE_OPEN).append(tableName).append(CLOSE_WITH_TICK)
    }

    fun closeTable() {
        sb.append(TABLE_CLOSE)
    }

    fun openRow() {
        sb.append(ROW_OPEN)
    }

    fun closeRow() {
        sb.append(ROW_CLOSE)
    }

    @Throws(IOException::class)
    fun addColumn(name: String?, `val`: String?) {
        sb.append(COL_OPEN).append(name).append(CLOSE_WITH_TICK).append(`val`).append(COL_CLOSE)
    }

    companion object {
        const val OPEN_XML_STANZA = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        private const val CLOSE_WITH_TICK = "'>"
        private const val DB_OPEN = "<database name='"
        private const val DB_CLOSE = "</database>"
        private const val TABLE_OPEN = "<table name='"
        private const val TABLE_CLOSE = "</table>"
        private const val ROW_OPEN = "<row>"
        private const val ROW_CLOSE = "</row>"
        private const val COL_OPEN = "<col name='"
        private const val COL_CLOSE = "</col>"
    }

}