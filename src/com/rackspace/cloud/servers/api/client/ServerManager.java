/**
 * 
 */
package com.rackspace.cloud.servers.api.client;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.RequestExpectContinue;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.rackspace.cloud.servers.api.client.parsers.CloudServersFaultXMLParser;
import com.rackspace.cloud.servers.api.client.parsers.ServersXMLParser;

/**
 * @author Mike Mayo - mike.mayo@rackspace.com - twitter.com/greenisus
 *
 */
public class ServerManager extends EntityManager {

	public static final String SOFT_REBOOT = "SOFT";
	public static final String HARD_REBOOT = "HARD";
	
	public void create(Server entity) throws CloudServersException {
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost post = new HttpPost(Account.getServerUrl() + "/servers.xml");
		
		post.addHeader("X-Auth-Token", Account.getAuthToken());
		post.addHeader("Content-Type", "application/xml");

		StringEntity tmp = null;
		try {
			tmp = new StringEntity(entity.toXML());
		} catch (UnsupportedEncodingException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}
		post.setEntity(tmp);
		
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		try {			
			HttpResponse resp = httpclient.execute(post);
		    BasicResponseHandler responseHandler = new BasicResponseHandler();
		    String body = responseHandler.handleResponse(resp);
		    
		    if (resp.getStatusLine().getStatusCode() == 202) {		    	
		    	ServersXMLParser serversXMLParser = new ServersXMLParser();
		    	SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		    	XMLReader xmlReader = saxParser.getXMLReader();
		    	xmlReader.setContentHandler(serversXMLParser);
		    	xmlReader.parse(new InputSource(new StringReader(body)));		    	
		    	entity = serversXMLParser.getServer();		    	
		    } else {
		    	CloudServersFaultXMLParser parser = new CloudServersFaultXMLParser();
		    	SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		    	XMLReader xmlReader = saxParser.getXMLReader();
		    	xmlReader.setContentHandler(parser);
		    	xmlReader.parse(new InputSource(new StringReader(body)));		    	
		    	CloudServersException cse = parser.getException();		    	
		    	throw cse;
		    }
		} catch (ClientProtocolException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (ParserConfigurationException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (SAXException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}	
	}

	public ArrayList<Server> createList(boolean detail) throws CloudServersException {
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(Account.getServerUrl() + "/servers/detail.xml" + cacheBuster());
		ArrayList<Server> servers = new ArrayList<Server>();
		
		get.addHeader("X-Auth-Token", Account.getAuthToken());
		
		try {			
			HttpResponse resp = httpclient.execute(get);		    
		    BasicResponseHandler responseHandler = new BasicResponseHandler();
		    String body = responseHandler.handleResponse(resp);
		    
		    if (resp.getStatusLine().getStatusCode() == 200 || resp.getStatusLine().getStatusCode() == 203) {		    	
		    	ServersXMLParser serversXMLParser = new ServersXMLParser();
		    	SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		    	XMLReader xmlReader = saxParser.getXMLReader();
		    	xmlReader.setContentHandler(serversXMLParser);
		    	xmlReader.parse(new InputSource(new StringReader(body)));		    	
		    	servers = serversXMLParser.getServers();		    	
		    } else {
		    	CloudServersFaultXMLParser parser = new CloudServersFaultXMLParser();
		    	SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		    	XMLReader xmlReader = saxParser.getXMLReader();
		    	xmlReader.setContentHandler(parser);
		    	xmlReader.parse(new InputSource(new StringReader(body)));		    	
		    	CloudServersException cse = parser.getException();		    	
		    	throw cse;
		    }
		} catch (ClientProtocolException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (ParserConfigurationException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (SAXException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}
		
		
		return servers;
	}

	public Server find(long id) throws CloudServersException {
		Server server = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(Account.getServerUrl() + "/servers/" + id + ".xml" + cacheBuster());
		
		get.addHeader("X-Auth-Token", Account.getAuthToken());
		
		try {			
			HttpResponse resp = httpclient.execute(get);		    
		    BasicResponseHandler responseHandler = new BasicResponseHandler();
		    String body = responseHandler.handleResponse(resp);
		    
		    if (resp.getStatusLine().getStatusCode() == 200 || resp.getStatusLine().getStatusCode() == 203) {		    	
		    	ServersXMLParser serversXMLParser = new ServersXMLParser();
		    	SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		    	XMLReader xmlReader = saxParser.getXMLReader();
		    	xmlReader.setContentHandler(serversXMLParser);
		    	xmlReader.parse(new InputSource(new StringReader(body)));		    	
		    	server = serversXMLParser.getServer();		    	
		    } else {
		    	CloudServersFaultXMLParser parser = new CloudServersFaultXMLParser();
		    	SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		    	XMLReader xmlReader = saxParser.getXMLReader();
		    	xmlReader.setContentHandler(parser);
		    	xmlReader.parse(new InputSource(new StringReader(body)));		    	
		    	CloudServersException cse = parser.getException();		    	
		    	throw cse;
		    }
		} catch (ClientProtocolException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (ParserConfigurationException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (SAXException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}
		
		return server;
	}

	public HttpResponse reboot(Server server, String rebootType) throws CloudServersException {
		HttpResponse resp = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost post = new HttpPost(Account.getServerUrl() + "/servers/" + server.getId() + "/action.xml");
				
		post.addHeader("X-Auth-Token", Account.getAuthToken());
		post.addHeader("Content-Type", "application/xml");

		StringEntity tmp = null;
		try {
			tmp = new StringEntity("<reboot xmlns=\"http://docs.rackspacecloud.com/servers/api/v1.0\" type=\"" + rebootType + "\"/>");
		} catch (UnsupportedEncodingException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}
		post.setEntity(tmp);
		
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		try {			
			resp = httpclient.execute(post);			
		} catch (ClientProtocolException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}	
		return resp;
	}

	public HttpResponse resize(Server server, int flavorId) throws CloudServersException {
		HttpResponse resp = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost post = new HttpPost(Account.getServerUrl() + "/servers/" + server.getId() + "/action.xml");
				
		post.addHeader("X-Auth-Token", Account.getAuthToken());
		post.addHeader("Content-Type", "application/xml");
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		StringEntity tmp = null;
		try {
			tmp = new StringEntity("<resize xmlns=\"http://docs.rackspacecloud.com/servers/api/v1.0\" flavorId=\"" + flavorId + "\"/>");
		} catch (UnsupportedEncodingException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}
		post.setEntity(tmp);

		try {			
			resp = httpclient.execute(post);
		} catch (ClientProtocolException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}	
		return resp;
	}


	public HttpResponse delete(Server server) throws CloudServersException {
		HttpResponse resp = null;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpDelete delete = new HttpDelete(Account.getServerUrl() + "/servers/" + server.getId() + ".xml");
				
		delete.addHeader("X-Auth-Token", Account.getAuthToken());
		delete.addHeader("Content-Type", "application/xml");
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		try {			
			resp = httpclient.execute(delete);
		} catch (ClientProtocolException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (IOException e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		} catch (FactoryConfigurationError e) {
			CloudServersException cse = new CloudServersException();
			cse.setMessage(e.getLocalizedMessage());
			throw cse;
		}	
		return resp;
	}

}
