package com.tortel.notifier;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class Settings extends PreferenceActivity {
	
	public void onCreate(Bundle savedState){
		super.onCreate(savedState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
