package systems.concurrent.crediversemobile.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import systems.concurrent.crediversemobile.activities.LoginActivity
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.Instant
import kotlin.system.exitProcess

class UncaughtExceptionHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    companion object {
        private val _tag = this::class.java.kotlin.simpleName
        const val CRASH_FILE_NAME = "CRASH_LOG.txt"
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Handle the uncaught exception here
        Log.e(_tag, "Let's handle this uncaught exception... ${throwable.message}")

        writeCrashToDisk(throwable)

        if (context !is LoginActivity) restartApp()
        if (context is Activity) context.finish()

        exitProcess(0)
    }

    private fun writeCrashToDisk(exception: Throwable) {
        val now = Instant.now().toString()
        val crashFile = File(context.filesDir, CRASH_FILE_NAME)
        val writer = FileWriter(crashFile)
        writer.write("CRASH TIMESTAMP: $now\n\n" + exception.stackTraceToString())
        writer.write("\n\n")
        exception.printStackTrace(PrintWriter(writer))
        writer.close()
    }

    private fun restartApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    fun getCrashReportIfExists(): String? {
        val file = File(context.filesDir, CRASH_FILE_NAME)
        if (file.exists() && file.canRead()) {
            var returnString = ""
            returnString += file.readLines().joinToString("\n")
            return returnString
        }
        return null
    }

    fun deleteCrashReport() {
        File(context.filesDir, CRASH_FILE_NAME).delete()
    }
}
