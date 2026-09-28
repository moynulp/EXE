package com.zunawet.clipvault.data.ocr
import com.zunawet.clipvault.utils.AppPaths
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.sourceforge.tess4j.Tesseract
import java.io.File
import java.nio.file.Files
class OcrService {
 private val mutex=Mutex()
 suspend fun recognize(file: File, bengali: Boolean): String = withContext(Dispatchers.IO) { mutex.withLock {
  for(lang in listOf("eng","ben")) {
   val target=AppPaths.tessdata.resolve("$lang.traineddata")
   if(!Files.exists(target)) javaClass.getResourceAsStream("/tessdata/$lang.traineddata")?.use { input -> Files.copy(input,target) }
  }
  require(Files.exists(AppPaths.tessdata.resolve("eng.traineddata"))) { "English OCR data not installed" }
  val tess=Tesseract();tess.setDatapath(AppPaths.tessdata.toString());tess.setLanguage(if(bengali) "eng+ben" else "eng")
  tess.doOCR(file).trim()
 } }
}
