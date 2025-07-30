package org.weaver.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

class FilterCriteria {

	private String field;

	private Object val;

	private boolean not = false;

	private String type;

	public static String NOT = "not";

	private String op;

	private List<FilterCriteria> filterCriteria;

	private Map<String, Object> paramMap = new HashMap<>();

	private String dbSql;
	private Object dbValue;
	private String dbKey;

	private String primarySearchValue;

	public Map<String, Object> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<String, Object> paramMap) {
		this.paramMap = paramMap;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Object getVal() {
		return val;
	}

	public void setVal(Object val) {
		this.val = val;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public List<FilterCriteria> getFilterCriteria() {
		return filterCriteria;
	}

	public void setFilterCriteria(List<FilterCriteria> filterCriteria) {
		this.filterCriteria = filterCriteria;
	}

	public boolean getNot() {
		return not;
	}

	public void setNot(boolean not) {
		this.not = not;
	}

	public Object getDbValue() {
		return dbValue;
	}

	public void setDbValue(Object dbValue) {
		this.dbValue = dbValue;
	}

	public String getDbSql() {
		return dbSql;
	}

	public void setDbSql(String dbSql) {
		this.dbSql = dbSql;
	}

	public String getDbKey() {
		return dbKey;
	}

	public void setDbKey(String dbKey) {
		this.dbKey = dbKey;
	}

	public String getPrimarySearchValue() {
		return primarySearchValue;
	}

	public void setPrimarySearchValue(String primarySearchValue) {
		this.primarySearchValue = primarySearchValue;
	}

}
