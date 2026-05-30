package com.netscanner.app.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object RootUtils {

    fun isRooted(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -c id")
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    fun runAsRoot(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val dos = DataOutputStream(process.outputStream)
            val br = BufferedReader(InputStreamReader(process.inputStream))
            val errBr = BufferedReader(InputStreamReader(process.errorStream))

            dos.writeBytes("$command\n")
            dos.writeBytes("exit\n")
            dos.flush()

            val output = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                output.appendLine(line)
            }
            val error = StringBuilder()
            while (errBr.readLine().also { line = it } != null) {
                error.appendLine(line)
            }

            process.waitFor()
            dos.close()

            if (output.isNotBlank()) output.toString() else error.toString()
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }

    fun runAsRootLines(command: String): List<String> {
        return runAsRoot(command).lines().filter { it.isNotBlank() }
    }
}
