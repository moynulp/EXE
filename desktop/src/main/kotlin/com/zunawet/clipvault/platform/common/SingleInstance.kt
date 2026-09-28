package com.zunawet.clipvault.platform.common
import com.zunawet.clipvault.utils.AppPaths
import java.nio.channels.FileChannel
import java.nio.file.*
import kotlinx.coroutines.*
/** No network IPC: the existing process watches a request file beside the lock. */
class SingleInstance: AutoCloseable {
 private val channel=FileChannel.open(AppPaths.root.resolve("instance.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE)
 private val lock=runCatching { channel.tryLock() }.getOrNull()
 val primary get()=lock!=null
 private val request=AppPaths.root.resolve("show.request")
 private var job: Job?=null
 fun notifyExisting() { Files.writeString(request,System.nanoTime().toString()) }
 fun listen(scope: CoroutineScope,show: () -> Unit) {
  Files.deleteIfExists(request)
  job=scope.launch { while(isActive) { delay(400);if(withContext(Dispatchers.IO) { Files.deleteIfExists(request) }) show() } }
 }
 override fun close() { job?.cancel();lock?.release();channel.close() }
}
