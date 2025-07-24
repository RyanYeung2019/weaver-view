package org.weaver.view.query.entity;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class KeyValueSettingEn {
	
	String dataSourceName;
	
	String table;
	
	String typeField;
	
	String type;
	
	String keyField;
	
	String valueField;
	
	
	public KeyValueSettingEn(String table, String typeField, String keyField, String valueField, String type) {
		super();
		this.table = table;
		this.typeField = typeField;
		this.keyField = keyField;
		this.valueField = valueField;
		this.type = type;
	}

	public KeyValueSettingEn(String dataSourceName, String table, String typeField, String keyField,
			String valueField,String type) {
		super();
		this.dataSourceName = dataSourceName;
		this.table = table;
		this.typeField = typeField;
		this.keyField = keyField;
		this.valueField = valueField;
		this.type = type;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table.replace("'","''");
	}

	public String getTypeField() {
		return typeField;
	}

	public void setTypeField(String typeField) {
		this.typeField = typeField.replace("'","''");
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKeyField() {
		return keyField;
	}

	public void setKeyField(String keyField) {
		this.keyField = keyField.replace("'","''");
	}

	public String getValueField() {
		return valueField;
	}

	public void setValueField(String valueField) {
		this.valueField = valueField.replace("'","''");
	}
	
	
}
