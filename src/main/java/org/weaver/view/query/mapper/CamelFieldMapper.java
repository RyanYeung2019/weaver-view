package org.weaver.view.query.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import org.weaver.config.entity.ViewField;
import org.weaver.view.query.Translator;

public class CamelFieldMapper implements RowMapper<Map<String, Object>> {

	private List<ViewField> fieldList;

	private EnumFieldValueCollector enumFieldMapCollector = new EnumFieldValueCollector();

	public void setFieldList(List<ViewField> fieldList) {
		this.fieldList = fieldList;
	}

	public List<ViewField> getFieldList() {
		return fieldList;
	}

	public void setFieldMap(Map<String, ViewField> fieldMap) {
		enumFieldMapCollector.setFieldMap(fieldMap);
	}

	public void setTranslator(Translator translator) {
		enumFieldMapCollector.setTranslator(translator);
	}
	
	public Map<String, Map<String, Map<String, Object>>> getEnumFieldsValues() {
		return enumFieldMapCollector.getEnumFieldsValues();
	}

	@Override
	public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
		Map<String, Object> result = new LinkedHashMap<>();
		for (ViewField field : fieldList) {
			Object value = enumFieldMapCollector.getFieldValue(rs,field);
			if(value!=null)result.put(field.getField(), value);
		}
		return result;
	}

}
