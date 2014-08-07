package com.shuneault.netrunnerdeckbuilder.prefs;

import android.content.Context;
import android.util.AttributeSet;

import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

public class SetNamesPreferenceMultiSelect extends ListPreferenceMultiSelect {

	public SetNamesPreferenceMultiSelect(Context context) {
		super(context);
	}

	public SetNamesPreferenceMultiSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public CharSequence[] getEntries() {
		return AppManager.getInstance().getSetNames().toArray(new CharSequence[AppManager.getInstance().getSetNames().size()]);
	}
	
	@Override
	public CharSequence[] getEntryValues() {
		return getEntries();
	}
	
	

}
