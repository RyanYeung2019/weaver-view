package org.weaver.view.query.entity;

public class SortByField {

	public static final String ASC = "ASC";
	public static final String DESC = "DESC";

	private String fieldName;// CamelCase Style
	private String type = ASC;

	public SortByField(String fieldName, String type) {
		this.fieldName = fieldName;
		this.type = type==null?ASC:type;
	}
	public SortByField(String fieldName) {
		this.fieldName = fieldName;
		this.type = ASC;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "SortByField [fieldName=" + fieldName + ", type=" + type + "]";
	}
}
