package com.shuneault.netrunnerdeckbuilder;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.shuneault.netrunnerdeckbuilder.helper.AppManager;
import com.shuneault.netrunnerdeckbuilder.prefs.ListPreferenceMultiSelect;
import com.shuneault.netrunnerdeckbuilder.prefs.SetNamesPreferenceMultiSelect;

public class SettingsActivity extends PreferenceActivity
	implements OnSharedPreferenceChangeListener {
	
	public static final String KEY_PREF_DISPLAY_ALL_DATA_PACKS = "pref_DataPacksShowAll";
	public static final String KEY_PREF_DATA_PACKS_TO_DISPLAY = "pref_DataPacks";
	
	private String mInitialPacksToDisplay;
	
	// Preferences
	Preference prefDataPacks;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		
		// Preferences
		prefDataPacks = findPreference(KEY_PREF_DATA_PACKS_TO_DISPLAY);
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

		}
		
	}
	
	private void refreshPrefsSummaries() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		String packs[] = SetNamesPreferenceMultiSelect.parseStoredValue(sharedPreferences.getString(KEY_PREF_DATA_PACKS_TO_DISPLAY, ""));
		if (packs != null) {
			prefDataPacks.setSummary(TextUtils.join(", ", packs));
		}
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		super.onBackPressed();
	}

}
