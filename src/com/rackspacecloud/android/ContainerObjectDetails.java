package com.rackspacecloud.android;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.rackspace.cloud.file.api.client.http.Exceptions;
import com.rackspace.cloud.files.api.client.ContainerManager;
import com.rackspace.cloud.files.api.client.ContainerObjectManager;
import com.rackspace.cloud.files.api.client.ContainerObjects;
import com.rackspace.cloud.files.api.client.ContainerPreferences;
import com.rackspace.cloud.files.api.client.CustomHttpClient;
import com.rackspace.cloud.servers.api.client.Account;
import com.rackspace.cloud.servers.api.client.CloudServersException;
import com.rackspacecloud.android.helpers.FileBuilder;

public class ContainerObjectDetails extends Activity {
	public String LOG = "ContainerObjectsDetailsActivity";
	
	private static final int deleteObject = 0;
	private static final int purgeCdn = 1;
	public static final int DOWNLOADING = 0;
	public static final int MAX_BUFFER_SIZE = 1024;
	public static final int COMPLETE = 1;
	private ContainerObjects object;
	private String containerName;
	private String cdnURL;
	private Boolean cdnEnabled;
	private int bConver = 1048576;
	private int kbConver = 1024;
	private double megaBytes;
	private double kiloBytes;
	public Button previewButton;
	public Context context;
	private String email;
	private boolean isLogEnabled;
	private Exceptions cfex;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		object = (ContainerObjects) this.getIntent().getExtras().get("container");
		containerName = (String) this.getIntent().getExtras().get("containerNames");
		cdnURL = (String) this.getIntent().getExtras().get("cdnUrl");
		cdnEnabled = (Boolean) this.getIntent().getExtras().getBoolean("isCdnEnabled");

		setContentView(R.layout.viewobject);
		loadPreferences();
		setupButtons();
		restoreState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("container", object);
	}

	private void restoreState(Bundle state) {
		if (state != null && state.containsKey("container")) {
			object = (ContainerObjects) state.getSerializable("container");
		}
		loadObjectData();
		if (cdnEnabled) {
			this.previewButton = (Button) findViewById(R.id.preview_button);
			previewButton.setOnClickListener(new MyOnClickListener());
		} else {
			this.previewButton = (Button) findViewById(R.id.preview_button);
			previewButton.setVisibility(View.GONE);
		}

	}

	private void loadPreferences() {
		SharedPreferences cPrefs = this.getSharedPreferences(
				ContainerPreferences.SHARED_CONTAINER_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences prefs = this.getSharedPreferences(
				Preferences.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		email = cPrefs.getString(ContainerPreferences.PREF_USER_EMAIL, String
				.valueOf(prefs.getString(Preferences.PREF_USER_EMAIL, null)));
		isLogEnabled = cPrefs.getBoolean(
				ContainerPreferences.PREF_SEND_CDN_EMAIL, false);
	}

	private void setupButton(int resourceId, OnClickListener onClickListener) {
		Button button = (Button) findViewById(resourceId);
		button.setOnClickListener(onClickListener);
	}

	private void setupButtons() {
		setupButton(R.id.view_file_button, new OnClickListener() {
			public void onClick(View v) {
				new DownloadFileTask().execute((Void[]) null);
			}
		});
	}

	private void loadObjectData() {
		// Object Name
		TextView name = (TextView) findViewById(R.id.view_container_name);
		name.setText(object.getCName().toString());
		// File size
		if (object.getBytes() >= bConver) {
			megaBytes = Math.abs(object.getBytes() / bConver + 0.2);
			TextView sublabel = (TextView) findViewById(R.id.view_file_bytes);
			sublabel.setText(megaBytes + " MB");
		} else if (object.getBytes() >= kbConver) {
			kiloBytes = Math.abs(object.getBytes() / kbConver + 0.2);
			TextView sublabel = (TextView) findViewById(R.id.view_file_bytes);
			sublabel.setText(kiloBytes + " KB");
		} else {
			TextView sublabel = (TextView) findViewById(R.id.view_file_bytes);
			sublabel.setText(object.getBytes() + " B");
		}

		// Content Type
		TextView cType = (TextView) findViewById(R.id.view_content_type);
		cType.setText(object.getContentType().toString());

		// Last Modification date
		String strDate = object.getLastMod();
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.ssssss");
		Date dateStr = null;
		try {
			dateStr = formatter.parse(strDate);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String formattedDate = formatter.format(dateStr);
		Date date1 = null;
		try {
			date1 = formatter.parse(formattedDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		formatter = new SimpleDateFormat("MMM-dd-yyyy");
		formattedDate = formatter.format(date1);
		TextView lastmod = (TextView) findViewById(R.id.view_file_modification);
		lastmod.setText(formattedDate);

	}

	//User wishes to view from the browser via CDN...
	private class MyOnClickListener implements View.OnClickListener {
		public void onClick(View v) {
			Intent viewIntent = new Intent("android.intent.action.VIEW",
					Uri.parse(cdnURL + "/" + object.getCName()));
			startActivity(viewIntent);

		}
	}

	private class ContainerObjectDeleteTask extends AsyncTask<Void, Void, HttpResponse> {
		private Exception exception;

		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;
			try {
				resp = (new ContainerObjectManager(context)).deleteObject(
						containerName, object.getCName());
				Log.v(LOG, "container name " + object.getCName() + " "
						+ containerName);
			} catch (Exception e) {
				exception = e;
			}
			return resp;
		}

		@Override
		protected void onPostExecute(HttpResponse response) {
			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 204) {
					setResult(Activity.RESULT_OK);
					finish();
				} else {
					if ("".equals(exception.getMessage())) {
						showAlert("Error",
								"There was a problem deleting your File.");
					} else {
						showAlert("Error",
								"There was a problem deleting your file: "
										+ exception.getMessage());
					}
				}
			} else if (exception != null) {
				showAlert("Error", "There was a problem deleting your file: "
						+ exception.getMessage());
			}
		}
	}
	
	private class DownloadFileTask extends AsyncTask<Void, Integer, File> {
		ProgressDialog mProgressDialog = new ProgressDialog(ContainerObjectDetails.this);

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.setMessage("Processing...");
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.show();
	        Log.d("UploadObjectTask", "onPreExecute()");
		}

		@Override
		  protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		    mProgressDialog.setProgress(values[0]);
		  }

		protected File doInBackground(Void... arg0) {
			String storageUri = Account.getStorageUrl() + "/" + containerName + "/" + object.getCName();
			File file = null;
			File cFile = null;
			BufferedInputStream bis;
			OutputStream fOut;
			
			//Check against these
			file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RackSpaceCloud/" + object.getCName());
			cFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RackSpaceCloud/" +  "compressed_" + object.getCName());
			//TODO:Need to format the log names...
			if (object.getCName().contains("/")) {
				return null;
			}
			
			if (!file.exists() && !cFile.exists()) {
				Log.v(LOG, "Connect to to users storage container... Begin transaction");
					HttpGet httpRequest = new HttpGet(storageUri);
					HttpClient httpclient = new CustomHttpClient(context);
					httpRequest.addHeader("X-Auth-Token", Account.getAuthToken());
			
				try {
					Log.v(LOG, "container name " + object.getCName() + " " + containerName);
					file = FileBuilder.createFile(object.getCName());
					HttpResponse response = httpclient.execute(httpRequest);
				    bis = new BufferedInputStream(response.getEntity().getContent());
		            long lenghtOfFile = response.getEntity().getContentLength();

					fOut = new FileOutputStream(file);
					byte buffer[];
					long total = 0;
					buffer = new byte[MAX_BUFFER_SIZE];
					int read;
					while ((read = bis.read(buffer)) != -1) {
						total += read;
						this.publishProgress(((int)((total*100)/lenghtOfFile)));
						fOut.write(buffer, 0, read);
					}
					bis.close();
					fOut.close();
					//Remove, we'll create it later
					cFile.delete();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (cFile.exists()) {
				return cFile;
			}
			return file;
		}

		@Override
		protected void onPostExecute(File file) {
			mProgressDialog.dismiss();
			try {
				playDownloadedVideo(file);
			} catch (IllegalArgumentException e) {
				Log.e(LOG,"Illegal argument exception.");
			} catch (OutOfMemoryError e) {
				Log.e(LOG, "Out of memory error :(");
			}
		}
	}
	
	public void playDownloadedVideo(File file) {
		if (file != null) {
			String path = file.getAbsolutePath();
			//TODO:send audio to proper player..
			if (path.contains("m4v") || path.contains("vid") || path.contains("mp3") || path.contains("3gp")) {
				File videoFile2Play = new File(file.getAbsolutePath());
				Intent i = new Intent();
				i.setAction(android.content.Intent.ACTION_VIEW);
				i.setDataAndType(Uri.fromFile(videoFile2Play), "video/*");
				startActivity(i);
			} else if (path.contains("doc") || path.contains("txt") || path.contains("zip") || path.contains("rar") || path.contains("gz")) {
				//do nothing, just save it, user can use file manager to view the document...
			} else {
				displayImage(file);
			}
		} else {
		showAlert("Error", "Format not supported");
		}
	}
	
	public void displayImage(File file) {
		OutputStream fOut = null;
		Bitmap bmImg = null;
		File nfile = null;

		if (file != null) {
			//If its compressed already lets just show it...
			if (!file.getAbsolutePath().contains("compressed_")) {
				try {
					// Convert to a compressed bitmap so the gallery can somewhat handle it nicely**
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 8;
					bmImg = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
					nfile = FileBuilder.createFile("compressed_" + object.getCName());
					fOut = new FileOutputStream(nfile);
					bmImg.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
					fOut.close();
					fOut = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Intent viewIntent = new Intent(Intent.ACTION_VIEW);
			viewIntent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "image/*");
			startActivity(viewIntent);
		} else {
			showAlert("Error", "File does not exist");
		}
	}
	
	//Set up alerts, dialogs and menus
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
	}

	// Create the Menu options
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.container_object_list_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete_object:
			showDialog(deleteObject);
			return true;
		case R.id.refresh:
			loadObjectData();
			return true;
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case deleteObject:
			return new AlertDialog.Builder(ContainerObjectDetails.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("Delete File")
					.setMessage("Are you sure you want to delete this file?")
					.setPositiveButton("Delete File",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									new ContainerObjectDeleteTask()
											.execute((Void[]) null);
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
	
	/**
	 * @return the file
	 */
	public ContainerObjects getViewFile() {
		return object;
	}

	/**
	 * @param File
	 *            the file to set
	 */
	public void setViewFile(ContainerObjects object) {
		this.object = object;
	}

}
