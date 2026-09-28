package com.zunawet.clipvault
import kotlin.test.*
import com.zunawet.clipvault.data.clipboard.*
import com.zunawet.clipvault.domain.model.*
import com.zunawet.clipvault.domain.usecase.SearchClipsUseCase
class CoreTest {
 @Test fun detection() {
  assertEquals(ClipType.COLOR,ContentDetector.detect("#FBBF24"))
  assertEquals(ClipType.LINK,ContentDetector.detect("https://zunawet.com"))
  assertEquals(ClipType.EMAIL,ContentDetector.detect("hello@example.com"))
  assertEquals(ClipType.SENSITIVE,ContentDetector.detect("4111 1111 1111 1111"))
  assertEquals(ClipType.SENSITIVE,ContentDetector.detect("password: hidden"))
  assertEquals(ClipType.CODE,ContentDetector.detect("fun main() {}"))
 }
 @Test fun duplicates() { val d=DuplicateFilter();assertTrue(d.accept("a",1000));assertFalse(d.accept("a",1100));assertTrue(d.accept("b",1150));assertTrue(d.accept("b",1400)) }
 @Test fun search() {
  val c=Clip("1",ClipType.TEXT,"hello world","hello world","x",0)
  val s=SearchClipsUseCase()
  assertEquals(1,s.execute(listOf(c),"helo","Recent").size)
  assertEquals(0,s.execute(listOf(c),"hello","Pinned").size)
 }
}
