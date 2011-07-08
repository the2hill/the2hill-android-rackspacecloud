package com.rackspacecloud.android;

import java.util.ArrayList;
import java.util.TreeMap;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TabHost;

import com.rackspace.cloud.servers.api.client.Flavor;
import com.rackspace.cloud.servers.api.client.FlavorManager;
import com.rackspace.cloud.servers.api.client.Image;
import com.rackspace.cloud.servers.api.client.ImageManager;

public class TabViewActivity extends TabActivity {

	private TabHost.TabSpec spec;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new LoadImagesTask().execute((Void[]) null);
		TabHost tabs = getTabHost();

		spec = tabs.newTabSpec("tab1");
		spec.setContent(new Intent(this, ListServersActivity.class));
		spec.setIndicator("Cloud Servers",
				getResources().getDrawable(R.drawable.cloudservers_icon));
		tabs.addTab(spec);

		spec = tabs.newTabSpec("tab2");
		spec.setContent(new Intent(this, ListContainerActivity.class));
		spec.setIndicator("Cloud Files",
				getResources().getDrawable(R.drawable.cloudfiles));
		tabs.addTab(spec);

	}

	// this is called when the screen rotates.
	// (onCreate is no longer called when screen rotates due to manifest, see:
	// android:configChanges)
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		spec.setContent(new Intent(this, ListServersActivity.class));
		spec.setContent(new Intent(this, ListContainerActivity.class));

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
	}

	private class LoadFlavorsTask extends
			AsyncTask<Void, Void, ArrayList<Flavor>> {

		@Override
		protected ArrayList<Flavor> doInBackground(Void... arg0) {
			return (new FlavorManager()).createList(true);
		}

		@Override
		protected void onPostExecute(ArrayList<Flavor> result) {
			if (result != null && result.size() > 0) {
				TreeMap<String, Flavor> flavorMap = new TreeMap<String, Flavor>();
				for (int i = 0; i < result.size(); i++) {
					Flavor flavor = result.get(i);
					flavorMap.put(flavor.getId(), flavor);
				}
				Flavor.setFlavors(flavorMap);
			} else {
				showAlert("Login Failure",
						"There was a problem loading server flavors.  Please try again.");
			}
		}
	}

	private class LoadImagesTask extends
			AsyncTask<Void, Void, ArrayList<Image>> {

		@Override
		protected ArrayList<Image> doInBackground(Void... arg0) {
			return (new ImageManager()).createList(true);
		}

		@Override
		protected void onPostExecute(ArrayList<Image> result) {
			if (result != null && result.size() > 0) {
				TreeMap<String, Image> imageMap = new TreeMap<String, Image>();
				for (int i = 0; i < result.size(); i++) {
					Image image = result.get(i);
					imageMap.put(image.getId(), image);
				}
				Image.setImages(imageMap);
				new LoadFlavorsTask().execute((Void[]) null);
				// startActivity(tabViewIntent);
			} else {
				showAlert("Login Failure",
						"There was a problem loading server images.  Please try again.");
			}
		}
	}

}
