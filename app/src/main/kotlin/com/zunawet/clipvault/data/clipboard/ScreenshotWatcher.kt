package com.zunawet.clipvault.data.clipboard
import java.nio.file.*
import kotlinx.coroutines.*
/** Optional importer. Caller owns deduplication and the configured folder. */
class ScreenshotWatcher(private val folder: Path, private val scope: CoroutineScope, private val import: suspend (Path) -> Unit): AutoCloseable {
 private val service=FileSystems.getDefault().newWatchService()
 private var job: Job?=null
 fun start() {
  folder.register(service,StandardWatchEventKinds.ENTRY_CREATE)
  job=scope.launch(Dispatchers.IO) {
   while(isActive) {
    val key=runCatching { service.take() }.getOrNull() ?: break
    for(event in key.pollEvents()) {
     val path=folder.resolve(event.context() as? Path ?: continue)
     if(path.toString().substringAfterLast('.').lowercase() in setOf("png","jpg","jpeg")) {
      delay(500);runCatching { import(path) }
     }
    }
    if(!key.reset()) break
   }
  }
 }
 override fun close() { job?.cancel();service.close() }
}
