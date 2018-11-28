package com.shuneault.netrunnerdeckbuilder.helper;

import android.support.annotation.NonNull;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;

public interface ISettingsProvider {
    @NonNull
    CardRepository.CardRepositoryPreferences getCardRepositoryPreferences();

    String getLanguagePref();
}
