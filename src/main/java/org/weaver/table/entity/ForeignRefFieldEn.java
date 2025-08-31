package org.weaver.table.entity;

import java.io.Serializable;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class ForeignRefFieldEn  implements Serializable {

	private static final long serialVersionUID = -7142265875163373676L;

	private String field;
	private String fieldDb;
	private String fieldDbSql;
    private String refTable;
    private String refField;
    private String refFieldDb;
    private Integer sortField;

	public ForeignRefFieldEn(String field,String fieldDb) {
		super();
		this.field = field;
		this.fieldDb = fieldDb;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getRefTable() {
		return refTable;
	}

	public void setRefTable(String refTable) {
		this.refTable = refTable;
	}

	public String getRefField() {
		return refField;
	}

	public void setRefField(String refField,String refFieldDb) {
		this.refField = refField;
		this.refFieldDb = refFieldDb;
	}

	public Integer getSortField() {
		return sortField;
	}

	public void setSortField(Integer sortField) {
		this.sortField = sortField;
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


	public String getRefFieldDb() {
		return refFieldDb;
	}

	public void setRefFieldDb(String refFieldDb) {
		this.refFieldDb = refFieldDb;
	}

	@Override
	public String toString() {
		return "ForeignRefFieldEn [field=" + field + ", refTable=" + refTable + ", refField=" + refField
				+ ", sortField=" + sortField + "]";
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
		ForeignRefFieldEn other = (ForeignRefFieldEn) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}



}
