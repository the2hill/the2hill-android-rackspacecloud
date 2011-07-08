/**
 * 
 */
package com.rackspace.cloud.servers.api.client;

/**
 * @author Mike Mayo - mike.mayo@rackspace.com - twitter.com/greenisus
 *
 */
public class Server extends Entity {

	private static final long serialVersionUID = 5994739895998309675L;
	private String status;
	private String progress;
	private String hostId;
	private String flavorId;
	private String imageId;
	private String[] publicIpAddresses;
	private String[] privateIpAddresses;
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the progress
	 */
	public String getProgress() {
		return progress;
	}
	/**
	 * @param progress the progress to set
	 */
	public void setProgress(String progress) {
		this.progress = progress;
	}
	/**
	 * @return the hostId
	 */
	public String getHostId() {
		return hostId;
	}
	/**
	 * @param hostId the hostId to set
	 */
	public void setHostId(String hostId) {
		this.hostId = hostId;
	}
	/**
	 * @return the flavorId
	 */
	public String getFlavorId() {
		return flavorId;
	}
	/**
	 * @param flavorId the flavorId to set
	 */
	public void setFlavorId(String flavorId) {
		this.flavorId = flavorId;
	}
	/**
	 * @return the imageId
	 */
	public String getImageId() {
		return imageId;
	}
	/**
	 * @param imageId the imageId to set
	 */
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	/**
	 * @return the publicIpAddresses
	 */
	public String[] getPublicIpAddresses() {
		return publicIpAddresses;
	}
	/**
	 * @param publicIpAddresses the publicIpAddresses to set
	 */
	public void setPublicIpAddresses(String[] publicIpAddresses) {
		this.publicIpAddresses = publicIpAddresses;
	}
	/**
	 * @return the privateIpAddresses
	 */
	public String[] getPrivateIpAddresses() {
		return privateIpAddresses;
	}
	/**
	 * @param privateIpAddresses the privateIpAddresses to set
	 */
	public void setPrivateIpAddresses(String[] privateIpAddresses) {
		this.privateIpAddresses = privateIpAddresses;
	}
	/**
	 * @return the flavor
	 */
	public Flavor getFlavor() {
		Flavor flavor = Flavor.getFlavors().get(flavorId);
		if (flavor == null) {
			flavor = new Flavor();
		}
		return flavor;
	}
	
	/**
	 * @return the image
	 */
	public Image getImage() {
		Image image = Image.getImages().get(imageId);
		if (image == null) {
			image = new Image();
		}
		return image;
	}
	
	public String toXML() {
		String xml = "";
		xml = "<server xmlns=\"http://docs.rackspacecloud.com/servers/api/v1.0\" name=\"" + getName() 
			+ "\" imageId=\"" + imageId + "\" flavorId=\"" + flavorId + "\"></server>";
		return xml;
	}
	
}
