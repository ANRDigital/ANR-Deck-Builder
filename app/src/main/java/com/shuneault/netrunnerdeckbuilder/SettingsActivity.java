package com.shuneault.netrunnerdeckbuilder;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import androidx.core.content.FileProvider;
import kotlin.Lazy;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.ArraySet;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.db.CardRepository;
import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.game.Deck;
import com.shuneault.netrunnerdeckbuilder.game.Format;
import com.shuneault.netrunnerdeckbuilder.game.NetRunnerBD;
import com.shuneault.netrunnerdeckbuilder.game.Pack;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.helper.CardImagesDownloader;
import com.shuneault.netrunnerdeckbuilder.helper.StringDownloader;
import com.shuneault.netrunnerdeckbuilder.prefs.SetNamesPreferenceMultiSelect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.shuneault.netrunnerdeckbuilder.helper.LocalFileHelper.FILE_CARDS_JSON;
import static org.koin.java.standalone.KoinJavaComponent.inject;

public class SettingsActivity extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

//    public static final String KEY_PREF_DISPLAY_ALL_DATA_PACKS = "pref_DataPacksShowAll";
//    public static final String KEY_PREF_DATA_PACKS_TO_DISPLAY = "pref_DataPacks";
//    public static final String KEY_PREF_AMOUNT_OF_CORE_DECKS = "pref_AmountOfCoreDecks";
    public static final String KEY_PREF_DISPLAY_SET_NAMES_WITH_CARDS = "pref_ShowSetNames";
    public static final String KEY_PREF_CLEAR_CACHE = "pref_ClearCache";
    public static final String KEY_PREF_DOWNLOAD_ALL_IMAGES = "pref_DownloadAllImages";
    public static final String KEY_PREF_LANGUAGE = "pref_Language";
    public static final String KEY_PREF_USE_MOST_WANTED_LIST = "pref_MostWantedList";
    public static final String KEY_PREF_EXPORT_ALL_DECKS = "pref_ExportAllDecks";
    public static final String KEY_PREF_ABOUT = "pref_About";
    public static final String KEY_PREF_DEFAULT_FORMAT = "pref_Format";
    public static final String DEFAULT_CORE_DECKS = "1";
    public static final String KEY_PREF_COLLECTION = "pref_collection";

    private String mInitialPacksToDisplay;

    // Preferences
//    Preference prefDataPacks;
//    Preference prefAmountOfCoreDecks;
    MultiSelectListPreference prefCollection;
    Preference prefClearCache;
    Preference prefDownloadAllImages;
    Preference prefLanguage;
    Preference prefExportDecks;
    Preference prefAbout;
    ListPreference prefDefFormat;

    Lazy<CardRepository> repo = inject(CardRepository.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

//        prefAmountOfCoreDecks = findPreference(KEY_PREF_AMOUNT_OF_CORE_DECKS);
        prefLanguage = findPreference(KEY_PREF_LANGUAGE);
        prefAbout = findPreference(KEY_PREF_ABOUT);
        prefAbout.setOnPreferenceClickListener(preference -> {
            showAboutDialog();
            return false;
        });

//        prefDataPacks = findPreference(KEY_PREF_DATA_PACKS_TO_DISPLAY);
//        prefDataPacks.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                if (newValue.toString().isEmpty()) {
//                    Toast.makeText(SettingsActivity.this, R.string.toast_require_one_datapack, Toast.LENGTH_LONG).show();
//                    return false;
//                } else {
//                    return true;
//                }
//            }
//        });
        prefClearCache = findPreference(KEY_PREF_CLEAR_CACHE);
        prefClearCache.setOnPreferenceClickListener(preference -> {
            doClearCache();
            return false;
        });
        prefDownloadAllImages = findPreference(KEY_PREF_DOWNLOAD_ALL_IMAGES);
        prefDownloadAllImages.setOnPreferenceClickListener(preference -> {
            doDownloadAllImages();
            return false;
        });
        prefExportDecks = findPreference(KEY_PREF_EXPORT_ALL_DECKS);
        prefExportDecks.setOnPreferenceClickListener(preference -> {
            doExportAllDecks();
            return false;
        });

        // Collection
        prefCollection = (MultiSelectListPreference) findPreference(KEY_PREF_COLLECTION);
        CardRepository repo = AppManager.getInstance().getCardRepository();
        ArrayList<Pack> packs = repo.getPacks(true);
        CharSequence[] packNames = new CharSequence[packs.size()];
        CharSequence[] packCodes = new CharSequence[packs.size()];
        for (int i = 0; i < packs.size(); i++) {
            packNames[i] = packs.get(i).getName();
            packCodes[i] = packs.get(i).getCode();
        }

        prefCollection.setEntries(packNames);
        prefCollection.setEntryValues(packCodes);

        // Format
        prefDefFormat = (ListPreference)findPreference(KEY_PREF_DEFAULT_FORMAT);
        ArrayList<Format> formats = repo.getFormats();
        CharSequence[] formatNames = new CharSequence[formats.size()];
        CharSequence[] formatIds = new CharSequence[formats.size()];
        for (int i = 0; i < formats.size(); i++) {
            formatNames[i] = formats.get(i).getName();
            formatIds[i] = String.valueOf(formats.get(i).getId());
        }
        prefDefFormat.setEntries(formatNames);
        prefDefFormat.setEntryValues(formatIds);

        // Display the summary for data packs to display
        refreshPrefsSummaries();
    }

    private void showAboutDialog() {
        PackageInfo pInfo;
        TextView txt = new TextView(this);
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            txt.setText(getString(R.string.about_text, pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        txt.setMovementMethod(LinkMovementMethod.getInstance());
        txt.setPadding(25, 25, 25, 25);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle(R.string.menu_about);
        builder2.setView(txt);
        builder2.setPositiveButton(R.string.ok, null);
        builder2.show();
    }

    private void doClearCache() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle(R.string.clear_cache);
        builder.setMessage(getString(R.string.message_clear_cache));
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCache(SettingsActivity.this);
            }
        });
        builder.show();
    }

    private void doDownloadAllImages() {
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
    }

    private void doExportAllDecks() {
        JSONArray jsonArray = new JSONArray();
        for (Deck deck : AppManager.getInstance().getAllDecks()) {
            JSONObject jsonDeck = deck.toJSON();
            jsonArray.put(jsonDeck);
        }
        String filename = "netrunner_decks.anrdecks";
        // Save the file as OCTGN format
        try {
            FileOutputStream fileOut = openFileOutput(filename, Context.MODE_PRIVATE);
            fileOut.write(jsonArray.toString().getBytes());
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("LOGCAT", String.valueOf(getFileStreamPath(filename)));

        // Create the send intent
        Intent intentEmail = new Intent(Intent.ACTION_SEND);
        intentEmail.setType("text/plain");
        intentEmail.putExtra(Intent.EXTRA_SUBJECT, "NetRunner Deck - All decks");
        intentEmail.putExtra(Intent.EXTRA_TEXT, "\r\n\r\nDownload Android Netrunner DeckBuilder for free at https://play.google.com/store/apps/details?id=com.shuneault.netrunnerdeckbuilder");

        File fileStreamPath = getFileStreamPath(filename);
        Uri fileUri = FileProvider.getUriForFile(getApplicationContext(),
                BuildConfig.APPLICATION_ID,
                fileStreamPath);

        intentEmail.putExtra(Intent.EXTRA_STREAM, fileUri);
        startActivityForResult(Intent.createChooser(intentEmail, getText(R.string.menu_share)), 0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        switch (key) {
//            case KEY_PREF_DATA_PACKS_TO_DISPLAY:
//                // If zero is selected, cancel commit
//                if (SetNamesPreferenceMultiSelect.parseStoredValue(sharedPreferences.getString(key, "")) == null) {
//                    sharedPreferences.edit().putString(key, mInitialPacksToDisplay).apply();
//                }
//                // change the summary to display the datapacks to use
//                mInitialPacksToDisplay = sharedPreferences.getString(key, "");
//                refreshPrefsSummaries();
//                break;
//            case KEY_PREF_DISPLAY_ALL_DATA_PACKS:
//            case KEY_PREF_AMOUNT_OF_CORE_DECKS:
//                refreshPrefsSummaries();
//                break;
            case KEY_PREF_LANGUAGE:
                doLanguageChange();
                break;
        }
    }

    private void doLanguageChange() {
        StringDownloader sd = new StringDownloader(this, String.format(NetRunnerBD.getAllCardsUrl()), FILE_CARDS_JSON, new StringDownloader.FileDownloaderListener() {
            ProgressDialog mDialog;

            @Override
            public void onBeforeTask() {
                // Display a progress dialog
                mDialog = new ProgressDialog(SettingsActivity.this);
                mDialog.setTitle(getString(R.string.downloading_cards));
                mDialog.setIndeterminate(true);
                mDialog.setCancelable(false);
                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mDialog.setMessage(getString(R.string.downloading_cards_restart));
                mDialog.show();
            }

            @Override
            public void onTaskComplete(String s) {
                // Close the dialog
                mDialog.dismiss();

                Context context = SettingsActivity.this;
                Intent mStartActivity = new Intent(context, MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }

            @Override
            public void onError(Exception e) {
                // Display the error and cancel the ongoing dialog
                mDialog.dismiss();

                // If zero cards are available, exit the application
                if (AppManager.getInstance().getCardRepository().hasCards()) {
                    Toast.makeText(SettingsActivity.this, R.string.error_downloading_cards_quit, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.error_downloading_cards, Toast.LENGTH_LONG).show();
                }

                // Log
                Log.e("LOG", e.getMessage());
            }
        });
        sd.execute();
    }

    private void refreshPrefsSummaries() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Set<String> stringSet = sharedPreferences.getStringSet(KEY_PREF_COLLECTION, new HashSet<String>());
        prefCollection.setSummary(stringSet.size() + " packs");

        String defaultFormat = sharedPreferences.getString(KEY_PREF_DEFAULT_FORMAT, String.valueOf(Format.FORMAT_STANDARD));
        int formatId = Integer.parseInt(defaultFormat);
        String formatSummary = repo.getValue().getFormat(formatId).getName();
        prefDefFormat.setSummary(formatSummary);
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
        } catch (Exception e) {
        }
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
