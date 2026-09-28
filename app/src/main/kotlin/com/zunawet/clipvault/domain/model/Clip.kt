package com.zunawet.clipvault.domain.model

enum class ClipType { TEXT, CODE, LINK, IMAGE, COLOR, EMAIL, FILE, PHONE, ADDRESS, SENSITIVE }
data class Clip(
 val id: String, val type: ClipType, val content: String, val preview: String,
 val hash: String, val createdAt: Long, val pinned: Boolean = false,
 val ocr: String? = null, val sourceApp: String? = null
)
data class Keybind(val action: String, val modifiers: Set<String>, val key: String) {
 val label: String get() = (listOf("ctrl", "shift", "alt", "win").filter { it in modifiers }.map { it.replaceFirstChar(Char::uppercase) } + key).joinToString(" + ")
 companion object {
  val defaults = listOf(
   Keybind("panel", setOf("ctrl", "shift"), "V"), Keybind("overlay", setOf("alt"), "V"),
   Keybind("pause", setOf("ctrl", "shift"), "P"), Keybind("plain", setOf("ctrl", "shift"), "T"),
   Keybind("recent1", setOf("ctrl", "shift"), "1"), Keybind("recent2", setOf("ctrl", "shift"), "2"), Keybind("recent3", setOf("ctrl", "shift"), "3")
  )
 }
}
