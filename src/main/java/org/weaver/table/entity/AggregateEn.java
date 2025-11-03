package org.weaver.table.entity;

public class AggregateEn {
	private String AggType;
	private String fieldDb;
	private String fieldDbSql;
	public String getAggType() {
		return AggType;
	}
	public void setAggType(String aggType) {
		AggType = aggType;
	}
	public String getFieldDb() {
		return fieldDb;
	}
	public void setFieldDb(String fieldDb) {
		this.fieldDb = fieldDb;
	}
	public String getFieldDbSql() {
		return fieldDbSql;
	}
	public void setFieldDbSql(String fieldDbSql) {
		this.fieldDbSql = fieldDbSql;
	}
	
}
