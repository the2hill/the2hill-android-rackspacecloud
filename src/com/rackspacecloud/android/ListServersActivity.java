/**
 * 
 */
package com.rackspacecloud.android;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.rackspace.cloud.servers.api.client.CloudServersException;
import com.rackspace.cloud.servers.api.client.Server;
import com.rackspace.cloud.servers.api.client.ServerManager;

/**
 * @author Mike Mayo - mike.mayo@rackspace.com - twitter.com/greenisus
 *
 */
public class ListServersActivity extends ListActivity {

	private Server[] servers;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreState(savedInstanceState);
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("servers", servers);
	}

    private void restoreState(Bundle state) {
    	if (state != null && state.containsKey("servers")) {
    		servers = (Server[]) state.getSerializable("servers");
    		if (servers.length == 0) {
    			displayNoServersCell();
    		} else {
    			getListView().setDividerHeight(1); // restore divider lines 
    			setListAdapter(new ServerAdapter());
    		}
    	} else {
            loadServers();        
    	}
    }
    
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	if (servers != null && servers.length > 0) {
	    	Intent viewIntent = new Intent(this, ViewServerActivity.class);
	    	viewIntent.putExtra("server", servers[position]);
			startActivityForResult(viewIntent, 55); // arbitrary number; never used again
    	}
    }
    
    private void loadServers() {
    	displayLoadingCell();
    	new LoadServersTask().execute((Void[]) null);
    }
    
    private void setServerList(ArrayList<Server> servers) {
    	if (servers == null) {
    		servers = new ArrayList<Server>();
    	}
    	String[] serverNames = new String[servers.size()];
    	this.servers = new Server[servers.size()];
    	
		if (servers != null) {
			for (int i = 0; i < servers.size(); i++) {
				Server server = servers.get(i);
				this.servers[i] = server;
				serverNames[i] = server.getName();
			}
		}
		
		if (serverNames.length == 0) {
			displayNoServersCell();
		} else {
			getListView().setDividerHeight(1); // restore divider lines 
			setListAdapter(new ServerAdapter());
		}
    }
    
    private void displayLoadingCell() {
    	String a[] = new String[1];
    	a[0] = "Loading...";
        setListAdapter(new ArrayAdapter<String>(this, R.layout.loadingcell, R.id.loading_label, a));
        getListView().setTextFilterEnabled(true);
        getListView().setDividerHeight(0); // hide the dividers so it won't look like a list row
        getListView().setItemsCanFocus(false);
    }
    
    private void displayNoServersCell() {
    	String a[] = new String[1];
    	a[0] = "No Servers";
        setListAdapter(new ArrayAdapter<String>(this, R.layout.noserverscell, R.id.no_servers_label, a));
        getListView().setTextFilterEnabled(true);
        getListView().setDividerHeight(0); // hide the dividers so it won't look like a list row
        getListView().setItemsCanFocus(false);
    }
    
    private void showAlert(String title, String message) {
    	//Can't create handler inside thread that has not called Looper.prepare()
    	//Looper.prepare();
    	try {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setButton("OK", new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int which) {
	        return;
	    } }); 
		alert.show();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    
    private class LoadServersTask extends AsyncTask<Void, Void, ArrayList<Server>> {
    	
    	private CloudServersException exception;
    	
		@Override
		protected ArrayList<Server> doInBackground(Void... arg0) {
			ArrayList<Server> servers = null;
			try {
				servers = (new ServerManager()).createList(true);
			} catch (CloudServersException e) {
				exception = e;				
			}
			return servers;
		}
    	
		@Override
		protected void onPostExecute(ArrayList<Server> result) {
			if (exception != null) {
				showAlert("Error", exception.getMessage());
			}
			setServerList(result);
		}
    }
    
    @Override 
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.servers_list_menu, menu);
		return true;
	} 
    
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_server:
			startActivityForResult(new Intent(this, AddServerActivity.class), 56); // arbitrary number; never used again
			return true;
		case R.id.refresh:
			loadServers();
	        return true;
		}
		return false;
	} 
	//*/
    
	class ServerAdapter extends ArrayAdapter<Server> {
		ServerAdapter() {
			super(ListServersActivity.this, R.layout.listservercell, servers);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			
			Server server = servers[position];
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.listservercell, parent, false);

			TextView label = (TextView) row.findViewById(R.id.label);
			label.setText(server.getName());
			
			TextView sublabel = (TextView) row.findViewById(R.id.sublabel);
			sublabel.setText(server.getFlavor().getName() + " - " + server.getImage().getName());
			
			ImageView icon = (ImageView) row.findViewById(R.id.icon);
			icon.setImageResource(server.getImage().iconResourceId());

			return(row);
		}
	}
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  
	  if (resultCode == RESULT_OK) {	  
		  // a sub-activity kicked back, so we want to refresh the server list
		  loadServers();
	  }
	}	
}
