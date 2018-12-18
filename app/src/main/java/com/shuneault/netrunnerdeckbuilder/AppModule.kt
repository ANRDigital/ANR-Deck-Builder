package com.shuneault.netrunnerdeckbuilder

import android.app.Application
import com.shuneault.netrunnerdeckbuilder.ViewModel.BrowseCardsViewModel
import com.shuneault.netrunnerdeckbuilder.ViewModel.DeckActivityViewModel
import com.shuneault.netrunnerdeckbuilder.ViewModel.MainActivityViewModel
import com.shuneault.netrunnerdeckbuilder.db.*
import com.shuneault.netrunnerdeckbuilder.helper.ISettingsProvider
import com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper
import com.shuneault.netrunnerdeckbuilder.helper.SettingsProvider
import org.koin.android.ext.android.startKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.experimental.builder.viewModel
import org.koin.dsl.module.module

val appModule = module {

    single<IDeckRepository> { DeckRepository(get(), get()) }
    single<ISettingsProvider> { SettingsProvider(androidContext()) }

    single { CardRepository(androidContext(), get(), get()) }
    single { DatabaseHelper(androidContext()) }

    factory { JSONDataLoader(get()) }
    factory { LocalFileHelper(androidContext()) }

    viewModel<DeckActivityViewModel>()
    viewModel<MainActivityViewModel>()
    viewModel<BrowseCardsViewModel>()
}

open class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()

        startKoin(this, listOf(appModule))
    }
}