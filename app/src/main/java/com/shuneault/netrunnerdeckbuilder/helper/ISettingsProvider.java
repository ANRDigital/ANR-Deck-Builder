package com.shuneault.netrunnerdeckbuilder.helper;

import androidx.annotation.NonNull;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;

public interface ISettingsProvider {
    @NonNull
    CardRepository.CardRepositoryPreferences getCardRepositoryPreferences();

    String getLanguagePref();

    int getDefaultFormatId();
}
