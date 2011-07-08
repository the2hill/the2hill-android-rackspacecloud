package com.rackspacecloud.android;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rackspace.cloud.files.api.client.ContainerManager;

public class AddContainerActivity extends Activity implements  OnClickListener {	
	private EditText fileName;
	private Context context;	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.createcontainer);
        fileName = (EditText) findViewById(R.id.container_name);
        ((Button) findViewById(R.id.save_button)).setOnClickListener(this);
    }

	public void onClick(View arg0) {
		if ("".equals(fileName.getText().toString())) {
			showAlert("Required Fields Missing", " Container name is required.");
		} else {
			showActivityIndicators();
				new SaveFileTask().execute((Void[]) null);
		}
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
	
    private void setActivityIndicatorsVisibility(int visibility) {
        ProgressBar pb = (ProgressBar) findViewById(R.id.save_container_progress_bar);
    	TextView tv = (TextView) findViewById(R.id.saving_container_label);
        pb.setVisibility(visibility);
        tv.setVisibility(visibility);
    }

    private void showActivityIndicators() {
    	setActivityIndicatorsVisibility(View.VISIBLE);
    }
    
    private void hideActivityIndicators() {
    	setActivityIndicatorsVisibility(View.INVISIBLE);
    }
    
    private class SaveFileTask extends AsyncTask<Void, Void, HttpResponse> {
    	private Exception exception;
    	
    	@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;
			try {
				resp = (new ContainerManager(context)).create(fileName.getText());
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
					finish();
				} else {
					if ("".equals(exception.getMessage())) {
						showAlert("Error", "There was a problem creating your container.");
					} else {
						showAlert("Error", "There was a problem creating your container: " + exception.getMessage() + " Check container name and try again");
					}
				}
			} else if (exception != null) {
				showAlert("Error", "There was a problem creating your container: " + exception.getMessage()+" Check container name and try again");				
			}			
		}
    }
}