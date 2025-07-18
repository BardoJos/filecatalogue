package com.farrusco.projectclasses.databases

import java.sql.*
import java.util.*

class OdbcConnect {
    private var conn: Connection? = null
    //var username = "username" // provide the username
    //var password = "password" // provide
    fun printTable() {
        // make a connection to MySQL Server
        getConnection()
        // execute the query via connection object
        executeMySQLQuery()
    }
    private fun executeMySQLQuery() {
        var stmt: Statement? = null
        var resultset: ResultSet? = null

        try {
            stmt = conn!!.createStatement()
            resultset = stmt!!.executeQuery("SHOW DATABASES;")

            if (stmt.execute("SHOW DATABASES;")) {
                resultset = stmt.resultSet
            }

            while (resultset!!.next()) {
                println(resultset.getString("Database"))
            }
        } catch (ex: SQLException) {
            // handle any errors
            ex.printStackTrace()
        } finally {
            // release resources
            if (resultset != null) {
                try {
                    resultset.close()
                } catch (sqlEx: SQLException) {
                    // lazy
                }

            }

            if (stmt != null) {
                try {
                    stmt.close()
                } catch (_: SQLException) {
                }

            }

            if (conn != null) {
                try {
                    conn!!.close()
                } catch (_: SQLException) {
                }

                conn = null
            }
        }
    }
    private fun getConnection() {
        val connectionProps = Properties()
        connectionProps["user"] = "root"
        connectionProps["password"] = "Tfzu4RyRE1yE"
        try {
            //Class.forName("com.mysql.jdbc.Driver").newInstance()
           /* conn = DriverManager.getConnection(
                "jdbc:mysql://192.168.0.103:49170/",
                "root","Tfzu4RyRE1yE")*/
            conn = DriverManager.getConnection(
                "jdbc:mysql://nasf7f273.myqnapcloud.com:49170/",
                "root","Tfzu4RyRE1yE")
        } catch (ex: SQLException) {
            // handle any errors
            ex.printStackTrace()
        } catch (ex: Exception) {
            // handle any errors
            ex.printStackTrace()
        }
    }
}