package com.rackspace.cloud.files.api.client;

import java.io.IOException;
import java.io.StringReader;
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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.protocol.RequestExpectContinue;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.text.Editable;
import android.util.Log;

import com.rackspace.cloud.files.api.client.parsers.ContainerXMLParser;
import com.rackspace.cloud.servers.api.client.Account;
import com.rackspace.cloud.servers.api.client.EntityManager;

public class ContainerManager extends EntityManager {
	private Context context;

	public ContainerManager(Context context) {
		this.context = context;
	}

	public HttpResponse create(Editable editable) throws Exception {
		HttpResponse resp = null;
		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpPut put = new HttpPut(Account.getStorageUrl() + "/" + editable);

		put.addHeader("X-Auth-Token", Account.getAuthToken());
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		try {
			resp = httpclient.execute(put);
		} catch (ClientProtocolException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		}
		return resp;
	}

	public ArrayList<Container> createCDNList(boolean detail) throws Exception {

		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpGet get = new HttpGet(Account.getCdnManagementUrl() + "?format=xml");
		ArrayList<Container> cdnContainers = new ArrayList<Container>();

		get.addHeader("X-Auth-Token", Account.getAuthToken());

		try {
			HttpResponse resp = httpclient.execute(get);
			BasicResponseHandler responseHandler = new BasicResponseHandler();
			String body = responseHandler.handleResponse(resp);

			if (resp.getStatusLine().getStatusCode() == 200) {
				ContainerXMLParser cdnContainerXMLParser = new ContainerXMLParser();
				SAXParser saxParser = SAXParserFactory.newInstance()
						.newSAXParser();
				XMLReader xmlReader = saxParser.getXMLReader();
				xmlReader.setContentHandler(cdnContainerXMLParser);

				xmlReader.parse(new InputSource(new StringReader(body)));
				cdnContainers = cdnContainerXMLParser.getContainers();
			} else {
				Log.e("Error", "The operation failed due to status code: " + resp.getStatusLine().getStatusCode());
			}
		} catch (ClientProtocolException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (ParserConfigurationException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (SAXException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		}

		return cdnContainers;
	}

	public HttpResponse enable(String container, String ttl, String logRet)
			throws Exception {
		HttpResponse resp = null;
		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpPut put = new HttpPut(Account.getCdnManagementUrl() + "/"
				+ container);

		put.addHeader("X-Auth-Token", Account.getAuthToken());
		put.addHeader("X-TTL", ttl);
		put.addHeader("X-Log-Retention", logRet);
		Log.v("cdn manager", ttl + container + logRet);
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		try {
			resp = httpclient.execute(put);
		} catch (ClientProtocolException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		}
		return resp;
	}

	public HttpResponse disable(String container) throws Exception {
		HttpResponse resp = null;
		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpDelete delete = new HttpDelete(Account.getCdnManagementUrl() + "/"
				+ container);

		delete.addHeader("X-Auth-Token", Account.getAuthToken());
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		try {
			resp = httpclient.execute(delete);
		} catch (ClientProtocolException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		}
		return resp;
	}

	public HttpResponse update(String container, String cdn, String ttl,
			String logRet) throws Exception {
		HttpResponse resp = null;
		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpPost post = new HttpPost(Account.getCdnManagementUrl() + "/"+ container);

		post.addHeader("X-Auth-Token", Account.getAuthToken());
		post.addHeader("X-TTL", ttl);
		post.addHeader("X-Log-Retention", logRet);
		post.addHeader("X-CDN-Enabled", cdn);
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		try {
			resp = httpclient.execute(post);
		} catch (ClientProtocolException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		}
		return resp;
	}

	public HttpResponse delete(String containerName)
			throws Exception {
		HttpResponse resp = null;
		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpDelete delete = new HttpDelete(Account.getStorageUrl() + "/"+ containerName);

		delete.addHeader("X-Auth-Token", Account.getAuthToken());
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		try {
			resp = httpclient.execute(delete);
		} catch (ClientProtocolException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		}
		return resp;
	}

	public HttpResponse purge(String containerName, String email,
			boolean isLogEnabled, Boolean cdnEnabled)
			throws Exception {
		// According to doc this should remove the object from the CDN.
		HttpResponse resp = null;
		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpDelete delete = new HttpDelete(Account.getCdnManagementUrl() + "/"
				+ containerName);

		delete.addHeader("X-Auth-Token", Account.getAuthToken());
		delete.addHeader("X-Purge-Email", email);

		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		try {
			resp = httpclient.execute(delete);
		} catch (ClientProtocolException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		}
		return resp;
	}

	public ArrayList<Container> createList(boolean detail)
			throws Exception {
		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpGet get = new HttpGet(Account.getStorageUrl() + "?format=xml");
		ArrayList<Container> containers = new ArrayList<Container>();

		get.addHeader("X-Storage-Token", Account.getStorageToken());
		get.addHeader("Content-Type", "application/xml");

		try {
			HttpResponse resp = httpclient.execute(get);
			BasicResponseHandler responseHandler = new BasicResponseHandler();
			String body = responseHandler.handleResponse(resp);

			if (resp.getStatusLine().getStatusCode() == 200
					|| resp.getStatusLine().getStatusCode() == 203) {
				ContainerXMLParser containerXMLParser = new ContainerXMLParser();
				SAXParser saxParser = SAXParserFactory.newInstance()
						.newSAXParser();
				XMLReader xmlReader = saxParser.getXMLReader();
				xmlReader.setContentHandler(containerXMLParser);

				xmlReader.parse(new InputSource(new StringReader(body)));
				containers = containerXMLParser.getContainers();
			} else {
				Log.e("Error", "The operation failed due to status code: " + resp.getStatusLine().getStatusCode());
				throw new Exception("The operation failed due to status code: " + resp.getStatusLine().getStatusCode());
			}
		} catch (ClientProtocolException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (ParserConfigurationException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (SAXException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
			throw new Exception("The operation failed due to status code: " + e.getLocalizedMessage());
		}

		return containers;
	}
}