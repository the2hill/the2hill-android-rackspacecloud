/**
 * 
 */
package com.rackspace.cloud.servers.api.client;


/**
 * @author Mike Mayo - mike.mayo@rackspace.com - twitter.com/greenisus
 *
 */
public class Account {
	
	private static String username;
	private static String apiKey;
	private static String authToken;
	private static String authServer;
	private static String serverUrl;
	private static String storageUrl;
	private static String storageToken;
	private static String cdnManagementUrl;
	
	/**
	 * @return the serverUrl
	 */
	public static String getServerUrl() {
		return serverUrl;
	}

	/**
	 * @param serverUrl the serverUrl to set
	 */
	public static void setServerUrl(String serverUrl) {
		Account.serverUrl = serverUrl;
	}

	/**
	 * @return the storageUrl
	 */
	public static String getStorageUrl() {
		return storageUrl;
	}
	/**
	 * @return the storageToken
	 */
	public static String getStorageToken() {
		return storageToken;
	}
	/**
	 * @param storageUrl the storageUrl to set
	 */
	public static void setStorageUrl(String storageUrl) {
		Account.storageUrl = storageUrl;
	}

	/**
	 * @return the cdnManagementUrl
	 */
	public static String getCdnManagementUrl() {
		return cdnManagementUrl;
	}

	/**
	 * @param cdnManagementUrl the cdnManagementUrl to set
	 */
	public static void setCdnManagementUrl(String cdnManagementUrl) {
		Account.cdnManagementUrl = cdnManagementUrl;
	}

	/**
	 * @return the authToken
	 */
	public static String getAuthToken() {
		return authToken;
	}

	/**
	 * @param authToken the authToken to set
	 */
	public static void setAuthToken(String authToken) {
		Account.authToken = authToken;
	}

	/**
	 * @return the authToken
	 */
	public static String getAuthServer() {
		return authServer;
	}

	/**
	 * @param authToken the authToken to set
	 */
	public static void setAuthServer(String authServer) {
		Account.authServer = authServer;
	}

	
	/**
	 * @return the username
	 */
	public static String getUsername() {
		return username;
	}
	
	/**
	 * @param username the username to set
	 */
	public static void setUsername(String username) {
		Account.username = username;
	}
	
	/**
	 * @return the apiKey
	 */
	public static String getApiKey() {
		return apiKey;
	}
	
	/**
	 * @param apiKey the apiKey to set
	 */
	public static void setApiKey(String apiKey) {
		Account.apiKey = apiKey;
	}
   /**
    */
	public static void setStorageToken(String storageToken) {
		Account.storageToken = storageToken;
		
	}
	
}

