package com.rackspacecloud.android.helpers;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

public class FileBuilder {
	private static String directory = "/RackSpaceCloud/";

	public static File createFile(String objectName) {
		File file = null;
		String sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
		new File(sdCard + directory).mkdirs();		
		file = new File(sdCard + directory + objectName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
}
