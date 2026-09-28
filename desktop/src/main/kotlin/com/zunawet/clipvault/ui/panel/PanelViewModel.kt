package com.zunawet.clipvault.ui.panel
import com.zunawet.clipvault.data.repository.VaultRepository
import com.zunawet.clipvault.domain.usecase.SearchClipsUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
@OptIn(FlowPreview::class,ExperimentalCoroutinesApi::class)
class PanelViewModel(val repo: VaultRepository,scope: CoroutineScope) {
 val query=MutableStateFlow("")
 val category=MutableStateFlow("Recent")
 val selected=MutableStateFlow(0)
 val limit=MutableStateFlow(50)
 val results=combine(repo.clips,query.debounce(50),category) { c,q,t -> Triple(c,q,t) }
  .mapLatest { (c,q,t) -> withContext(Dispatchers.Default) { SearchClipsUseCase().execute(c,q,t) } }
  .onEach { selected.value=0;limit.value=50 }.stateIn(scope,SharingStarted.Eagerly,emptyList())
 fun move(delta: Int) { selected.value=(selected.value+delta).coerceIn(0,(results.value.size.coerceAtMost(limit.value)-1).coerceAtLeast(0)) }
 fun reset() { query.value="";category.value="Recent";selected.value=0;limit.value=50 }
}
