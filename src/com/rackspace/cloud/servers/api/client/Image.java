/**
 * 
 */
package com.rackspace.cloud.servers.api.client;

import java.util.TreeMap;

import com.rackspacecloud.android.R;

/**
 * @author Mike Mayo - mike.mayo@rackspace.com - twitter.com/greenisus
 *
 */
public class Image extends Entity {

	private static final long serialVersionUID = -9020224299062520935L;
	private static TreeMap<String, Image> images;
	private String status;
	private String updated;
	
	public int iconResourceId() {
		int iconResourceId = R.drawable.cloudservers_icon; // default if unknown
		
		if ("2".equals(getId())) {
			iconResourceId = R.drawable.centos_icon;
		} else if ("3".equals(getId())) {
			iconResourceId = R.drawable.gentoo_icon;
		} else if ("4".equals(getId())) {
			iconResourceId = R.drawable.debian_icon;
		} else if ("5".equals(getId())) {
			iconResourceId = R.drawable.fedora_icon;
		} else if ("7".equals(getId())) {
			iconResourceId = R.drawable.centos_icon;
		} else if ("8".equals(getId())) {
			iconResourceId = R.drawable.ubuntu_icon;
		} else if ("9".equals(getId())) {
			iconResourceId = R.drawable.arch_icon;
		} else if ("10".equals(getId())) {
			iconResourceId = R.drawable.ubuntu_icon;
		} else if ("11".equals(getId())) {
			iconResourceId = R.drawable.ubuntu_icon;
		} else if ("12".equals(getId())) {
			iconResourceId = R.drawable.redhat_icon;
		} else if ("13".equals(getId())) {
			iconResourceId = R.drawable.fedora_icon;
		} else if ("4056".equals(getId())) {
			iconResourceId = R.drawable.fedora_icon;
		} else if ("14362".equals(getId())) {
			iconResourceId = R.drawable.ubuntu_icon;
		} else if ("23".equals(getId())) {
			iconResourceId = R.drawable.windows_icon;
		} else if ("24".equals(getId())) {
			iconResourceId = R.drawable.windows_icon;
		} else if ("28".equals(getId())) {
			iconResourceId = R.drawable.windows_icon;
		} else if ("29".equals(getId())) {
			iconResourceId = R.drawable.windows_icon;
		} else if ("31".equals(getId())) {
			iconResourceId = R.drawable.windows_icon;
		} else if ("14".equals(getId())) {
			iconResourceId = R.drawable.redhat_icon;
		} else if ("17".equals(getId())) {
			iconResourceId = R.drawable.fedora_icon;
		} else if ("19".equals(getId())) {
			iconResourceId = R.drawable.gentoo_icon;
		} else if ("187811".equals(getId())) {
			iconResourceId = R.drawable.centos_icon;
		} else if ("49".equals(getId())) {
			iconResourceId = R.drawable.ubuntu_icon;
		}
		
		return iconResourceId;
	}
	
	public int logoResourceId() {
		int logoResourceId = R.drawable.cloudservers_large; // default if unknown
		
		if ("2".equals(getId())) {
			logoResourceId = R.drawable.centos_large;
		} else if ("3".equals(getId())) {
			logoResourceId = R.drawable.gentoo_large;
		} else if ("4".equals(getId())) {
			logoResourceId = R.drawable.debian_large;
		} else if ("5".equals(getId())) {
			logoResourceId = R.drawable.fedora_large;
		} else if ("7".equals(getId())) {
			logoResourceId = R.drawable.centos_large;
		} else if ("8".equals(getId())) {
			logoResourceId = R.drawable.ubuntu_large;
		} else if ("9".equals(getId())) {
			logoResourceId = R.drawable.arch_large;
		} else if ("10".equals(getId())) {
			logoResourceId = R.drawable.ubuntu_large;
		} else if ("11".equals(getId())) {
			logoResourceId = R.drawable.ubuntu_large;
		} else if ("12".equals(getId())) {
			logoResourceId = R.drawable.redhat_large;
		} else if ("13".equals(getId())) {
			logoResourceId = R.drawable.fedora_large;
		} else if ("4056".equals(getId())) {
			logoResourceId = R.drawable.fedora_large;
		} else if ("14362".equals(getId())) {
			logoResourceId = R.drawable.ubuntu_large;
		} else if ("23".equals(getId())) {
			logoResourceId = R.drawable.windows_large;
		} else if ("24".equals(getId())) {
			logoResourceId = R.drawable.windows_large;
		} else if ("28".equals(getId())) {
			logoResourceId = R.drawable.windows_large;
		} else if ("29".equals(getId())) {
			logoResourceId = R.drawable.windows_large;
		} else if ("31".equals(getId())) {
			logoResourceId = R.drawable.windows_large;
		} else if ("14".equals(getId())) {
			logoResourceId = R.drawable.redhat_large;
		} else if ("17".equals(getId())) {
			logoResourceId = R.drawable.fedora_large;
		} else if ("19".equals(getId())) {
			logoResourceId = R.drawable.gentoo_large;
		} else if ("187811".equals(getId())) {
			logoResourceId = R.drawable.centos_large;
		} else if ("49".equals(getId())) {
			logoResourceId = R.drawable.ubuntu_large;
		}
		
		return logoResourceId;
	}
	
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
	 * @return the updated
	 */
	public String getUpdated() {
		return updated;
	}
	/**
	 * @param updated the updated to set
	 */
	public void setUpdated(String updated) {
		this.updated = updated;
	}

	/**
	 * @return the images
	 */
	public static TreeMap<String, Image> getImages() {
		return images;
	}

	/**
	 * @param images the images to set
	 */
	public static void setImages(TreeMap<String, Image> images) {
		Image.images = images;
	}
	
	
}
