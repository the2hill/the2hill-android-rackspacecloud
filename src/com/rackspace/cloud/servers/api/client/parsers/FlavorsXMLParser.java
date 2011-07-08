/**
 * 
 */
package com.rackspace.cloud.servers.api.client.parsers;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.rackspace.cloud.servers.api.client.Flavor;

/**
 * @author Mike Mayo - mike.mayo@rackspace.com - twitter.com/greenisus
 *
 */
public class FlavorsXMLParser extends DefaultHandler {
	private Flavor flavor;
	private ArrayList<Flavor> flavors;
	private StringBuffer currentData;

	public void startDocument() {
	}

	public void endDocument() {
	}

	public void startElement(String uri, String name, String qName, Attributes atts) {

		currentData = new StringBuffer();
		if ("flavors".equals(name)) {
			flavors = new ArrayList<Flavor>();
		} else if ("flavor".equals(name)) {
			flavor = new Flavor();
			flavor.setDisk(atts.getValue("disk"));
			flavor.setRam(atts.getValue("ram"));
			flavor.setId(atts.getValue("id"));
			flavor.setName(atts.getValue("name"));
		}
	}

	public void endElement(String uri, String name, String qName) {
		if ("flavor".equals(name)) {
			if (flavors != null) {
				flavors.add(flavor);
			}
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

	public Flavor getFlavor() {
		return flavor;
	}

	public void setFlavor(Flavor flavor) {
		this.flavor = flavor;
	}

	/**
	 * @return the servers
	 */
	public ArrayList<Flavor> getFlavors() {
		return flavors;
	}

	/**
	 * @param servers the servers to set
	 */
	public void setFlavors(ArrayList<Flavor> flavors) {
		this.flavors = flavors;
	}

}
