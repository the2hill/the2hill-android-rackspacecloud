package com.rackspacecloud.android;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.rackspace.cloud.files.api.client.Container;
import com.rackspace.cloud.files.api.client.ContainerManager;

public class EnableCDNActivity extends Activity implements OnClickListener, OnItemSelectedListener {
	public String LOG = "EnableCDNActivity";
	public static Container container = null;
	private String selectedTtlId;
	private String selectedLogRetId;
	private Spinner ttlSpinner;
	private Spinner logRetSpinner;
	private Context context;
	private boolean isLogEnabled;
	private String email;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enable_cdn_container);
		context = getApplicationContext();
		loadIntent();
		setupButtons();
		loadTtlSpinner();
		loadLogRetSpinner();

	}

	private void loadIntent() {
		container = (Container) this.getIntent().getExtras().get("container");
		email = (String) this.getIntent().getExtras().get("email");
		isLogEnabled = (boolean) this.getIntent().getBooleanExtra("isLogEnabled", false);
	}

	private void setupButton(int resourceId, OnClickListener onClickListener) {
		Button button = (Button) findViewById(resourceId);
		button.setOnClickListener(onClickListener);
	}

	private void setupButtons() {
		setupButton(R.id.enable_button, new OnClickListener() {
			public void onClick(View v) {
				if (!container.isCdnEnabled()) {
					showDialog(R.id.enable_button);
				} else {
					showAlert("Attention", "The container is already enabled");
				}
			}
		});

		setupButton(R.id.disable_button, new OnClickListener() {
			public void onClick(View v) {
				if (container.isCdnEnabled()) {
					showDialog(R.id.disable_button);
				} else {
					showAlert("Attention", "The container is not CDN enabled");
				}
			}
		});
		setupButton(R.id.update_button, new OnClickListener() {
			public void onClick(View v) {
				if (container.isCdnEnabled()) {
					showDialog(R.id.update_button);
				} else {
					showAlert("Attention", "The container is not CDN enabled");
				}
			}
		});
		setupButton(R.id.purge_cdn_button, new OnClickListener() {
			public void onClick(View v) {
				if (container.isCdnEnabled()) {
					showDialog(R.id.purge_cdn_button);
				} else {
					showAlert("Attention", "The container is not CDN enabled");
				}
			}
		});
	}

	private void loadTtlSpinner() {
		ttlSpinner = (Spinner) findViewById(R.id.ttl_spinner);
		ttlSpinner.setOnItemSelectedListener(this);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.ttl, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ttlSpinner.setAdapter(adapter);

	}

	private void loadLogRetSpinner() {
		logRetSpinner = (Spinner) findViewById(R.id.log_retention_spinner);
		logRetSpinner.setOnItemSelectedListener(this);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.logRet, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		logRetSpinner.setAdapter(adapter);

	}

	public void onItemSelected(AdapterView<?> parent, View view, int id,
			long arg3) {
		if (parent == ttlSpinner) {
			selectedTtlId = ttlSpinner.getSelectedItem().toString();
		} else if (parent == logRetSpinner) {
			selectedLogRetId = logRetSpinner.getSelectedItem().toString();
		}
	}

	public void onNothingSelected(AdapterView<?> parent) {

	}

	private void setActivityIndicatorsVisibility(int visibility) {
		ProgressBar pb = (ProgressBar) findViewById(R.id.save_container_progress_bar);
		TextView tv = (TextView) findViewById(R.id.saving_container_label);
		pb.setVisibility(visibility);
		tv.setVisibility(visibility);
	}

	@SuppressWarnings("unused")
	private void showActivityIndicators() {
		setActivityIndicatorsVisibility(View.VISIBLE);
	}

	private void hideActivityIndicators() {
		setActivityIndicatorsVisibility(View.INVISIBLE);
	}

	public class EnableCDNTask extends AsyncTask<Void, Void, HttpResponse> {
		private Exception exception;

		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;
			if (container.isCdnEnabled())
				showAlert("Attention", "The container is already enabled");
			try {
				resp = (new ContainerManager(context)).enable(
						container.getName(), selectedTtlId, selectedLogRetId);
			} catch (Exception e) {
				exception = e;
			}
			return resp;
		}

		@Override
		protected void onPostExecute(HttpResponse response) {
			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 201) {
					setResult(Activity.RESULT_OK);
					showAlert("Accepted", "Container was successfully enabled");
				} else if (statusCode == 202) {
					showAlert("Accepted", "Container was successfully enabled");
				} else {
					if ("".equals(exception.getMessage())) {
						showAlert("Error",
								"There was a problem creating your container.");
					} else {
						showAlert("Error",
								"There was a problem creating your container: "
										+ "Check container name and try again");
					}
				}
			} else if (exception != null) {
				showAlert("Error",
						"There was a problem creating your container: "
								+ exception.getMessage()
								+ " Check container name and try again");
			}
		}
	}

	public class DisableCDNTask extends AsyncTask<Void, Void, HttpResponse> {
		private Exception exception;

		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;
			if (!container.isCdnEnabled())
				showAlert("Attention", "The container is not CDN enabled");
			try {
				resp = (new ContainerManager(context)).disable(container
						.getName());
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
					showAlert("Accepted", "Container was successfully disabled");
				} else {
					if ("".equals(exception.getMessage())) {
						showAlert("Error",
								"There was a problem creating your container.");
					} else {
						showAlert("Error",
								"There was a problem creating your container: "
										+ " Check container name and try again");
					}
				}
			} else if (exception != null) {
				showAlert("Error",
						"There was a problem creating your container: "
								+ exception.getMessage()
								+ " Check container name and try again");
			}
		}
	}

	public class UpdateCDNTask extends AsyncTask<Void, Void, HttpResponse> {
		private Exception exception;

		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;
			if (container.isCdnEnabled())
				showAlert("Attention", "The Container is not CDN enabled.");
			try {
				resp = (new ContainerManager(context)).update(
						container.getName(), "true", selectedTtlId,
						selectedLogRetId);
			} catch (Exception e) {
				exception = e;
			}
			return resp;
		}

		@Override
		protected void onPostExecute(HttpResponse response) {
			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 202) {
					setResult(Activity.RESULT_OK);
					showAlert("Accepted", "Container was successfully updated");
				} else {
					if ("".equals(exception.getMessage())) {
						showAlert("Error",
								"There was a problem creating your container.");
					} else {
						showAlert("Error",
								"There was a problem creating your container: "
										+ " Check container name and try again");
					}
				}
			} else if (exception != null) {
				showAlert("Error",
						"There was a problem creating your container: "
								+ exception.getMessage()
								+ " Check container name and try again");
			}
		}
	}

	private class PurgeCDNTask extends AsyncTask<Void, Void, HttpResponse> {
		private Exception exception;

		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;
			try {
				resp = (new ContainerManager(context)).purge(
						container.getName(), email, isLogEnabled, container.isCdnEnabled());
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
				if (statusCode == 204) {
					setResult(Activity.RESULT_OK);
					showAlert("Accepted", "Container was successfully purged.");
				} else {
					if ("".equals(exception.getMessage())) {
						showAlert("Error",
								"There was a problem deleting your container.");
					} else {
						showAlert("Error",
								"There was a problem deleting your container: "
										+ exception.getMessage());
					}
				}
			} else if (exception != null) {
				showAlert("Error",
						"There was a problem deleting your container: "
								+ exception.getMessage());
			}
		}
	}

	// Build needed alerts and dialogs
	private void showAlert(String title, String message) {
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
		hideActivityIndicators();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case R.id.enable_button:
			return new AlertDialog.Builder(EnableCDNActivity.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("Enable CDN")
					.setMessage(
							"Are you sure you want to enable CDN on this container?")
					.setPositiveButton("Enable CDN",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									new EnableCDNTask().execute((Void[]) null);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked Cancel so do some stuff
								}
							}).create();
		case R.id.disable_button:
			return new AlertDialog.Builder(EnableCDNActivity.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("Change Attributes")
					.setMessage(
							"Are you sure you want to disable CDN? CDN will remain active till the TTL expires;")
					.setPositiveButton("Disable CDN",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									new DisableCDNTask().execute((Void[]) null);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked Cancel so do some stuff
								}
							}).create();
		case R.id.update_button:
			return new AlertDialog.Builder(EnableCDNActivity.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("Change Attributes")
					.setMessage(
							"Are you sure you wish to update the CDN attributes?")
					.setPositiveButton("Change Attributes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									new UpdateCDNTask().execute((Void[]) null);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked Cancel so do some stuff
								}
							}).create();
		case R.id.purge_cdn_button:
			return new AlertDialog.Builder(EnableCDNActivity.this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle("Purge CDN Container")
					.setMessage(
							"Are you sure you want to purge this container from the CDN?")
					.setPositiveButton("Purge Container",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// User clicked OK so do some stuff
									new PurgeCDNTask()
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

	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}