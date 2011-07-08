package com.rackspacecloud.android;

import java.util.ArrayList;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rackspace.cloud.files.api.client.Container;
import com.rackspace.cloud.files.api.client.ContainerManager;
import com.rackspace.cloud.files.api.client.ContainerObjectManager;
import com.rackspace.cloud.files.api.client.ContainerObjects;
import com.rackspace.cloud.files.api.client.ContainerPreferences;
import com.rackspace.cloud.servers.api.client.CloudServersException;

public class ContainerObjectsActivity extends ListActivity {

	private static final int deleteContainer = 1;
	private static final int containerInfo = 2;
	private static final int containerInfoNull = 3;
	private ContainerObjects[] files;
	private static Container container;
	public String LOG = "viewFilesActivity";
	public Object megaBytes;
	public Object kiloBytes;
	public int bConver = 1048576;
	public int kbConver = 1024;
	private Context context;
	private String email;
	private boolean isLogEnabled;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		container = (Container) this.getIntent().getExtras().get("container");
		Log.v(LOG, "CDNEnabled:" + container.isCdnEnabled());
		context = this.getApplicationContext();
		loadContainerPreferences();
		restoreState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("container", files);
	}

	private void restoreState(Bundle state) {
		if (state != null && state.containsKey("container")) {
			files = (ContainerObjects[]) state.getSerializable("container");
			if (files.length == 0) {
				displayNoContainerCell();
			} else {
				getListView().setDividerHeight(1); // restore divider lines
				setListAdapter(new ContainerAdapter());
			}
		} else {
			loadFiles();

		}
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (files != null && files.length > 0) {
			Intent viewIntent = new Intent(this, ContainerObjectDetails.class);
			viewIntent.putExtra("container", files[position]);
			viewIntent.putExtra("cdnUrl", container.getCdnUrl());
			viewIntent.putExtra("containerNames", container.getName());
			viewIntent.putExtra("isCdnEnabled", container.isCdnEnabled());
			startActivityForResult(viewIntent, 55); // arbitrary number; never
													// used again
		}
	}

	private void loadFiles() {
		displayLoadingCell();
		new LoadFilesTask().execute((Void[]) null);

	}

	private void setContainerList(ArrayList<ContainerObjects> files) {
		if (files == null) {
			files = new ArrayList<ContainerObjects>();
		}
		String[] fileNames = new String[files.size()];
		this.files = new ContainerObjects[files.size()];

		if (files != null) {
			for (int i = 0; i < files.size(); i++) {
				ContainerObjects file = files.get(i);
				this.files[i] = file;
				fileNames[i] = file.getName();
			}
		}

		if (fileNames.length == 0) {
			displayNoContainerCell();
		} else {
			getListView().setDividerHeight(1); // restore divider lines
			setListAdapter(new ContainerAdapter());
		}
	}

	private void displayLoadingCell() {
		String a[] = new String[1];
		a[0] = "Loading...";
		setListAdapter(new ArrayAdapter<String>(this, R.layout.loadingcell,
				R.id.loading_label, a));
		getListView().setTextFilterEnabled(true);
		getListView().setDividerHeight(0); // hide the dividers so it won't look
											// like a list row
		getListView().setItemsCanFocus(false);
	}

	private void displayNoContainerCell() {
		String a[] = new String[1];
		a[0] = "No Files";
		setListAdapter(new ArrayAdapter<String>(this, R.layout.noobjectscell,
				R.id.no_files_label, a));
		getListView().setTextFilterEnabled(true);
		getListView().setDividerHeight(0); // hide the dividers so it won't look
											// like a list row
		getListView().setItemsCanFocus(false);
	}

	private void showAlert(String title, String message) {
		try {
			AlertDialog alert = new AlertDialog.Builder(this).create();
			alert.setTitle(title);
			alert.setMessage(message);
			alert.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
					return;
				}
			});
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class LoadFilesTask extends
			AsyncTask<Void, Void, ArrayList<ContainerObjects>> {
		private CloudServersException exception;

		@Override
		protected ArrayList<ContainerObjects> doInBackground(Void... arg0) {
			ArrayList<ContainerObjects> files = null;
			try {
				files = (new ContainerObjectManager(context)).createList(true,
						container.getName());
			} catch (CloudServersException e) {
				exception = e;
				e.printStackTrace();
			}
			return files;
		}

		@Override
		protected void onPostExecute(ArrayList<ContainerObjects> result) {
			if (exception != null) {
				showAlert("Error", exception.getMessage());
			}
			setContainerList(result);
		}
	}

	class ContainerAdapter extends ArrayAdapter<ContainerObjects> {
		ContainerAdapter() {
			super(ContainerObjectsActivity.this,
					R.layout.listcontainerobjectcell, files);
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ContainerObjects file = files[position];
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.listcontainerobjectcell,
					parent, false);

			TextView label = (TextView) row.findViewById(R.id.label);
			label.setText(file.getCName());

			if (file.getBytes() >= bConver) {
				megaBytes = Math.abs(file.getBytes() / bConver + 0.2);
				TextView sublabel = (TextView) row.findViewById(R.id.sublabel);
				sublabel.setText(megaBytes + " MB |");
			} else if (file.getBytes() >= kbConver) {
				kiloBytes = Math.abs(file.getBytes() / kbConver + 0.2);
				TextView sublabel = (TextView) row.findViewById(R.id.sublabel);
				sublabel.setText(kiloBytes + " KB |");
			} else {
				TextView sublabel = (TextView) row.findViewById(R.id.sublabel);
				sublabel.setText(file.getBytes() + " B |");
			}

			return (row);
		}
	}

	private class DeleteContainerTask extends
			AsyncTask<Void, Void, HttpResponse> {
		private Exception exception;

		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;
			try {
				resp = (new ContainerManager(context)).delete(container
						.getName());
				Log.v(LOG, "container's name " + container.getName());
			} catch (Exception e) {
				exception = e;
			}
			return resp;
		}

		@Override
		protected void onPostExecute(HttpResponse response) {
			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 409) {
					showAlert("Error",
							"Container must be empty in order to delete");
				} else if (statusCode == 204) {
					setResult(Activity.RESULT_OK);
					showAlert("Accepted", "Container was successfully deleted.");
				} else if ("".equals(exception.getMessage())) {
					showAlert("Error",
							"There was a problem deleting your container.");
				} else {
					showAlert("Error",
							"There was a problem deleting your container: "
									+ exception.getMessage());
				}
			} else if (exception != null) {
				showAlert("Error",
						"There was a problem deleting your container: "
								+ exception.getMessage());
			}
		}
	}

	// Build needed alert dialog and menu's
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.view_container_object_list_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.cont_pref:
			Intent settingsActivity = new Intent(getBaseContext(),
					ContainerPreferences.class);
			startActivity(settingsActivity);
			return true;
		case R.id.delete_container:
			showDialog(deleteContainer);
			return true;
		case R.id.container_info:
			// TODO: more useful info could go here in a custom dialog or a new page.
			if (container.getCdnUrl() == null) {
				showDialog(containerInfoNull);
			} else {
				showDialog(containerInfo);
			}
			return true;
		case R.id.enable_cdn:
			Intent viewIntent1 = new Intent(this, EnableCDNActivity.class);
			viewIntent1.putExtra("container", container);
			viewIntent1.putExtra("email", email);
			viewIntent1.putExtra("isLogEnabled", isLogEnabled);
			startActivityForResult(viewIntent1, 56);
			return true;
		case R.id.refresh:
			loadFiles();
			return true;
		}
		return false;
	}

	private void loadContainerPreferences() {
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

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case deleteContainer:
			return new AlertDialog.Builder(ContainerObjectsActivity.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("Delete Container")
					.setMessage(
							"Are you sure you want to delete this Container?")
					.setPositiveButton("Delete Container",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									new DeleteContainerTask()
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
		case containerInfo:
			return new AlertDialog.Builder(ContainerObjectsActivity.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("Container Info")
					.setMessage("CDN URL:" + container.getCdnUrl())
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		case containerInfoNull:
			return new AlertDialog.Builder(ContainerObjectsActivity.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("Container Info")
					.setMessage("The Container is not CDN enabled.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();

		}
		return null;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			// a sub-activity kicked back, so we want to refresh the list
			loadFiles();
		}
		if (requestCode == 56) {
			if (resultCode == RESULT_OK) {
				Intent viewIntent1 = new Intent(this,
						ListContainerActivity.class);
				startActivityForResult(viewIntent1, 56);
			}
		}
	}
}
