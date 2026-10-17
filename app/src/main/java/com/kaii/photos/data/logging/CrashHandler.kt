package com.kaii.photos.data.logging

import android.content.Context
import android.os.Build
import android.util.Log

class CrashHandler(private val context: Context, private val defaultHandler: Thread.UncaughtExceptionHandler?) : Thread.UncaughtExceptionHandler {
 companion object { fun getLogFile(context: Context) = java.io.File(context.getExternalFilesDir("logs"), "crash-log.txt") }
 override fun uncaughtException(thread: Thread, throwable: Throwable) {
  try { CrashDiagnostics.append(context, "FATAL THREAD: ${thread.name}\nDevice: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n${Log.getStackTraceString(throwable)}") } catch (_: Throwable) { }
  finally { defaultHandler?.uncaughtException(thread, throwable) }
 }
}