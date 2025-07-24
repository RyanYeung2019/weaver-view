package org.weaver.config.entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;

import org.weaver.view.query.entity.EnumItemEn;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class ViewField {
	
	public static String FIELDTYPE_STRING = "string";
	public static String FIELDTYPE_BOOLEAN = "boolean";
	public static String FIELDTYPE_DATE = "date";
	public static String FIELDTYPE_TIME = "time";
	public static String FIELDTYPE_DATETIME = "datetime";
	public static String FIELDTYPE_NUMBER = "number";
	public static String FIELDTYPE_OTHER = "other";	

	private String field; // CamelCase Field Style

	private String fieldDb; // Database Field Style

	private Boolean nullable;

	private String type; // TypeScript Types

	private String typeDb;

	private String typeJava;
	
	private int sqlType;

	private Integer preci;

	private Integer scale;

	private EnumApiEn enumApi;

	private Map<String, String> enumDataMap;

	private List<EnumItemEn> enumDataList;

	private String enumDataString; // Refer To Language Setting

	private JSONObject props = new JSONObject();

	public ViewField(String field) {
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

	public EnumApiEn getEnumApi() {
		return enumApi;
	}

	public void setEnumApi(EnumApiEn enumApi) {
		this.enumApi = enumApi;
	}

	public Map<String, String> getEnumDataMap() {
		return enumDataMap;
	}

	public void setEnumDataMap(Map<String, String> enumDataMap) {
		this.enumDataMap = enumDataMap;
	}

	public List<EnumItemEn> getEnumDataList() {
		return enumDataList;
	}

	public void setEnumDataList(List<EnumItemEn> enumDataList) {
		this.enumDataList = enumDataList;
	}

	public String getEnumDataString() {
		return enumDataString;
	}

	public void setEnumDataString(String enumDataString) {
		this.enumDataString = enumDataString;
	}

	public JSONObject getProps() {
		return props;
	}

	public void setProps(JSONObject props) {
		this.props = props;
	}

	public int getSqlType() {
		return sqlType;
	}

	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
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
		ViewField other = (ViewField) obj;
		return Objects.equals(field, other.field);
	}

}
