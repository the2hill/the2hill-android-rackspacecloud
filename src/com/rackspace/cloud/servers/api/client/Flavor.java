/**
 * 
 */
package com.rackspace.cloud.servers.api.client;

import java.util.TreeMap;

/**
 * @author Mike Mayo - mike.mayo@rackspace.com - twitter.com/greenisus
 *
 */
public class Flavor extends Entity {

	private static final long serialVersionUID = 7501003956094662782L;
	private static TreeMap<String, Flavor> flavors;
	private String disk;
	private String ram;
	
	/**
	 * @return the disk
	 */
	public String getDisk() {
		return disk;
	}
	/**
	 * @param disk the disk to set
	 */
	public void setDisk(String disk) {
		this.disk = disk;
	}
	/**
	 * @return the ram
	 */
	public String getRam() {
		return ram;
	}
	/**
	 * @param ram the ram to set
	 */
	public void setRam(String ram) {
		this.ram = ram;
	}
	/**
	 * @return the flavors
	 */
	public static TreeMap<String, Flavor> getFlavors() {
		return flavors;
	}
	/**
	 * @param flavors the flavors to set
	 */
	public static void setFlavors(TreeMap<String, Flavor> flavors) {
		Flavor.flavors = flavors;
	}
	
	
}
