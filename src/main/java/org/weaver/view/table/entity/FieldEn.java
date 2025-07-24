package org.weaver.view.table.entity;

import java.io.Serializable;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class FieldEn  implements Serializable {

	private static final long serialVersionUID = -3750851797529096851L;

	private String fieldId;
    private String fieldName;
    private String fieldDb;
    private String dataType;
    private String type;
    private String sqlType;
    private Integer length;
    private Integer deci;
    private Boolean notNull;
    private Boolean autoInc;
    private String comment;
    private Integer sortField;

	public FieldEn( String fieldId) {
		super();
		this.fieldId = fieldId;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSqlType() {
		return sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getDeci() {
		return deci;
	}

	public void setDeci(Integer deci) {
		this.deci = deci;
	}

	public Boolean getNotNull() {
		return notNull;
	}

	public void setNotNull(Boolean notNull) {
		this.notNull = notNull;
	}

	public Boolean getAutoInc() {
		return autoInc;
	}

	public void setAutoInc(Boolean autoInc) {
		this.autoInc = autoInc;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
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


	@Override
	public String toString() {
		return "FieldEn [fieldId=" + fieldId + ", fieldName=" + fieldName + ", fieldDb=" + fieldDb + ", dataType="
				+ dataType + ", type=" + type + ", sqlType=" + sqlType + ", length=" + length + ", deci=" + deci
				+ ", notNull=" + notNull + ", autoInc=" + autoInc + ", comment=" + comment + ", sortField=" + sortField
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldId == null) ? 0 : fieldId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		FieldEn other = (FieldEn) obj;
		if (fieldId == null) {
			if (other.fieldId != null)
				return false;
		} else if (!fieldId.equals(other.fieldId))
			return false;
		return true;
	}




}
