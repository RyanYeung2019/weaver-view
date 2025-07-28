package org.weaver.view.query.entity;

import java.util.LinkedHashMap;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 *
 */

public class KeyValueSettingEn {

	String dataSourceName;

	String table;

	String key;

	String value;

	String createDate;

	String updateDate;

	String createUser;

	String updateUser;

	LinkedHashMap<String,Object> typeData;
	
	String selectSql;
	String updateSql;
	String insertSql;


	public KeyValueSettingEn( String table, String key, String value,
			String createDate, String updateDate, String createUser, String updateUser,
			LinkedHashMap<String, Object> typeData) {
		super();
		this.table = table;
		this.key = key;
		this.value = value;
		this.createDate = createDate;
		this.updateDate = updateDate;
		this.createUser = createUser;
		this.updateUser = updateUser;
		this.typeData = typeData;
	}

	public KeyValueSettingEn(String dataSourceName, String table, String key, String value,
			String createDate, String updateDate, String createUser, String updateUser,
			LinkedHashMap<String, Object> typeData) {
		super();
		this.dataSourceName = dataSourceName;
		this.table = table;
		this.key = key;
		this.value = value;
		this.createDate = createDate;
		this.updateDate = updateDate;
		this.createUser = createUser;
		this.updateUser = updateUser;
		this.typeData = typeData;
	}

	public KeyValueSettingEn(String dataSourceName, String table, String key, String value, LinkedHashMap<String, Object> typeData) {
		super();
		this.dataSourceName = dataSourceName;
		this.table = table;
		this.key = key;
		this.value = value;
		this.typeData = typeData;
	}

	public KeyValueSettingEn(String dataSourceName, String table, String key, String value) {
		super();
		this.dataSourceName = dataSourceName;
		this.table = table;
		this.key = key;
		this.value = value;
	}

	public KeyValueSettingEn(String table, String key, String value) {
		super();
		this.table = table;
		this.key = key;
		this.value = value;
	}

    public KeyValueSettingEn(String table, String key, String value, LinkedHashMap<String, Object> typeData) {
        super();
        this.table = table;
        this.key = key;
        this.value = value;
        this.typeData = typeData;
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
		this.table = table;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public LinkedHashMap<String, Object> getTypeData() {
		return typeData;
	}

	public void setTypeData(LinkedHashMap<String, Object> typeData) {
		this.typeData = typeData;
	}

	public String getSelectSql() {
		return selectSql;
	}

	public void setSelectSql(String selectSql) {
		this.selectSql = selectSql;
	}

	public String getUpdateSql() {
		return updateSql;
	}

	public void setUpdateSql(String updateSql) {
		this.updateSql = updateSql;
	}

	public String getInsertSql() {
		return insertSql;
	}

	public void setInsertSql(String insertSql) {
		this.insertSql = insertSql;
	}


}
