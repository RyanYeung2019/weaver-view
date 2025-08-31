package org.weaver.table.entity;

import java.io.Serializable;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class PrimaryKeyEn implements Serializable {

	private static final long serialVersionUID = -48295481071718560L;

	private String field;
	private Integer sortField;
	private String fieldDb;
	private String fieldDbSql;

	public PrimaryKeyEn(String field,String fieldDb) {
		super();
		this.field = field;
		this.fieldDb = fieldDb;

	}

	public String getField() {
		return field;
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

	public void setField(String field) {
		this.field = field;
	}



	public Integer getSortField() {
		return sortField;
	}

	public void setSortField(Integer sortField) {
		this.sortField = sortField;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		PrimaryKeyEn other = (PrimaryKeyEn) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PrimaryKeyEn [field=" + field + ", sortField=" + sortField + ", fieldDb=" + fieldDb + ", fieldDbSql="
				+ fieldDbSql + "]";
	}






}
