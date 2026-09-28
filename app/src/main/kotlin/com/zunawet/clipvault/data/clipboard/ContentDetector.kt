package com.zunawet.clipvault.data.clipboard
import com.zunawet.clipvault.domain.model.ClipType
object ContentDetector {
 fun detect(text: String): ClipType {
  val t = text.trim()
  val digits = t.filter(Char::isDigit)
  return when {
   Regex("(?i)(password|pass:|pwd:|api[_-]?key|secret[=:])").containsMatchIn(t) -> ClipType.SENSITIVE
   Regex("[\\d -]{13,25}").matches(t) && digits.length in 13..19 && luhn(digits) -> ClipType.SENSITIVE
   Regex("#[0-9a-fA-F]{3}([0-9a-fA-F]{1}|[0-9a-fA-F]{3}|[0-9a-fA-F]{5})?").matches(t) -> ClipType.COLOR
   Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+").matches(t) -> ClipType.EMAIL
   Regex("(?i)(https?://|www\\.)\\S+|[\\w.-]+\\.(com|org|net|io|dev|bd)(/\\S*)?").matches(t) -> ClipType.LINK
   Regex("[+()\\d .-]{7,25}").matches(t) && digits.length in 7..15 -> ClipType.PHONE
   t.contains('\n') && Regex("(?i)\\b(street|road|avenue|lane|block)\\b").containsMatchIn(t) -> ClipType.ADDRESS
   Regex("\\b(function|class|def|fun)\\b|[{};]").containsMatchIn(t) -> ClipType.CODE
   else -> ClipType.TEXT
  }
 }
 fun luhn(s: String): Boolean = s.reversed().mapIndexed { i,c ->
  val n=c.digitToInt(); if(i%2==1) (n*2).let { if(it>9) it-9 else it } else n
 }.sum()%10==0
}
class DuplicateFilter {
 private var last = ""; private var at = 0L
 @Synchronized fun accept(hash: String, now: Long = System.currentTimeMillis()): Boolean {
  val ok = hash != last || now-at >= 200; last=hash; at=now; return ok
 }
}
