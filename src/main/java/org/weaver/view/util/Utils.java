package org.weaver.view.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

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
	
    public static <T> Map<String, Object> entityToMap(T entity) {
        Map<String, Object> map = new HashMap<>();
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(entity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
    
    
    public static <T> void mapToEntity(Map<String, Object> map, T instance) {
        try {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Field field = instance.getClass().getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(instance, entry.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
    
}
