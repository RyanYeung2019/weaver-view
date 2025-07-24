package org.weaver.view.util;

import java.util.Arrays;
import org.weaver.config.entity.ViewField;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

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
	
	public static String convertSqlType(String name, int sqlType) {
		String fieldType = ViewField.FIELDTYPE_OTHER;
		if (sqlType == java.sql.Types.BIT || sqlType == java.sql.Types.BOOLEAN)
			fieldType = ViewField.FIELDTYPE_BOOLEAN;

		if (sqlType == java.sql.Types.TINYINT || sqlType == java.sql.Types.ROWID || sqlType == java.sql.Types.SMALLINT
				|| sqlType == java.sql.Types.INTEGER || sqlType == java.sql.Types.BIGINT
				|| sqlType == java.sql.Types.FLOAT || sqlType == java.sql.Types.REAL || sqlType == java.sql.Types.DOUBLE
				|| sqlType == java.sql.Types.NUMERIC || sqlType == java.sql.Types.DECIMAL)
			fieldType = ViewField.FIELDTYPE_NUMBER;

		if (sqlType == java.sql.Types.CHAR || sqlType == java.sql.Types.VARCHAR || sqlType == java.sql.Types.LONGVARCHAR
				|| sqlType == java.sql.Types.NCHAR || sqlType == java.sql.Types.NVARCHAR
				|| sqlType == java.sql.Types.LONGNVARCHAR || sqlType == java.sql.Types.NCLOB
				|| sqlType == java.sql.Types.SQLXML)
			fieldType = ViewField.FIELDTYPE_STRING;

		if (sqlType == java.sql.Types.DATE)
			fieldType = ViewField.FIELDTYPE_DATE;

		if (sqlType == java.sql.Types.TIME || sqlType == java.sql.Types.TIME_WITH_TIMEZONE)
			fieldType = ViewField.FIELDTYPE_TIME;

		if (sqlType == java.sql.Types.TIMESTAMP || sqlType == java.sql.Types.TIMESTAMP_WITH_TIMEZONE) {
			fieldType = ViewField.FIELDTYPE_DATETIME;
			String fieldName = name.toLowerCase();
			if (fieldName.endsWith(ViewField.FIELDTYPE_TIME + "_only")) {
				fieldType = ViewField.FIELDTYPE_TIME;
			}
			if (fieldName.endsWith(ViewField.FIELDTYPE_DATE + "_only")) {
				fieldType = ViewField.FIELDTYPE_DATE;
			}
		}
		return fieldType;
	}
	


}
