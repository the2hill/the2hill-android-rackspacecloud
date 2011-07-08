package com.rackspacecloud.android;

import java.util.ArrayList;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rackspace.cloud.files.api.client.Container;
import com.rackspace.cloud.files.api.client.ContainerManager;
import com.rackspace.cloud.files.api.client.ContainerObjectManager;

public class ListContainerActivity extends ListActivity {

	private Container[] containers;
	public Container container;
	public Container cdnContainer;
	public String[] containerNames;
	public Object megaBytes;
	public Object kiloBytes;
	public Uri image;
	public int bConver = 1048576;
	public int kbConver = 1024;
	public String objectPath;
	public int column_index;

	protected static final int DELETE_ID = 0;
	private static final int uploadFile = 0;
	private static final int retryUploadFile = 1;
	
	private Context context;
	private Container uploadContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context = getApplicationContext();
		loadCameraPickerIntent();
		restoreState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("container", containers);
	}

	private void restoreState(Bundle state) {
		if (state != null && state.containsKey("container")) {
			containers = (Container[]) state.getSerializable("container");
			if (containers.length == 0) {
				displayNoContainersCell();
			} else {
				getListView().setDividerHeight(1); // restore divider lines
				setListAdapter(new FileAdapter());
			}
		} else {
			loadContainers();
			registerForContextMenu(getListView());
		}
	}
	
	private void loadCameraPickerIntent() {
		if (getIntent().getExtras() != null) {
			image = (Uri) this.getIntent().getExtras().get("image");
			if (image != null) {
				objectPath = getPath(image);
			}
		}
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (image != null) {
			uploadContainer = containers[position];
			showDialog(uploadFile);
		} else if (containers != null && containers.length > 0) {
			Intent viewIntent = new Intent(this, ContainerObjectsActivity.class);
			viewIntent.putExtra("container", containers[position]);
			startActivityForResult(viewIntent, 55);
		}
	}

	private void loadContainers() {
		displayLoadingCell();
		new LoadContainersTask().execute((Void[]) null);
	}

	private void setContainerList() {
		if (containerNames.length == 0) {
			displayNoContainersCell();
		} else {
			getListView().setDividerHeight(1); // restore divider lines
			setListAdapter(new FileAdapter());
		}
	}
	

	public String getPath(Uri uri) {
	    String[] proj = { MediaStore.Images.Media.DATA };
	    Cursor cursor = managedQuery(image, proj, null, null, null);
	    int column_index = cursor
	            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
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

	private void displayNoContainersCell() {
		String a[] = new String[1];
		a[0] = "No Containers";
		setListAdapter(new ArrayAdapter<String>(this,
				R.layout.nocontainerscell, R.id.no_containers_label, a));
		getListView().setTextFilterEnabled(true);
		getListView().setDividerHeight(0); // hide the dividers so it won't look
											// like a list row
		getListView().setItemsCanFocus(false);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			// a sub-activity kicked back, so we want to refresh the server list
			loadContainers();
		}
	}

	private class LoadContainersTask extends
			AsyncTask<Void, Void, ArrayList<Container>> {
		Exception ex;

		@Override
		protected ArrayList<Container> doInBackground(Void... arg0) {
			ArrayList<Container> containers = null;
			try {
				containers = (new ContainerManager(context)).createList(true);
			} catch (Exception e) {
				Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
				ex = e;
			}
			return containers;
		}

		@Override
		protected void onPostExecute(ArrayList<Container> result) {
			if (ex != null) {
				showAlert("Error", ex.getMessage());
			}
			ArrayList<Container> containerList = result;
			containerNames = new String[containerList.size()];
			containers = new Container[containerList.size()];
			if (containerList != null) {
				for (int i = 0; i < containerList.size(); i++) {
					Container container = containerList.get(i);
					containers[i] = container;
					containerNames[i] = container.getName();
				}
			}

			new LoadCDNContainersTask().execute((Void[]) null);
		}
	}

	private class LoadCDNContainersTask extends
			AsyncTask<Void, Void, ArrayList<Container>> {

		private Exception exception;

		@Override
		protected ArrayList<Container> doInBackground(Void... arg0) {
			ArrayList<Container> cdnContainers = null;

			try {
				cdnContainers = (new ContainerManager(context))
						.createCDNList(true);
			} catch (Exception e) {
				Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
				exception = e;
			}
			return cdnContainers;
		}

		@Override
		protected void onPostExecute(ArrayList<Container> result) {
			Log.v("listcontainerActivity", "onPostExecute loadCDNcontainerTask");
			if (exception != null) {
				showAlert("Error", exception.getMessage());
			}

			ArrayList<Container> cdnContainers = result;

			for (int i = 0; i < containers.length; i++) {
				Container container = containers[i];
				for (int t = 0; t < cdnContainers.size(); t++) {
					Container cdnContainer = cdnContainers.get(t);
					if (container.getName().equals(cdnContainer.getName())) {
						container.setCdnEnabled(cdnContainer.isCdnEnabled());
						container.setCdnUrl(cdnContainer.getCdnUrl());
						container.setTtl(cdnContainer.getTtl());
					}
				}
			}
			setContainerList();
		}
	}
	
	private class UploadObjectTask extends AsyncTask<Void, Void, HttpResponse> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			 dialog = new ProgressDialog(ListContainerActivity.this);
		        dialog.setMessage("Uploading file...");
		        dialog.setIndeterminate(true);
		        dialog.setCancelable(true);
		        dialog.show();
			Log.d("UploadObjectTask", "onPreExecute()");
		}

		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse response = null;
			
			//DEBUG AUDIO UPLOAD!!!
			try {
			 response = (new ContainerObjectManager(getApplicationContext()).uploadFromShareIntent(objectPath, uploadContainer.getName()));
			} catch (Exception e) {
				Log.e("UploadObjectTask", e.toString());
			}
			return response;
		}

		@Override
		protected void onPostExecute(HttpResponse response) {
			dialog.dismiss();
			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 201) {
					setResult(Activity.RESULT_OK);
					showAlert("Accepted", "Object was uploaded.");
					image = null;
				} else {
					Log.e("Error", "Status code: " + String.valueOf(statusCode));
					showDialog(retryUploadFile);
				}
			} else {
				Log.e("Error", "The response was null");
				showDialog(retryUploadFile);
			}
		}
	}

	//Create menus, dialogs and alerts...
	private void showAlert(String title, String message) {
		try {
			AlertDialog alert = new AlertDialog.Builder(this).create();
			alert.setTitle(title);
			alert.setMessage(message);
			alert.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case uploadFile:
			return new AlertDialog.Builder(ListContainerActivity.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("Uploading...")
					.setMessage("About to upload file now...")
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
										new UploadObjectTask()
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
		case retryUploadFile:
			return new AlertDialog.Builder(ListContainerActivity.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("")
					.setMessage(
							"There was an error, you can retry, if problem presists please contact support")
					.setPositiveButton("Retry?",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									new UploadObjectTask()
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.container_list_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_container:
			startActivityForResult(
					new Intent(this, AddContainerActivity.class), 56);
			return true;
		case R.id.refresh:
			loadContainers();
			return true;
		}
		return false;
	}

	class FileAdapter extends ArrayAdapter<Container> {
		FileAdapter() {
			super(ListContainerActivity.this, R.layout.listcontainerscell,
					containers);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			Container container = containers[position];
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.listcontainerscell, parent,
					false);

			TextView label = (TextView) row.findViewById(R.id.label);
			label.setText(container.getName());

			if (container.getBytes() >= bConver) {
				megaBytes = Math.abs(container.getBytes() / bConver + 0.2);
				TextView sublabel = (TextView) row.findViewById(R.id.sublabel);
				sublabel.setText(container.getCount() + " Objects |" + megaBytes
						+ " MB |");
			} else if (container.getBytes() >= kbConver) {
				kiloBytes = Math.abs(container.getBytes() / kbConver + 0.2);
				TextView sublabel = (TextView) row.findViewById(R.id.sublabel);
				sublabel.setText(container.getCount() + " Objects |" + kiloBytes
						+ " KB |");
			} else {
				TextView sublabel = (TextView) row.findViewById(R.id.sublabel);
				sublabel.setText(container.getCount() + " Objects |"
						+ container.getBytes() + " B |");
			}
			if (container.isCdnEnabled()) {
				ImageView cdnLight = (ImageView) row
						.findViewById(R.id.cdn_light);
				cdnLight.setImageResource(R.drawable.cdn_light);
			}
			return (row);
		}
	}
}
