/**
 * 
 */
package com.rackspace.cloud.servers.api.client.parsers;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.rackspace.cloud.servers.api.client.Server;


/**
 * @author Mike Mayo - mike.mayo@rackspace.com - twitter.com/greenisus
 *
 */
public class ServersXMLParser extends DefaultHandler {

	private Server server;
	private ArrayList<Server> servers;
	private ArrayList<String> publicAddresses;
	private ArrayList<String> privateAddresses;
	private boolean parsingPublicAddresses;
	private StringBuffer currentData;

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String uri, String name, String qName, Attributes atts) {

		currentData = new StringBuffer();
		if ("servers".equals(name)) {
			servers = new ArrayList<Server>();
		} else if ("server".equals(name)) {
			server = new Server();
			server.setStatus(atts.getValue("status"));
			server.setProgress(atts.getValue("progress"));
			server.setHostId(atts.getValue("hostId"));
			server.setFlavorId(atts.getValue("flavorId"));
			server.setImageId(atts.getValue("imageId"));
			server.setId(atts.getValue("id"));
			server.setName(atts.getValue("name"));
		} else if ("addresses".equals(name)) {
			//addresses = new ArrayList<String>();
		} else if ("public".equals(name)) {
			parsingPublicAddresses = true;
			publicAddresses = new ArrayList<String>();
		} else if ("private".equals(name)) {
			parsingPublicAddresses = false;
			privateAddresses = new ArrayList<String>();
		} else if ("ip".equals(name)) {
			String ipAddress = atts.getValue("addr");
			if (parsingPublicAddresses) {
				publicAddresses.add(ipAddress);
			} else {
				privateAddresses.add(ipAddress);
			}
		}
	}

	public void endElement(String uri, String name, String qName) {
		if ("servers".equals(name)) {
			
		} else if ("server".equals(name)) {
			if (servers != null) {
				servers.add(server);
			}
		} else if ("addresses".equals(name)) {
			
			String[] privateIpAddresses = new String[privateAddresses.size()];
			String[] publicIpAddresses = new String[publicAddresses.size()];
			
			for (int i = 0; i < privateAddresses.size(); i++) {
				privateIpAddresses[i] = privateAddresses.get(i);
			}
			for (int i = 0; i < publicAddresses.size(); i++) {
				publicIpAddresses[i] = publicAddresses.get(i);
			}
			
			server.setPublicIpAddresses(publicIpAddresses);
			server.setPrivateIpAddresses(privateIpAddresses);			
		}
	}

	public void characters(char ch[], int start, int length) {
		Log.d("Rackspace-Cloud", "Characters:    \"");
		for (int i = start; i < start + length; i++) {
			switch (ch[i]) {
			case '\\':
				Log.d("Rackspace-Cloud", "\\\\");
				break;
			case '"':
				Log.d("Rackspace-Cloud", "\\\"");
				break;
			case '\n':
				Log.d("Rackspace-Cloud", "\\n");
				break;
			case '\r':
				Log.d("Rackspace-Cloud", "\\r");
				break;
			case '\t':
				Log.d("Rackspace-Cloud", "\\t");
				break;
			default:
				Log.d("Rackspace-Cloud", String.valueOf(ch[i]));
				break;
			}
		}
		Log.d("Rackspace-Cloud", "\"\n");
		
		
		for (int i = start; i < (start + length); i++) {
			currentData.append(ch[i]);
		}
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	/**
	 * @return the servers
	 */
	public ArrayList<Server> getServers() {
		return servers;
	}

	/**
	 * @param servers the servers to set
	 */
	public void setServers(ArrayList<Server> servers) {
		this.servers = servers;
	}

	
}
