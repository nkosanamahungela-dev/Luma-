package com.kaii.photos.data.logging

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import java.io.File
import java.util.Date
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object CrashDiagnostics {
 fun start(context: Context, enabled: Boolean) {
  if (!enabled) return
  if (Build.VERSION.SDK_INT >= 30) thread(name="Luma-exit-reasons") { runCatching { val am=context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager; am.getHistoricalProcessExitReasons(context.packageName,0,5).forEach { e -> val reason=when(e.reason){3->"LOW MEMORY";4->"CRASH";5->"NATIVE CRASH";6->"ANR";else->"EXIT ${e.reason}"}; val b=StringBuilder("PROCESS EXIT $reason\nTime: ${Date(e.timestamp)}\nDescription: ${e.description}"); if(e.reason==6) runCatching { e.traceInputStream?.bufferedReader()?.useLines { lines -> b.append("\nANR TRACE\n"); lines.take(200).forEach { b.append(it).append('\n') } } }; append(context,b.toString()) } }.onFailure { append(context,it.stackTraceToString()) } }
  StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectDiskReads().detectNetwork().detectCustomSlowCalls().penaltyLog().build())
  val h=Handler(Looper.getMainLooper()); thread(name="Luma-watchdog",isDaemon=true) { while(true) { val latch=CountDownLatch(1); h.post { latch.countDown() }; if(!latch.await(2,TimeUnit.SECONDS)) append(context,"MAIN THREAD STALL >2s\n"+Looper.getMainLooper().thread.stackTrace.joinToString("\n")); Thread.sleep(500) } }
 }
 fun append(context:Context,text:String) { runCatching { val dir=File(context.filesDir,"diagnostics").apply{mkdirs()}; val f=File(dir,"crash_log.txt"); synchronized(this) { f.appendText("\n===== ${Date()} =====\nDevice: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n$text\n"); if(f.length()>512000) { File(dir,"crash_log_${System.currentTimeMillis()}.txt").writeText(f.readText()); f.writeText("Rotated after 512 KB\n") }; dir.listFiles()?.filter{it.name.startsWith("crash_log") }?.sortedByDescending{it.lastModified()}?.drop(5)?.forEach{it.delete()} } } }
 fun read(context:Context)=File(File(context.filesDir,"diagnostics"),"crash_log.txt").takeIf{it.exists()}?.readText().orEmpty().ifBlank{"No reports yet."}
 fun clear(context:Context){File(context.filesDir,"diagnostics").deleteRecursively()}
}