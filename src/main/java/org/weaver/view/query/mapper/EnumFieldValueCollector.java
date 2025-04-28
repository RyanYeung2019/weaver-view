package org.weaver.view.query.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.weaver.config.entity.EnumApiEn;
import org.weaver.config.entity.ViewField;
import org.weaver.view.query.Translator;
import org.weaver.view.util.FormatterUtils;

class EnumFieldValueCollector {
	private Map<String, Map<String, Map<String, Object>>> enumFieldsValues;
	private Map<String, ViewField> fieldMap;
	private Translator translator;
	
	Map<String, Map<String, Map<String, Object>>> getEnumFieldsValues() {
		return enumFieldsValues;
	}
	
	void setFieldMap(Map<String, ViewField> fieldMap) {
		this.fieldMap = fieldMap;
	}
	
	Map<String, ViewField> getFieldMap() {
		return fieldMap;
	}

	void setTranslator(Translator translator) {
		this.translator = translator;
	}	

	Object getFieldValue(ResultSet rs, String fieldName)throws SQLException{
		ViewField field = fieldMap.get(fieldName);
		return getFieldValue(rs,field);
	}
	
	Object getFieldValue(ResultSet rs, ViewField field)throws SQLException{
		if(field==null)return null;
		String fieldCamel = field.getField();
		Object value = rs.getObject(field.getFieldDb());
		StringBuffer valueBuffer = new StringBuffer();
		boolean hasParam = false;
		EnumApiEn apiEnum = field.getEnumApi();
		if (apiEnum != null || field.getEnumDataMap() != null || field.getEnumDataString() != null) {
			if (enumFieldsValues == null)
				enumFieldsValues = new HashMap<String, Map<String, Map<String, Object>>>();
			Map<String, Map<String, Object>> data = enumFieldsValues.get(fieldCamel);
			if (data == null)
				data = new HashMap<>();
			Map<String, Object> newItem = new HashMap<>();
			if (apiEnum != null) {
				LinkedHashMap<String, String> paramMap = apiEnum.getParam();
				if (paramMap != null) {
					for (String key : paramMap.keySet()) {
						hasParam = true;
						String paramField = paramMap.get(key);
						String paramFieldDB = fieldMap.get(paramField).getFieldDb();
						Object paramValue = rs.getObject(paramFieldDB);
						newItem.put(paramField, paramValue);
						String paramValueStr = FormatterUtils.objectToString(paramValue);
						valueBuffer.append((paramValueStr == null ? "null" : paramValueStr) + "_");
					}
				}
			}
			String paramValueStr = FormatterUtils.objectToString(value);
			valueBuffer.append(paramValueStr == null ? "null" : paramValueStr);
			newItem.put(fieldCamel, value);
			data.put(valueBuffer.toString(), newItem);
			enumFieldsValues.put(fieldCamel, data);
		}
		Object result;
		if (hasParam)
			result = valueBuffer.toString();
		else {
			if (value != null && value instanceof String) {
				result = translator.tranText((String) value);
			}else {
				result = value;
			}
		}
		return result;
	}
	
	
}
