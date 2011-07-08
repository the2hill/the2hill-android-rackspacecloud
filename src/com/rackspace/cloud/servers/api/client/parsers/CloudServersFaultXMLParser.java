/**
 * 
 */
package com.rackspace.cloud.servers.api.client.parsers;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.rackspace.cloud.servers.api.client.CloudServersException;

/**
 * @author Mike Mayo - mike.mayo@rackspace.com - twitter.com/greenisus
 *
 */
public class CloudServersFaultXMLParser extends DefaultHandler {

	//<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	//<cloudServersFault xmlns="http://docs.rackspacecloud.com/servers/api/v1.0" code="400">
	//<message>422 Unprocessable Entity: Cannot perform requested action until reboot is complete.</message>
	//<details>com.rackspace.cloud.service.servers.CloudServersFault: 422 Unprocessable Entity: Cannot perform requested action until reboot is complete.</details>
	//</cloudServersFault>

	private CloudServersException exception;
	private StringBuffer currentData;

	public void startDocument() {
		exception = new CloudServersException();
	}

	public void endDocument() {
	}

	public void startElement(String uri, String name, String qName, Attributes atts) {

		currentData = new StringBuffer();
		if ("cloudServersFault".equals(name)) {
			exception.setCode(Integer.parseInt(atts.getValue("code")));
		}
	}

	public void endElement(String uri, String name, String qName) {
		if ("message".equals(name)) {
			exception.setMessage(currentData.toString());
		} else if ("details".equals(name)) {
			exception.setDetails(currentData.toString());
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

	/**
	 * @return the exception
	 */
	public CloudServersException getException() {
		return exception;
	}

	/**
	 * @param exception the exception to set
	 */
	public void setException(CloudServersException exception) {
		this.exception = exception;
	}

	
}
