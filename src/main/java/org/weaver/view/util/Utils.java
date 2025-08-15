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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class Utils {
	private static final Logger log = LoggerFactory.getLogger(Utils.class);

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
                Object value = entry.getValue();
    			if(value!=null) {
    				try {
    					field.setAccessible(true);
    					Object _value = Utils.convertEntityValue(value,field.getType());
    					field.set(instance, _value);
    				} catch (IllegalArgumentException | IllegalAccessException e) {
    					throw new RuntimeException(e);
    				}
    			}                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Object convertEntityValue(Object value,Class<?> type) {
    	Object result = value;
		if(!value.getClass().toString().equals(type.toString())) {
			if(value instanceof Integer && "class java.lang.Boolean".equals(type.toString())) {
				result=((Integer)value).intValue()==1?true:false;
			}
			if(value instanceof String && "class java.util.Date".equals(type.toString())) {
				SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					result = simpleDateFormat.parse((String)value);
				} catch (ParseException e) {
					result = null;
					log.error("parse error:",e);
				}
			}
			if(value instanceof LocalDateTime) {
				result = Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant());
			}
			if(value instanceof LocalDate) {
				result = Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant());
			}
		}   
		return result;
    }
}
