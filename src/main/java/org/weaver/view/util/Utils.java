package org.weaver.view.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Utils {

	public static String urlDecoder(String value) {
		if (value == null)
			return null;
		try {
			return URLDecoder.decode(value, "utf-8").trim();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String urlEncoder(String value) {
		try {
			return URLEncoder.encode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String readTextFile(InputStream is) throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			StringBuffer d = new StringBuffer();
			String b;
			while ((b = br.readLine()) != null)
				d.append(b + "\n");
			return d.toString();
		}
	}
	
	public static String toPath(String path) {
		String result = path;
		result = result.contains("\\") ? result.replace("\\", "/") : result;
		result = !result.endsWith("/") ? (result + "/") : result;
		return result;
	}
}
