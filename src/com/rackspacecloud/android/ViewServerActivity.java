/**
 * 
 */
package com.rackspacecloud.android;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rackspace.cloud.servers.api.client.CloudServersException;
import com.rackspace.cloud.servers.api.client.Flavor;
import com.rackspace.cloud.servers.api.client.Server;
import com.rackspace.cloud.servers.api.client.ServerManager;
import com.rackspace.cloud.servers.api.client.parsers.CloudServersFaultXMLParser;

/**
 * @author Mike Mayo - mike.mayo@rackspace.com - twitter.com/greenisus
 *
 */
public class ViewServerActivity extends Activity {
	
	private Server server;
	private boolean ipAddressesLoaded; // to prevent polling from loading tons of duplicates
	private Flavor[] flavors;
	private String[] flavorNames;
	private String selectedFlavorId;
	private boolean imageLoaded;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server = (Server) this.getIntent().getExtras().get("server");
        setContentView(R.layout.viewserver);
        restoreState(savedInstanceState);
    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("server", server);
		outState.putBoolean("imageLoaded", imageLoaded);
	}

    private void restoreState(Bundle state) {
    	if (state != null && state.containsKey("server")) {
    		server = (Server) state.getSerializable("server");
    		imageLoaded = state.getBoolean("imageLoaded");
    	}
        loadServerData();
        setupButtons();
        loadFlavors();
    }

    private void loadImage() {
    	// hate to do this, but devices run out of memory after a few rotations
    	// because the background images are so large
    	if (!imageLoaded) {
        	ImageView osLogo = (ImageView) findViewById(R.id.view_server_os_logo);
        	osLogo.setAlpha(100);
	    	osLogo.setImageResource(server.getImage().logoResourceId());
	    	imageLoaded = true;
    	}

    }
    
    private void loadServerData() {
    	TextView name = (TextView) findViewById(R.id.view_server_name);
    	name.setText(server.getName());
    	
    	TextView os = (TextView) findViewById(R.id.view_server_os);
    	os.setText(server.getImage().getName());
    	
    	TextView memory = (TextView) findViewById(R.id.view_server_memory);
    	memory.setText(server.getFlavor().getRam() + " MB");
    	
    	TextView disk = (TextView) findViewById(R.id.view_server_disk);
    	disk.setText(server.getFlavor().getDisk() + " GB");
    	
    	TextView status = (TextView) findViewById(R.id.view_server_status);

    	// show status and possibly the progress, with polling
    	if (!"ACTIVE".equals(server.getStatus())) {
        	status.setText(server.getStatus() + " - " + server.getProgress() + "%");
    		new PollServerTask().execute((Void[]) null);
    	} else {
        	status.setText(server.getStatus());
    	}
    	
    	if (!ipAddressesLoaded) {
	    	// public IPs
	    	int layoutIndex = 12; // public IPs start here
	    	LinearLayout layout = (LinearLayout) this.findViewById(R.id.view_server_layout);    	
	    	String publicIps[] = server.getPublicIpAddresses();
	    	for (int i = 0; i < publicIps.length; i++) {
	        	TextView tv = new TextView(this.getBaseContext());
	        	tv.setLayoutParams(os.getLayoutParams()); // easy quick styling! :)
	        	tv.setTypeface(tv.getTypeface(), 1); // 1 == bold
	        	tv.setTextSize(os.getTextSize());
	        	tv.setTextColor(Color.WHITE);
	        	tv.setText(publicIps[i]);
	        	layout.addView(tv, layoutIndex++);
	    	}
	    	
	    	// private IPs
	    	layoutIndex++; // skip over the Private IPs label
	    	String privateIps[] = server.getPrivateIpAddresses();
	    	for (int i = 0; i < privateIps.length; i++) {
	        	TextView tv = new TextView(this.getBaseContext());
	        	tv.setLayoutParams(os.getLayoutParams()); // easy quick styling! :)
	        	tv.setTypeface(tv.getTypeface(), 1); // 1 == bold
	        	tv.setTextSize(os.getTextSize());
	        	tv.setTextColor(Color.WHITE);
	        	tv.setText(privateIps[i]);
	        	layout.addView(tv, layoutIndex++);
	    	}

	    	loadImage();
	    	ipAddressesLoaded = true;
    	}
    }
    
    private void loadFlavors() {
		flavorNames = new String[Flavor.getFlavors().size()]; 
		flavors = new Flavor[Flavor.getFlavors().size()];

		Iterator<Flavor> iter = Flavor.getFlavors().values().iterator();
		int i = 0;
		while (iter.hasNext()) {
			Flavor flavor = iter.next();
			flavors[i] = flavor;
			flavorNames[i] = flavor.getName() + ", " + flavor.getDisk() + " GB disk";
			i++;
		}
		selectedFlavorId = flavors[0].getId();
    }

    private void setupButton(int resourceId, OnClickListener onClickListener) {
		Button button = (Button) findViewById(resourceId);
		button.setOnClickListener(onClickListener);
    }
    
    private void setupButtons() {
    	setupButton(R.id.view_server_soft_reboot_button, new OnClickListener() {
            public void onClick(View v) {
                showDialog(R.id.view_server_soft_reboot_button);
            }
        });
    	
    	setupButton(R.id.view_server_hard_reboot_button, new OnClickListener() {
            public void onClick(View v) {
                showDialog(R.id.view_server_hard_reboot_button);
            }
    	});

    	setupButton(R.id.view_server_resize_button, new OnClickListener() {
            public void onClick(View v) {
                showDialog(R.id.view_server_resize_button);
            }
    	});

    	setupButton(R.id.view_server_delete_button, new OnClickListener() {
            public void onClick(View v) {
                showDialog(R.id.view_server_delete_button);
            }
    	});
    	
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
    }
	
    /**
	 * @return the server
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(Server server) {
		this.server = server;
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case R.id.view_server_soft_reboot_button:
            return new AlertDialog.Builder(ViewServerActivity.this)
        	.setIcon(R.drawable.alert_dialog_icon)
        	.setTitle("Soft Reboot")
        	.setMessage("Are you sure you want to perform a soft reboot?")
        	.setPositiveButton("Reboot Server", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
        			// User clicked OK so do some stuff
        			new SoftRebootServerTask().execute((Void[]) null);
        		}
        	})
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
        			// User clicked Cancel so do some stuff
        		}
        	})
        	.create();
        case R.id.view_server_hard_reboot_button:
            return new AlertDialog.Builder(ViewServerActivity.this)
        	.setIcon(R.drawable.alert_dialog_icon)
        	.setTitle("Hard Reboot")
        	.setMessage("Are you sure you want to perform a hard reboot?")
        	.setPositiveButton("Reboot Server", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
        			// User clicked OK so do some stuff
        			new HardRebootServerTask().execute((Void[]) null);
        		}
        	})
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
        			// User clicked Cancel so do some stuff
        		}
        	})
        	.create();
        case R.id.view_server_resize_button:
            return new AlertDialog.Builder(ViewServerActivity.this)
            .setItems(flavorNames, new ResizeClickListener())
        	.setIcon(R.drawable.alert_dialog_icon)
        	.setTitle("Resize Server")
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
        			// User clicked Cancel so do some stuff
        		}
        	})
        	.create();
        case R.id.view_server_delete_button:
            return new AlertDialog.Builder(ViewServerActivity.this)
        	.setIcon(R.drawable.alert_dialog_icon)
        	.setTitle("Delete Server")
        	.setMessage("Are you sure you want to delete this server?  This operation cannot be undone and all backups will be deleted.")
        	.setPositiveButton("Delete Server", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
        			// User clicked OK so do some stuff
        			new DeleteServerTask().execute((Void[]) null);
        		}
        	})
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        		public void onClick(DialogInterface dialog, int whichButton) {
        			// User clicked Cancel so do some stuff
        		}
        	})
        	.create();
        }
        return null;
    }

    private class ResizeClickListener implements android.content.DialogInterface.OnClickListener {

		public void onClick(DialogInterface dialog, int which) {
			selectedFlavorId = which + "";
			new ResizeServerTask().execute((Void[]) null);
		}
    	
    }
    
    private CloudServersException parseCloudServersException(HttpResponse response) {
		CloudServersException cse = new CloudServersException();
		try {
		    BasicResponseHandler responseHandler = new BasicResponseHandler();
		    String body = responseHandler.handleResponse(response);
	    	CloudServersFaultXMLParser parser = new CloudServersFaultXMLParser();
	    	SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
	    	XMLReader xmlReader = saxParser.getXMLReader();
	    	xmlReader.setContentHandler(parser);
	    	xmlReader.parse(new InputSource(new StringReader(body)));		    	
	    	cse = parser.getException();		    	
		} catch (ClientProtocolException e) {
			cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
		} catch (IOException e) {
			cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
		} catch (ParserConfigurationException e) {
			cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
		} catch (SAXException e) {
			cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
		}
		return cse;
    }
    
    // HTTP request tasks
    
	private class PollServerTask extends AsyncTask<Void, Void, Server> {
    	
		@Override
		protected Server doInBackground(Void... arg0) {
			try {
				server = (new ServerManager()).find(Integer.parseInt(server.getId()));
			} catch (NumberFormatException e) {
				// we're polling, so need to show exceptions
			} catch (CloudServersException e) {
				// we're polling, so need to show exceptions
			}
			return server;
		}
    	
		@Override
		protected void onPostExecute(Server result) {
			server = result;
			loadServerData();
		}
    }

    
	private class SoftRebootServerTask extends AsyncTask<Void, Void, HttpResponse> {
    	
		private CloudServersException exception;
		
		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;
			try {
				resp = (new ServerManager()).reboot(server, ServerManager.SOFT_REBOOT);
			} catch (CloudServersException e) {
				exception = e;
			}
			return resp;
		}
    	
		@Override
		protected void onPostExecute(HttpResponse response) {

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();				
				if (statusCode != 202) {
					CloudServersException cse = parseCloudServersException(response);
					if ("".equals(cse.getMessage())) {
						showAlert("Error", "There was a problem rebooting your server.");
					} else {
						showAlert("Error", "There was a problem rebooting your server: " + cse.getMessage());
					}
				}
			} else if (exception != null) {
				showAlert("Error", "There was a problem rebooting your server: " + exception.getMessage());
				
			}
		}
    }

	private class HardRebootServerTask extends AsyncTask<Void, Void, HttpResponse> {
    	
		private CloudServersException exception;

		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;			
			try {
				resp = (new ServerManager()).reboot(server, ServerManager.HARD_REBOOT);
			} catch (CloudServersException e) {
				exception = e;
			}
			return resp;
		}
    	
		@Override
		protected void onPostExecute(HttpResponse response) {
			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();			
				if (statusCode != 202) {
					CloudServersException cse = parseCloudServersException(response);
					if ("".equals(cse.getMessage())) {
						showAlert("Error", "There was a problem rebooting your server.");
					} else {
						showAlert("Error", "There was a problem rebooting your server: " + cse.getMessage());
					}
				}
			} else if (exception != null) {
				showAlert("Error", "There was a problem rebooting your server: " + exception.getMessage());
				
			}
		}
    }

	private class ResizeServerTask extends AsyncTask<Void, Void, HttpResponse> {
    	
		private CloudServersException exception;

		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;
			try {
				resp = (new ServerManager()).resize(server, Integer.parseInt(selectedFlavorId));
			} catch (CloudServersException e) {
				exception = e;
			}
			return resp;
		}
    	
		@Override
		protected void onPostExecute(HttpResponse response) {
			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();			
				if (statusCode == 202) {
					new PollServerTask().execute((Void[]) null);
				} else {					
					CloudServersException cse = parseCloudServersException(response);
					if ("".equals(cse.getMessage())) {
						showAlert("Error", "There was a problem deleting your server.");
					} else {
						showAlert("Error", "There was a problem deleting your server: " + cse.getMessage());
					}					
				}
			} else if (exception != null) {
				showAlert("Error", "There was a problem resizing your server: " + exception.getMessage());
				
			}
			
		}
    }
	
	private class DeleteServerTask extends AsyncTask<Void, Void, HttpResponse> {
    	
		private CloudServersException exception;

		@Override
		protected HttpResponse doInBackground(Void... arg0) {
			HttpResponse resp = null;
			try {
				resp = (new ServerManager()).delete(server);
			} catch (CloudServersException e) {
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
					finish();
				} else {
					CloudServersException cse = parseCloudServersException(response);
					if ("".equals(cse.getMessage())) {
						showAlert("Error", "There was a problem deleting your server.");
					} else {
						showAlert("Error", "There was a problem deleting your server: " + cse.getMessage());
					}
				}
			} else if (exception != null) {
				showAlert("Error", "There was a problem deleting your server: " + exception.getMessage());				
			}			
		}
    }
}
