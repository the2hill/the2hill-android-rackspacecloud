package com.rackspacecloud.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.rackspace.cloud.servers.api.client.Account;
import com.rackspace.cloud.servers.api.client.http.Authentication;


public class RackspaceCloudActivity extends Activity implements View.OnClickListener, OnEditorActionListener {
	
	private static final String OPT_USERNAME = "username";
	private static final String OPT_USERNAME_DEF = "";
	private static final String OPT_API_KEY = "apiKey";
	private static final String OPT_API_KEY_DEF = "";

	private static final int SHOW_PREFERENCES = 1;

	private Intent tabViewIntent;
	private boolean authenticating;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final CheckBox show_clear = (CheckBox) findViewById(R.id.show_clear);
        final EditText loginApiKey = (EditText) findViewById(R.id.login_apikey);

        show_clear.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		        if (((CheckBox) v).isChecked()) {
		        	loginApiKey.setTransformationMethod(new SingleLineTransformationMethod());
		        } else {
		        	loginApiKey.setTransformationMethod(new PasswordTransformationMethod());	
		        }
		        loginApiKey.requestFocus();
		    }	
		});
        
        ((Button) findViewById(R.id.button)).setOnClickListener(this);
        
		loginApiKey.setOnEditorActionListener(this);
        loadLoginPreferences();
        restoreState(savedInstanceState);
        
        // use the TabViewActivity when Cloud Files is added
        // tabViewIntent = new Intent(this, TabViewActivity.class);
        
        tabViewIntent = new Intent(this, TabViewActivity.class);
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("authenticating", authenticating);
	}

    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuItem settings = menu.add(0, SHOW_PREFERENCES, 0, R.string.preference_name);
    	settings.setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }
	
    public boolean onOptionsItemSelected(MenuItem item) {
        
    	switch (item.getItemId()) {
    		case SHOW_PREFERENCES:
    			showPreferences();
    			break;
			}	
    	return true;
    }

    public void showPreferences() {
        Intent settingsActivity = new Intent(getBaseContext(),
                Preferences.class);
        startActivity(settingsActivity);
    }
    
    private void restoreState(Bundle state) {
    	if (state != null && state.containsKey("authenticating") && state.getBoolean("authenticating")) {
    		showActivityIndicators();
    	} else {
    		hideActivityIndicators();
    	}
    }
    
    public void login() {
    	if (hasValidInput()) {
        	showActivityIndicators();
        	setLoginPreferences();
        	new AuthenticateTask().execute((Void[]) null);
    	} else {
    		showAlert("Fields Missing", "User Name and API Key are required.");
    	}
    }
    
    public void onClick(View view) {
    	login();
    }
    
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		login();
		return false;
	}    

	private void loadLoginPreferences() {
    	SharedPreferences sp = this.getPreferences(Context.MODE_PRIVATE);
    	String username = sp.getString(OPT_USERNAME, OPT_USERNAME_DEF);    	
    	String apiKey = sp.getString(OPT_API_KEY, OPT_API_KEY_DEF);
    	EditText usernameText = (EditText) findViewById(R.id.login_username);
    	usernameText.setText(username);
    	EditText apiKeyText = (EditText) findViewById(R.id.login_apikey);
    	apiKeyText.setText(apiKey);
    }
    
    private void setLoginPreferences() {
        SharedPreferences prefs = getSharedPreferences(
                Preferences.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        String resultType = prefs.getString(
                Preferences.PREF_KEY_RESULTS_TYPE,
                String.valueOf(Preferences.COUNTRY_US));
        int resultTypeInt = Integer.parseInt(resultType);
        
        
        //Default Auth Server
        String authServer = Preferences.COUNTRY_US_AUTH_SERVER; 
        if (resultTypeInt == Preferences.COUNTRY_UK)
        	authServer = Preferences.COUNTRY_UK_AUTH_SERVER;
        
        String customAuthServer = prefs.getString(Preferences.PREF_KEY_AUTH_SERVER, "http://");
        if (!customAuthServer.equals("http://"))
        	authServer = customAuthServer;
        
        Log.d("RackSpace-Cloud", "Using AuthServer: " + authServer);
        
    	String username = ((EditText) findViewById(R.id.login_username)).getText().toString();
    	String apiKey = ((EditText) findViewById(R.id.login_apikey)).getText().toString();
    	Account.setUsername(username);
    	Account.setApiKey(apiKey);
    	Account.setAuthServer(authServer);
    	
    	Editor e = this.getPreferences(Context.MODE_PRIVATE).edit();
    	e.putString(OPT_USERNAME, username);
    	e.putString(OPT_API_KEY, apiKey);
    	e.commit();        	
    }
    
    private void showAlert(String title, String message) {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setButton("OK", new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	        return;
	    } }); 
		alert.show();
		hideActivityIndicators();
    }
    
    private boolean hasValidInput() {
    	String username = ((EditText) findViewById(R.id.login_username)).getText().toString();
    	String apiKey = ((EditText) findViewById(R.id.login_apikey)).getText().toString();
    	return !"".equals(username) && !"".equals(apiKey);
    }

    private void setActivityIndicatorsVisibility(int visibility) {
        ProgressBar pb = (ProgressBar) findViewById(R.id.login_progress_bar);
    	TextView tv = (TextView) findViewById(R.id.login_authenticating_label);
        pb.setVisibility(visibility);
        tv.setVisibility(visibility);
    }

    private void showActivityIndicators() {
    	setActivityIndicatorsVisibility(View.VISIBLE);
    }
    
    private void hideActivityIndicators() {
    	setActivityIndicatorsVisibility(View.INVISIBLE);
    }
    
    private class AuthenticateTask extends AsyncTask<Void, Void, Boolean> {
    	
		@Override
		protected Boolean doInBackground(Void... arg0) {
			authenticating = true;
			return new Boolean(Authentication.authenticate());
		}
    	
		@Override
		protected void onPostExecute(Boolean result) {
			authenticating = false;
			if (result.booleanValue()) {
				startActivity(tabViewIntent);
//	        	new LoadImagesTask().execute((Void[]) null);				
			} else {
				showAlert("Login Failure", "Authentication failed.  Please check your User Name and API Key.");
			}
			hideActivityIndicators();
		}
    }

//    private class LoadFlavorsTask extends AsyncTask<Void, Void, ArrayList<Flavor>> {
//    	
//		@Override
//		protected ArrayList<Flavor> doInBackground(Void... arg0) {
//			return (new FlavorManager()).createList(true);
//		}
//    	
//		@Override
//		protected void onPostExecute(ArrayList<Flavor> result) {
//			if (result != null && result.size() > 0) {
//				TreeMap<String, Flavor> flavorMap = new TreeMap<String, Flavor>();
//				for (int i = 0; i < result.size(); i++) {
//					Flavor flavor = result.get(i);
//					flavorMap.put(flavor.getId(), flavor);
//				}
//				Flavor.setFlavors(flavorMap);
//				startActivity(tabViewIntent);
//			} else {
//				showAlert("Login Failure", "There was a problem loading server flavors.  Please try again.");
//			}
//			hideActivityIndicators();
//		}
//    }
//
//    private class LoadImagesTask extends AsyncTask<Void, Void, ArrayList<Image>> {
//    	
//		@Override
//		protected ArrayList<Image> doInBackground(Void... arg0) {
//			return (new ImageManager()).createList(true);
//		}
//    	
//		@Override
//		protected void onPostExecute(ArrayList<Image> result) {
//			if (result != null && result.size() > 0) {
//				TreeMap<String, Image> imageMap = new TreeMap<String, Image>();
//				for (int i = 0; i < result.size(); i++) {
//					Image image = result.get(i);
//					imageMap.put(image.getId(), image);
//				}
//				Image.setImages(imageMap);
//				new LoadFlavorsTask().execute((Void[]) null);
//				//startActivity(tabViewIntent);
//			} else {
//				showAlert("Login Failure", "There was a problem loading server images.  Please try again.");
//			}
//			//hideActivityIndicators();
//		}
//    }

}