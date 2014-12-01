package com.shuneault.netrunnerdeckbuilder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.helper.CardImagesDownloader;
import com.shuneault.netrunnerdeckbuilder.prefs.SetNamesPreferenceMultiSelect;

import java.io.File;

public class SettingsActivity extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_DISPLAY_ALL_DATA_PACKS = "pref_DataPacksShowAll";
    public static final String KEY_PREF_DATA_PACKS_TO_DISPLAY = "pref_DataPacks";
    public static final String KEY_PREF_AMOUNT_OF_CORE_DECKS = "pref_AmountOfCoreDecks";
    public static final String KEY_PREF_DISPLAY_SET_NAMES_WITH_CARDS = "pref_ShowSetNames";
    public static final String KEY_PREF_TAP_TO_CLOSE_CARD_PREVIEW = "pref_TapToCloseCardPreview";
    public static final String KEY_PREF_CLEAR_CACHE = "pref_ClearCache";
    public static final String KEY_PREF_DOWNLOAD_ALL_IMAGES = "pref_DownloadAllImages";

    private String mInitialPacksToDisplay;

    // Preferences
    Preference prefDataPacks;
    Preference prefAmountOfCoreDecks;
    Preference prefClearCache;
    Preference prefDownloadAllImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // Preferences
        prefDataPacks = findPreference(KEY_PREF_DATA_PACKS_TO_DISPLAY);
        prefAmountOfCoreDecks = findPreference(KEY_PREF_AMOUNT_OF_CORE_DECKS);
        prefClearCache = findPreference(KEY_PREF_CLEAR_CACHE);
        prefDownloadAllImages = findPreference(KEY_PREF_DOWNLOAD_ALL_IMAGES);

        // Listeners
        prefDataPacks.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().isEmpty()) {
                    Toast.makeText(SettingsActivity.this, R.string.toast_require_one_datapack, Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    return true;
                }
            }
        });
        prefClearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle(R.string.clear_cache);
                builder.setMessage(getString(R.string.message_clear_cache));
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {                    }
                });
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCache(SettingsActivity.this);
                    }
                });
                builder.show();
                return false;
            }
        });
        prefDownloadAllImages.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CardImagesDownloader cardDownloader = new CardImagesDownloader(SettingsActivity.this, new CardImagesDownloader.CardImagesDownloaderListener() {
                    @Override
                    public void onBeforeStartTask(Context context, int max) {
                    }

                    @Override
                    public void onTaskCompleted() {
                    }

                    @Override
                    public void onImageDownloaded(Card card, int count, int max) {
                    }
                });
                cardDownloader.execute();
                return false;
            }
        });

        // Initial preferences
        mInitialPacksToDisplay = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_PREF_DATA_PACKS_TO_DISPLAY, "");

        // Display the summary for data packs to display
        refreshPrefsSummaries();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        if (key.equals(KEY_PREF_DATA_PACKS_TO_DISPLAY)) {
            // If zero is selected, cancel commit
            if (SetNamesPreferenceMultiSelect.parseStoredValue(sharedPreferences.getString(key, "")) == null) {
                sharedPreferences.edit().putString(key, mInitialPacksToDisplay).apply();
            }
            // change the summary to display the datapacks to use
            mInitialPacksToDisplay = sharedPreferences.getString(key, "");
            refreshPrefsSummaries();
        } else if (key.equals(KEY_PREF_DISPLAY_ALL_DATA_PACKS)) {
            refreshPrefsSummaries();
        } else if (key.equals(KEY_PREF_AMOUNT_OF_CORE_DECKS)) {
            refreshPrefsSummaries();
        }

    }

    private void refreshPrefsSummaries() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Packs
        String packs[] = SetNamesPreferenceMultiSelect.parseStoredValue(sharedPreferences.getString(KEY_PREF_DATA_PACKS_TO_DISPLAY, ""));
        if (packs != null) {
            prefDataPacks.setSummary(TextUtils.join(", ", packs));
        }

        // Amount of core decks
        prefAmountOfCoreDecks.setSummary(sharedPreferences.getString(KEY_PREF_AMOUNT_OF_CORE_DECKS, "1"));
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
