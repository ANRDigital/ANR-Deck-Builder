package com.shuneault.netrunnerdeckbuilder

import android.app.Application
import com.shuneault.netrunnerdeckbuilder.ViewModel.BrowseCardsViewModel
import com.shuneault.netrunnerdeckbuilder.ViewModel.DeckActivityViewModel
import com.shuneault.netrunnerdeckbuilder.ViewModel.FullScreenViewModel
import com.shuneault.netrunnerdeckbuilder.ViewModel.MainActivityViewModel
import com.shuneault.netrunnerdeckbuilder.db.*
import com.shuneault.netrunnerdeckbuilder.fragments.nrdb.NrdbFragmentViewModel
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider
import com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper
import com.shuneault.netrunnerdeckbuilder.helper.SettingsProvider
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

val appModule = module {

    single<IDeckRepository> { DeckRepository(get(), get()) }
    single<ISettingsProvider> { SettingsProvider(androidContext()) }

    single { CardRepository(androidContext(), get(), get()) }
    single { DatabaseHelper(androidContext()) }

    factory { JSONDataLoader(get()) }
    factory { LocalFileHelper(androidContext()) }

    viewModel { DeckActivityViewModel(get(), get(), get()) }
    viewModel { BrowseCardsViewModel(get(), get()) }
    viewModel { MainActivityViewModel (get(), get()) }
    viewModel { FullScreenViewModel(get(), get())}
    viewModel { NrdbFragmentViewModel(get(), get()) }
}

open class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}