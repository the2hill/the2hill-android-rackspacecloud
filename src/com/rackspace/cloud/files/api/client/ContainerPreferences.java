package com.rackspace.cloud.files.api.client;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.rackspacecloud.android.R;

public class ContainerPreferences extends PreferenceActivity{
    // The name of the SharedPreferences file we'll store preferences in.
    public static final String SHARED_CONTAINER_PREFERENCES_NAME = "ContainerPreferences";
    
    //The Email used to send CDN information
    public static final String PREF_USER_EMAIL = "userEmailPref";
    
    //Are we sending logs
    public static final String PREF_SEND_CDN_EMAIL = "sendEmail";

    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SHARED_CONTAINER_PREFERENCES_NAME);
		addPreferencesFromResource(R.layout.container_preferences);
	}
}
