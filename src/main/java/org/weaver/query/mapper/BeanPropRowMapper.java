package org.weaver.query.mapper;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.weaver.config.entity.ViewField;
import org.weaver.service.Translator;
import org.weaver.view.util.Utils;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class BeanPropRowMapper<T> extends BeanPropertyRowMapper<T> {
	
	private static final Logger log = LoggerFactory.getLogger(BeanPropRowMapper.class);

	
	private List<ViewField> fieldList;
	//Collector
	private EnumFieldValueCollector enumFieldValueCollector = new EnumFieldValueCollector();
	
	private Field[] fields;
	
	public BeanPropRowMapper(Class<T> mappedClass, boolean checkFullyPopulated) {
		super(mappedClass, checkFullyPopulated);
		this.fields = mappedClass.getDeclaredFields();
	}

	public BeanPropRowMapper(Class<T> mappedClass) {
		super(mappedClass);
		this.fields = mappedClass.getDeclaredFields();
	}	
	
	public void setFieldList(List<ViewField> fieldList) {
		this.fieldList = fieldList;
	}

	public List<ViewField> getFieldList() {
		return fieldList;
	}	

	public void setFieldMap(Map<String, ViewField> fieldMap) {
		enumFieldValueCollector.setFieldMap(fieldMap);
	}
	
	public void setTranslator(Translator translator) {
		enumFieldValueCollector.setTranslator(translator);
	}

	public Map<String, Map<String, Map<String, Object>>> getEnumFieldsValues() {
		return enumFieldValueCollector.getEnumFieldsValues();
	}
	
	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		T item = super.mapRow(rs, rowNum);
		for (Field beanField : this.fields) {
			Object value = enumFieldValueCollector.getFieldValue(rs,beanField.getName());
			if(value!=null) {
				try {
					beanField.setAccessible(true);
					Object _value = Utils.convertEntityValue(value,beanField.getType());
 					beanField.set(item, _value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return item;
	}

}
