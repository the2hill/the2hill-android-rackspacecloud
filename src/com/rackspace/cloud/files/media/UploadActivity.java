package com.rackspace.cloud.files.media;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
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
import com.rackspacecloud.android.ListContainerActivity;
import com.rackspacecloud.android.Preferences;
import com.rackspacecloud.android.R;

public class UploadActivity extends Activity implements View.OnClickListener,
		OnEditorActionListener {

	private static final String OPT_USERNAME = "username";
	private static final String OPT_USERNAME_DEF = "";
	private static final String OPT_API_KEY = "apiKey";
	private static final String OPT_API_KEY_DEF = "";

	private static final int SHOW_PREFERENCES = 1;

	private static final int uploadFile = 0;

	private boolean authenticating;
	private Uri objectUri;
	public Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		objectUri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
		context = getApplicationContext();

		final CheckBox show_clear = (CheckBox) findViewById(R.id.show_clear);
		final EditText loginApiKey = (EditText) findViewById(R.id.login_apikey);

		show_clear.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (((CheckBox) v).isChecked()) {
					loginApiKey
							.setTransformationMethod(new SingleLineTransformationMethod());
				} else {
					loginApiKey
							.setTransformationMethod(new PasswordTransformationMethod());
				}
				loginApiKey.requestFocus();
			}
		});

		((Button) findViewById(R.id.button)).setOnClickListener(this);

		loginApiKey.setOnEditorActionListener(this);
		// TODO: this needs to go in a separate class.
		loadLoginPreferences();
		restoreState(savedInstanceState);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("authenticating", authenticating);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem settings = menu.add(0, SHOW_PREFERENCES, 0,
				R.string.preference_name);
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
		if (state != null && state.containsKey("authenticating")
				&& state.getBoolean("authenticating")) {
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
				Preferences.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		String resultType = prefs.getString(Preferences.PREF_KEY_RESULTS_TYPE,
				String.valueOf(Preferences.COUNTRY_US));
		int resultTypeInt = Integer.parseInt(resultType);

		// Default Auth Server
		String authServer = Preferences.COUNTRY_US_AUTH_SERVER;
		if (resultTypeInt == Preferences.COUNTRY_UK)
			authServer = Preferences.COUNTRY_UK_AUTH_SERVER;

		String customAuthServer = prefs.getString(
				Preferences.PREF_KEY_AUTH_SERVER, "http://");
		if (!customAuthServer.equals("http://"))
			authServer = customAuthServer;

		Log.d("RackSpace-Cloud", "Using AuthServer: " + authServer);

		String username = ((EditText) findViewById(R.id.login_username))
				.getText().toString();
		String apiKey = ((EditText) findViewById(R.id.login_apikey)).getText()
				.toString();
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
			}
		});
		alert.show();
		hideActivityIndicators();
	}

	private boolean hasValidInput() {
		String username = ((EditText) findViewById(R.id.login_username))
				.getText().toString();
		String apiKey = ((EditText) findViewById(R.id.login_apikey)).getText()
				.toString();
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//			if (resultCode == RESULT_OK) {
//				container = (Container) this.getIntent().getExtras()
//						.get("conainter");
//				new UploadObjectTask().execute((Void[]) null);
//			}
		}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case uploadFile:
			return new AlertDialog.Builder(UploadActivity.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("Uploading...")
					.setMessage("Authenticated, Please choose which container to upload the file to.")
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									Intent myIntent = new Intent(getBaseContext(),
											ListContainerActivity.class);
									myIntent.putExtra("image", objectUri);
									startActivity(myIntent);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked Cancel so do some stuff
								}
							}).create();
		}
		return null;

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
				// Upload the file here.......
				showDialog(uploadFile);

			} else {
				showAlert("Login Failure",
						"Authentication failed.  Please check your User Name and API Key.");
			}
			hideActivityIndicators();
		}
	}

	
}