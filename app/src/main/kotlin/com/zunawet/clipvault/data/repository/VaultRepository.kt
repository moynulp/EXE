package com.zunawet.clipvault.data.repository
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.zunawet.clipvault.data.local.db.ClipVaultDatabase
import com.zunawet.clipvault.domain.model.*
import com.zunawet.clipvault.utils.AppPaths
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Files

class VaultRepository(private val path: java.nio.file.Path=AppPaths.root.resolve("clipvault.db")) : AutoCloseable {
 private val fresh=!Files.exists(path)
 private val driver=JdbcSqliteDriver("jdbc:sqlite:$path")
 private val db=ClipVaultDatabase(driver)
 private val q get() = db.clipVaultQueries
 private val mutex=Mutex()
 val clips=MutableStateFlow<List<Clip>>(emptyList())
 val settings=MutableStateFlow<Map<String,String>>(emptyMap())
 val keybinds=MutableStateFlow(Keybind.defaults)
 val excluded=MutableStateFlow<List<String>>(emptyList())
 suspend fun initialize() = withContext(Dispatchers.IO) {
  if(fresh) ClipVaultDatabase.Schema.create(driver)
  driver.execute(null,"PRAGMA journal_mode=WAL",0)
  driver.execute(null,"PRAGMA busy_timeout=5000",0)
  reload(); settings.value=q.allSettings().executeAsList().associate { it.key to it.value_ }
  val saved=q.allKeybinds().executeAsList().associateBy { it.action }
  keybinds.value=Keybind.defaults.map { d -> saved[d.action]?.let { Keybind(it.action,it.modifiers.split(',').filter(String::isNotEmpty).toSet(),it.key) } ?: d }
  excluded.value=q.allExcluded().executeAsList()
 }
 private fun reload() { clips.value=q.allClips().executeAsList().map {
  Clip(it.id,ClipType.valueOf(it.type),it.content,it.preview,it.content_hash,it.created_at,it.is_pinned==1L,it.ocr_text,it.source_app)
 } }
 private suspend fun write(block: () -> Unit) = withContext(Dispatchers.IO) { mutex.withLock { block() } }
 suspend fun insert(c: Clip) = write {
  q.insertClip(c.id,c.type.name,c.content,c.ocr,c.preview,c.hash,c.sourceApp,c.content.toByteArray().size.toLong(),c.createdAt,c.createdAt);reload()
 }
 suspend fun pin(c: Clip) = write { q.pin(if(c.pinned) 0 else 1,System.currentTimeMillis(),c.id);reload() }
 suspend fun delete(c: Clip) = write {
  q.deleteClip(c.id); if(c.type==ClipType.IMAGE) deleteImage(c);reload()
 }
 suspend fun clear() = write {
  val images=clips.value.filter { !it.pinned && it.type==ClipType.IMAGE }
  q.clearUnpinned(); images.forEach { runCatching { deleteImage(it) } };reload()
 }
 suspend fun ocr(id: String,text: String) = write { q.ocr(text,System.currentTimeMillis(),id);reload() }
 suspend fun setting(key: String,value: String) = write { q.putSetting(key,value); settings.value=settings.value+(key to value) }
 fun enabled(key: String, default: Boolean=false)=settings.value[key]?.toBooleanStrictOrNull() ?: default
 suspend fun bind(b: Keybind) = write { q.putKeybind(b.action,b.modifiers.joinToString(","),b.key,0); keybinds.value=keybinds.value.map { if(it.action==b.action) b else it } }
 suspend fun resetBindings() { Keybind.defaults.forEach { bind(it) } }
 suspend fun exclusions(names: List<String>) = write {
  excluded.value.forEach { q.removeExcluded(it) }
  names.filter { it.isNotBlank() }.distinct().forEach { q.addExcluded(java.util.UUID.randomUUID().toString(),it.trim().lowercase(),System.currentTimeMillis()) }
  excluded.value=q.allExcluded().executeAsList()
 }
 private fun deleteImage(c: Clip) {
  Files.deleteIfExists(java.nio.file.Path.of(c.content))
  Files.deleteIfExists(java.nio.file.Path.of(c.content.replace(".png","-thumb.png")))
 }
 override fun close() { driver.close() }
}
