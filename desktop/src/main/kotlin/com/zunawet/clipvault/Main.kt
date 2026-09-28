package com.zunawet.clipvault
import com.zunawet.clipvault.di.appModule
import com.zunawet.clipvault.platform.common.SingleInstance
import com.zunawet.clipvault.utils.AppPaths
import org.koin.core.context.startKoin
import java.io.PrintStream
import javax.swing.SwingUtilities
fun main() {
 System.setProperty("compose.swing.render.on.graphics","true")
 val instance=SingleInstance()
 if(!instance.primary) { instance.notifyExisting();instance.close();return }
 val log=PrintStream(AppPaths.root.resolve("clipvault.log").toFile().outputStream(),true,"UTF-8")
 System.setErr(log)
 Thread.setDefaultUncaughtExceptionHandler { _,e -> e.printStackTrace(log) }
 startKoin { modules(appModule) }
 SwingUtilities.invokeLater { App(instance).start() }
}
