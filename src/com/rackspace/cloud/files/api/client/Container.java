package com.rackspace.cloud.files.api.client;

import com.rackspace.cloud.servers.api.client.Entity;

/**
 * @author Phillip Toohill dead2hill@gmail.com
 * 
 */
public class Container extends Entity {

	private static final long serialVersionUID = 5994739895998309675L;

	// Regular attributes
	private String name;
	private int count;
	private long bytes;

	// CDN attributes
	private boolean cdnEnabled;
	private int Ttl;
	public String cdnUrl;
	public boolean logRetention;

	public String toXML() {
		String xml = "";
		xml = "<container xmlns=\"http://docs.rackspacecloud.com/servers/api/v1.0\" name=\""
				+ getName() + "\"></server>";
		return xml;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the bytes
	 */
	public long getBytes() {
		return bytes;
	}

	/**
	 * @param l
	 *            the bytes to set
	 */
	public void setBytes(long l) {
		this.bytes = l;
	}

	/**
	 * @return the cdnEnabled
	 */
	public boolean isCdnEnabled() {
		return cdnEnabled;
	}

	/**
	 * @param cdnEnabled
	 *            the cdnEnabled to set
	 */
	public void setCdnEnabled(boolean cdnEnabled) {
		this.cdnEnabled = cdnEnabled;
	}

	/**
	 * @return the ttl
	 */
	public int getTtl() {
		return Ttl;
	}

	/**
	 * @param ttl
	 *            the ttl to set
	 */
	public void setTtl(int ttl) {
		Ttl = ttl;
	}

	/**
	 * @return the cdnUrl
	 */
	public String getCdnUrl() {
		return cdnUrl;
	}

	/**
	 * @param cdnUrl
	 *            the cdnUrl to set
	 */
	public void setCdnUrl(String cdnUrl) {
		this.cdnUrl = cdnUrl;
	}

	/**
	 * @return the logRetention
	 */
	public boolean isLogRetention() {
		return logRetention;
	}

	/**
	 * @param logRetention
	 *            the logRetention to set
	 */
	public void setLogRetention(boolean logRetention) {
		this.logRetention = logRetention;
	}

}
