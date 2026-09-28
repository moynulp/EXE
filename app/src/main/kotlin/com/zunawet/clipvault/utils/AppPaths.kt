package com.zunawet.clipvault.utils
import java.nio.file.Files
import java.nio.file.Path
object AppPaths {
 val root: Path = Path.of(System.getenv("LOCALAPPDATA") ?: System.getProperty("user.home"), "ClipVault").also { Files.createDirectories(it) }
 val images: Path = root.resolve("images").also { Files.createDirectories(it) }
 val tessdata: Path = root.resolve("tessdata").also { Files.createDirectories(it) }
}
fun hash(bytes: ByteArray): String = java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
fun relativeTime(time: Long): String {
 val s = ((System.currentTimeMillis() - time) / 1000).coerceAtLeast(0)
 return when { s < 60 -> "now"; s < 3600 -> "${s/60}m"; s < 86400 -> "${s/3600}h"; else -> "${s/86400}d" }
}
