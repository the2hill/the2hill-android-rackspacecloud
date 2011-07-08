package com.rackspace.cloud.files.api.client;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.protocol.RequestExpectContinue;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.util.Log;

import com.rackspace.cloud.files.api.client.parsers.ContainerObjectXMLparser;
import com.rackspace.cloud.servers.api.client.Account;
import com.rackspace.cloud.servers.api.client.CloudServersException;
import com.rackspace.cloud.servers.api.client.EntityManager;
import com.rackspacecloud.android.helpers.ByteHelper;
import com.rackspacecloud.android.helpers.StringHelper;

public class ContainerObjectManager extends EntityManager {
	public String LOG = "ContainerObjectManager";
	
	public static final String storageToken = Account.getStorageToken();
	public static int bytes;
	private Context context;

	public ContainerObjectManager(Context context) {
		this.context = context;
	}

	public ArrayList<ContainerObjects> createList(boolean detail,
			String passName) throws CloudServersException {

		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpGet get = new HttpGet(Account.getStorageUrl() + "/" + passName + "?format=xml");
		ArrayList<ContainerObjects> files = new ArrayList<ContainerObjects>();

		get.addHeader("Content-Type", "application/xml");
		get.addHeader("X-Storage-Token", storageToken);

		try {
			HttpResponse resp = httpclient.execute(get);
			BasicResponseHandler responseHandler = new BasicResponseHandler();
			String body = responseHandler.handleResponse(resp);

			if (resp.getStatusLine().getStatusCode() == 200 || resp.getStatusLine().getStatusCode() == 203) {
				ContainerObjectXMLparser filesXMLParser = new ContainerObjectXMLparser();
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				XMLReader xmlReader = saxParser.getXMLReader();
				xmlReader.setContentHandler(filesXMLParser);
				xmlReader.parse(new InputSource(new StringReader(body)));
				files = filesXMLParser.getViewFiles();
			} else {
				Log.e("Error", "The operation failed due to status code: " + resp.getStatusLine().getStatusCode());
			}
		} catch (ClientProtocolException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (ParserConfigurationException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (SAXException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
		}
		return files;

	}

	public HttpResponse deleteObject(String Container, String Object)
			throws CloudServersException {
		HttpResponse resp = null;
		CustomHttpClient httpclient = new CustomHttpClient(context);
		HttpDelete deleteObject = new HttpDelete(Account.getStorageUrl() + "/"
				+ Container + "/" + Object);
		Log.v(LOG, "the container (deleteObject) vairble " + Container + " "
				+ Object);

		deleteObject.addHeader("X-Auth-Token", Account.getAuthToken());
		httpclient.removeRequestInterceptorByClass(RequestExpectContinue.class);

		try {
			resp = httpclient.execute(deleteObject);
		} catch (ClientProtocolException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
		} catch (FactoryConfigurationError e) {
			Log.e("Error", "The operation failed due to status code: " + e.getLocalizedMessage());
		}
		return resp;
	}

	public HttpResponse uploadFromShareIntent(String imageUri,String containerName) {
		HttpResponse putResponse = null;
		StringHelper sHelper = new StringHelper();
		String storageUri;

		String fileName = sHelper.splitUriForFileName(imageUri);
		storageUri = Account.getStorageUrl() + "/" + containerName + "/" + fileName;

		HttpClient httpclient = new CustomHttpClient(context);
		HttpPut httpput = new HttpPut(storageUri);
		httpput.getParams().setIntParameter("http.socket.timeout", 100000);

		File file = new File(imageUri);
		ByteArrayEntity bae = null;
		try {
			bae = new ByteArrayEntity(ByteHelper.getBytesFromFile(file));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		httpput.setEntity(bae);
		httpput.addHeader("X-Auth-Token", Account.getAuthToken());
		httpput.addHeader("Content-Type", "application/octet-stream");

		try {
			putResponse = httpclient.execute(httpput);
		} catch (ClientProtocolException e) {
			Log.e("Upload file", "Error in http connection " + e.toString());
		} catch (IOException e) {
			Log.e("Upload file", "Error in http connection " + e.toString());
		}
		return putResponse;
	}
}