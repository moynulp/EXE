package com.zunawet.clipvault
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import com.zunawet.clipvault.data.repository.VaultRepository
import com.zunawet.clipvault.domain.model.*
import java.nio.file.Files
class RepositoryTest {
 @Test fun persistenceAndPinProtection() = runBlocking {
  val folder=Files.createTempDirectory("clipvault-test")
  val path=folder.resolve("test.db")
  val clip=Clip("1",ClipType.TEXT,"persist me","persist me","hash",10)
  VaultRepository(path).use { repo ->
   repo.initialize();repo.insert(clip);repo.pin(clip);repo.setting("cursor","false")
   repo.bind(Keybind("panel",setOf("alt","shift"),"K"));repo.ocr("1","recognized")
   repo.exclusions(listOf("KeePass.exe"));repo.clear()
   assertEquals(1,repo.clips.value.size)
  }
  VaultRepository(path).use { repo ->
   repo.initialize();assertTrue(repo.clips.value.single().pinned)
   assertEquals("recognized",repo.clips.value.single().ocr)
   assertFalse(repo.enabled("cursor",true));assertEquals("K",repo.keybinds.value.first().key)
   assertEquals(listOf("keepass.exe"),repo.excluded.value)
   repo.delete(repo.clips.value.single());assertTrue(repo.clips.value.isEmpty())
  }
  folder.toFile().deleteRecursively()
  Unit
 }
}
