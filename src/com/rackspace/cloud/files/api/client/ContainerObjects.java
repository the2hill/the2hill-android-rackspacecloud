package com.rackspace.cloud.files.api.client;

import com.rackspace.cloud.servers.api.client.Entity;

/**
 * @author Phillip Toohill dead2hill@gmail.com
 *
 */
public class ContainerObjects extends Entity {

	private static final long serialVersionUID = 5994739895998309675L;
	private String object;
	private String hash;
	private String lastMod;
	private int bytes;
	private String cname;
	private String contentType;

	/**
	 * 
	 * @return the object
	 */
	public String getObject() {
		return object;
	}
	/**
	 * 
	 * @param object from container
	 */
	public void setObject(String object)	{
		this.object = object;
	}
	/**
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}
	/**
	 * @param hash hash of object
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}
	/**
	 * 
	 * @return the LastMod ification of file
	 */
	public String getLastMod() {
		return lastMod;
	}
	
	/**
	 * 
	 * @param lastMod that LastMod is set to
	 */
	public void setLastMod(String lastMod) {
		this.lastMod = lastMod;
	}
	/**
	 * 
	 * @return the object's name
	*/
	public String getCName() {
		return cname;
	}
	/**
	 * 
	 * @param name the object is set to
	 */
	public void setCName(String cname) {
		this.cname = cname;
	}
	/**
	 * 
	 * @return the objects size
	 */
	public int getBytes() {
		return bytes;
	}
	/**
	 * 
	 * @param the bytes the object is set to
	 */
	public void setBytes(int bytes) {
		this.bytes = bytes;
	}
	/**
	 * 
	 * @param contentType the object is set to
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
		
	}
	/**
	 * 
	 * @return the objects content type
	 */
	public String getContentType(){
		return contentType;
	}
}   

