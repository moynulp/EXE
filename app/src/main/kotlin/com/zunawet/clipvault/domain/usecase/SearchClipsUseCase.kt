package com.zunawet.clipvault.domain.usecase
import com.zunawet.clipvault.domain.model.Clip
class SearchClipsUseCase {
 fun execute(clips: List<Clip>, query: String, category: String): List<Clip> {
  val q=query.trim().lowercase()
  return clips.asSequence().filter {
   when(category) { "Pinned" -> it.pinned; "Text" -> it.type.name in setOf("TEXT","EMAIL","PHONE","ADDRESS"); "Code" -> it.type.name=="CODE"; "Links" -> it.type.name=="LINK"; "Images" -> it.type.name=="IMAGE"; else -> true }
  }.filter { c ->
   if(q.isEmpty()) true else {
    val text=(c.content+" "+(c.ocr ?: "")).lowercase()
    text.contains(q) || (q.length in 3..32 && text.split(Regex("\\W+")).any { near(it,q) })
   }
  }.toList()
 }
 private fun near(a: String,b: String): Boolean {
  if(kotlin.math.abs(a.length-b.length)>1) return false
  var prev=IntArray(b.length+1) { it }
  for(i in a.indices) {
   val cur=IntArray(b.length+1);cur[0]=i+1
   for(j in b.indices) cur[j+1]=minOf(cur[j]+1, prev[j+1]+1, prev[j]+if(a[i]==b[j]) 0 else 1)
   prev=cur
  }
  return prev.last()<=1
 }
}
