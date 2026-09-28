package com.zunawet.clipvault.data.clipboard
import com.sun.jna.Native
import com.sun.jna.win32.StdCallLibrary
/** Win32 sequence avoids re-encoding unchanged image clipboards on every poll. */
internal interface ClipboardApi : StdCallLibrary { fun GetClipboardSequenceNumber(): Int }
internal object ClipboardSequence {
 private val api: ClipboardApi?=if(System.getProperty("os.name").startsWith("Windows")) runCatching { Native.load("user32",ClipboardApi::class.java) }.getOrNull() else null
 fun current(): Int?=api?.GetClipboardSequenceNumber()
}
