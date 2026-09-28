package com.zunawet.clipvault.di
import com.zunawet.clipvault.data.repository.VaultRepository
import com.zunawet.clipvault.data.ocr.OcrService
import com.zunawet.clipvault.ui.panel.PanelViewModel
import kotlinx.coroutines.*
import org.koin.dsl.module
val appModule=module {
 single { CoroutineScope(SupervisorJob()+Dispatchers.Main) }
 single { VaultRepository() }
 single { OcrService() }
 single { PanelViewModel(get(),get()) }
}
