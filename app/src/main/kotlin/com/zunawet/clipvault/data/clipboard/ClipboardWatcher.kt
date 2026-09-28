package com.zunawet.clipvault.data.clipboard
import com.zunawet.clipvault.data.repository.VaultRepository
import com.zunawet.clipvault.data.ocr.OcrService
import com.zunawet.clipvault.domain.model.*
import com.zunawet.clipvault.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel
import java.awt.Toolkit
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.datatransfer.*
import java.io.File
import java.util.UUID
import javax.imageio.ImageIO
import net.coobird.thumbnailator.Thumbnails

class ClipboardWatcher(private val repo: VaultRepository, private val ocr: OcrService,
 private val scope: CoroutineScope, private val foreground: () -> String?, private val notify: (String) -> Unit) : AutoCloseable {
 val paused=MutableStateFlow(false)
 val processing=MutableStateFlow<Set<String>>(emptySet())
 private val clipboard=Toolkit.getDefaultToolkit().systemClipboard
 private val duplicates=DuplicateFilter()
 @Volatile private var skipHash: String?=null
 private var lastHash: String?=null
 private var job: Job?=null
 private var sequence: Int?=null
 private val changes=Channel<Unit>(Channel.CONFLATED)
 private val listener=FlavorListener { changes.trySend(Unit) }
 fun ignoreNext(hash: String) { skipHash=hash }
 fun start() {
  clipboard.addFlavorListener(listener)
  job=scope.launch(Dispatchers.IO) {
   while(isActive) {
    runCatching { sample() }.onFailure { System.err.println("Clipboard: ${it.message}") }
    withTimeoutOrNull(250) { changes.receive() }
   }
  }
 }
 private suspend fun sample() {
  val nextSequence=ClipboardSequence.current()
  if(nextSequence!=null && nextSequence==sequence) return
  val t=clipboard.getContents(null) ?: return
  // Fingerprint even when paused/excluded so resuming never captures an old secret.
  val id=UUID.randomUUID().toString()
  var image: BufferedImage?=null
  val type: ClipType
  val content: String
  val bytes: ByteArray
  when {
   t.isDataFlavorSupported(DataFlavor.javaFileListFlavor) -> {
    type=ClipType.FILE; content=(t.getTransferData(DataFlavor.javaFileListFlavor) as List<*>).filterIsInstance<File>().joinToString("\n") { it.absolutePath }; bytes=content.toByteArray()
   }
   t.isDataFlavorSupported(DataFlavor.imageFlavor) -> {
    type=ClipType.IMAGE
    val raw=t.getTransferData(DataFlavor.imageFlavor) as Image
    val w=raw.getWidth(null); val h=raw.getHeight(null)
    if(w<=0 || h<=0 || w.toLong()*h>40_000_000) return
    image=BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB)
    image.createGraphics().apply { drawImage(raw,0,0,null);dispose() }
    bytes=java.io.ByteArrayOutputStream().use { ImageIO.write(image,"png",it);it.toByteArray() }
    content=AppPaths.images.resolve("$id.png").toString()
   }
   t.isDataFlavorSupported(DataFlavor.stringFlavor) -> {
    content=t.getTransferData(DataFlavor.stringFlavor) as String
    if(content.isBlank() || content.length>2_000_000) return
    type=ContentDetector.detect(content);bytes=content.toByteArray()
   }
   else -> return
  }
  val hash=hash(bytes)
  val same=hash==lastHash
  sequence=nextSequence
  if(nextSequence==null && same) return
  lastHash=hash
  if(hash==skipHash) { skipHash=null;return }
  skipHash=null
  if(paused.value || !duplicates.accept(hash)) return
  val source=foreground()
  if(source==null && repo.excluded.value.isNotEmpty()) return
  if(repo.excluded.value.any { it.equals(source,true) }) return
  // Fail closed: never store suspected passwords or card numbers in an unencrypted database.
  if(type==ClipType.SENSITIVE) return
  if(image!=null) {
   File(content).writeBytes(bytes)
   Thumbnails.of(image).size(96,96).toFile(AppPaths.images.resolve("$id-thumb.png").toFile())
  }
  val c=Clip(id,type,content,if(type==ClipType.IMAGE) "Image · ${image!!.width} × ${image.height}" else content.replace('\n',' ').take(120),hash,System.currentTimeMillis(),sourceApp=source)
  repo.insert(c)
  if(repo.enabled("sound")) Toolkit.getDefaultToolkit().beep()
  if(repo.enabled("notifications")) notify(c.preview.take(40))
  if(type==ClipType.IMAGE) scope.launch(Dispatchers.IO) {
   processing.update { it+id }
   try { repo.ocr(id,ocr.recognize(File(content),repo.enabled("bengali"))) }
   catch(e: Exception) { System.err.println("OCR: ${e.message}") }
   finally { processing.update { it-id } }
  }
 }
 override fun close() { job?.cancel();clipboard.removeFlavorListener(listener) }
}
