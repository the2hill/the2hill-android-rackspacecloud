package com.rackspacecloud.android.helpers;

public class StringHelper {

	public String splitUriForFileName(String uri) {
		
		String[] split = uri.split("\\/");
		String[] splitDots = split[5].split("\\.");
		String[] splitFile = split[5].split("\\s+");
//		String type = splitDots[3];
//		String uploadFile = splitFile[0] + "." + type;

		return splitFile[0];
	}
}
