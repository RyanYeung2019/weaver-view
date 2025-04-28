package org.weaver.view.util;

import java.util.Arrays;
import org.weaver.config.entity.ViewField;

public class FormatterUtils {

	public static String toCamelCase(String str) {
		String[] words = str.split("[-_.]");
		return Arrays.stream(words, 1, words.length).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
				.reduce(words[0], String::concat);
	}

	public static String objectToString(Object value) {
		if (value == null)
			return null;
		if (value instanceof String) {
			return (String) value;
		} else if (value instanceof Boolean) {
			boolean temp = (Boolean) value;
			return temp ? "true" : "false";
		} else {
			return value.toString();
		}
	}

	public static String objectToString(int value) {
		return String.valueOf(value);
	}

	public static String objectToString(long value) {
		return String.valueOf(value);
	}

	public static String objectToString(float value) {
		return String.valueOf(value);
	}

	public static String objectToString(double value) {
		return String.valueOf(value);
	}

	public static String objectToString(boolean value) {
		return value ? "true" : "false";
	}
	
	public static String convertFieldType(String name, String javaType) {
		String fieldType = null;
		if (javaType.equals("java.lang.String")) {
			fieldType = ViewField.FIELDTYPE_STRING;
		} else if (javaType.equals("java.lang.Boolean")) {
			fieldType = ViewField.FIELDTYPE_BOOLEAN;
		} else if (javaType.equals("java.sql.Date")) {
			fieldType = ViewField.FIELDTYPE_DATE;
		} else if (javaType.equals("java.sql.Time")) {
			fieldType = ViewField.FIELDTYPE_TIME;
		} else if (javaType.equals("java.sql.Timestamp")) {
			fieldType = ViewField.FIELDTYPE_DATETIME;
			String fieldName = name.toLowerCase();
			if (fieldName.endsWith(ViewField.FIELDTYPE_TIME + "_only")) {
				fieldType = ViewField.FIELDTYPE_TIME;
			}
			if (fieldName.endsWith(ViewField.FIELDTYPE_DATE + "_only")) {
				fieldType = ViewField.FIELDTYPE_DATE;
			}
		} else
			fieldType = ViewField.FIELDTYPE_NUMBER;
		if (fieldType == null) {
			throw new RuntimeException("Not recognize field:[" + name + "] type:[" + javaType + "]!");
		}
		return fieldType;
	}
}
