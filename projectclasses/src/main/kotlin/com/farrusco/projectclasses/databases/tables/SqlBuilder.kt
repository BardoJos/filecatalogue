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
package com.farrusco.projectclasses.databases.tables

import java.util.Locale
@Suppress("unused", "MemberVisibilityCanBePrivate")
class SqlBuilder {
    private val columns: MutableList<String?> = mutableListOf()
    private val tables: MutableList<String?> = mutableListOf()
    private val joins: MutableList<String?> = mutableListOf()
    private val wheres: MutableList<String?> = mutableListOf()
    private val orderBys: MutableList<String?> = mutableListOf()
    private val groupBys: MutableList<String?> = mutableListOf()
    private val having: MutableList<String?> = mutableListOf()
    var distinct: Boolean= false

    private fun appendList(
        sql: StringBuilder, list: List<String?>, init: String,
        sep: String
    ) {
        var first = true
        for (s in list) {
            if (first) {
                sql.append(init)
            } else {
                sql.append(sep)
            }
            sql.append(s)
            first = false
        }
    }

    fun column(name: String?): SqlBuilder {
        columns.add(name)
        return this
    }

    fun columnAlias(table: Tables, name: String): SqlBuilder {
        columns.add(
            table.tableName + "." + name + " AS "
                    + table.tableName + "_" + name
        )
        return this
    }

    fun column(table: Tables, name: String): SqlBuilder {
        columns.add(table.tableName + "." + name)
        return this
    }

    fun column(table: Tables, name: String, alias: String): SqlBuilder {
        columns.add(table.tableName + "." + name + " AS " + alias)
        return this
    }

    fun column(name: String?, groupBy: Boolean): SqlBuilder {
        columns.add(name)
        if (groupBy) {
            groupBys.add(name)
        }
        return this
    }

    fun from(table: Tables): SqlBuilder {
        tables.add(table.tableName)
        return this
    }

    fun join(join: String?): SqlBuilder {
        joins.add(join)
        return this
    }

    fun orderBy(name: String?): SqlBuilder {
        orderBys.add(name)
        return this
    }

    override fun toString(): String {
        val sql = StringBuilder("SELECT ")
        if (distinct){
            sql.append(" DISTINCT ")
        }
        if (columns.isEmpty()) {
            sql.append("*")
        } else {
            appendList(sql, columns, "", ", ")
        }
        if (tables.isEmpty()) {
            try {
                throw Exception("No 'from' informed!")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            appendList(sql, tables, " FROM ", ", ")
        }
        appendList(sql, joins, " JOIN ", " JOIN ")
        appendList(sql, wheres, " WHERE ", " ")
        appendList(sql, groupBys, " GROUP BY ", ", ")
        appendList(sql, having, " HAVING ", " AND ")
        appendList(sql, orderBys, " ORDER BY ", ", ")
        return sql.toString()
    }

    fun where(expr: String): SqlBuilder {
        if (expr == "") {
            return this
        }
        if (wheres.isEmpty()) {
            wheres.add(expr)
        } else if (expr.lowercase(Locale.getDefault()).startsWith("AND ") || expr.startsWith("OR ")) {
            wheres.add(expr)
        } else {
            wheres.add(" AND $expr")
        }
        return this
    }

    fun andWhere(where: String): SqlBuilder {
        wheres.add((if (wheres.isEmpty()) "" else " AND ") + where)
        return this
    }

    fun join(table: Tables, on: String): SqlBuilder {
        joins.add(table.tableName + " ON " + on)
        return this
    }
}