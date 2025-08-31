package org.weaver.table.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class FieldEn  implements Serializable {

	private static final long serialVersionUID = -3750851797529096851L;

	private String field; // CamelCase Field Style

	private String fieldDb; // Database Field Style
	
	private String fieldDbSql; 

	private Boolean nullable;

	private String type; // TypeScript Types

	private String typeDb;

	private String typeJava;
	
	private int sqlType;

	private Integer preci;

	private Integer scale;
    
    private Boolean autoInc;
    
    private String remark;
    
    private String defaultValue;

	public FieldEn(String field) {
		super();
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getFieldDb() {
		return fieldDb;
	}

	public void setFieldDb(String fieldDb) {
		this.fieldDb = fieldDb;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeDb() {
		return typeDb;
	}

	public void setTypeDb(String typeDb) {
		this.typeDb = typeDb;
	}

	public String getTypeJava() {
		return typeJava;
	}

	public void setTypeJava(String typeJava) {
		this.typeJava = typeJava;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}

	public Integer getPreci() {
		return preci;
	}

	public void setPreci(Integer preci) {
		this.preci = preci;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public Boolean getAutoInc() {
		return autoInc;
	}

	public void setAutoInc(Boolean autoInc) {
		this.autoInc = autoInc;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getFieldDbSql() {
		return fieldDbSql;
	}

	public void setFieldDbSql(String fieldDbSql) {
		this.fieldDbSql = fieldDbSql;
	}

	@Override
	public int hashCode() {
		return Objects.hash(field);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldEn other = (FieldEn) obj;
		return Objects.equals(field, other.field);
	}

	@Override
	public String toString() {
		return "FieldEn [field=" + field + ", fieldDb=" + fieldDb + ", nullable=" + nullable + ", type=" + type
				+ ", typeDb=" + typeDb + ", typeJava=" + typeJava + ", sqlType=" + sqlType + ", preci=" + preci
				+ ", scale=" + scale + ", autoInc=" + autoInc + ", remark=" + remark + ", defaultValue="
				+ defaultValue + "]";
	}

}
