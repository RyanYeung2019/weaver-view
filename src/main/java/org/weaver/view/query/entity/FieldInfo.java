package org.weaver.view.query.entity;

import java.util.List;

import org.weaver.config.entity.EnumApiEn;

import com.alibaba.fastjson.JSONObject;

public class FieldInfo {
	private String field;
	private String name;
	private String desc;
	private String type;
	private Integer preci;
	private Integer scale;
	private Boolean nullable;
	private EnumApiEn enumApi;
	private List<EnumItemEn> enumDataList;
	private JSONObject props;
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
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
	public Boolean getNullable() {
		return nullable;
	}
	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}
	public EnumApiEn getEnumApi() {
		return enumApi;
	}
	public void setEnumApi(EnumApiEn enumApi) {
		this.enumApi = enumApi;
	}
	public List<EnumItemEn> getEnumDataList() {
		return enumDataList;
	}
	public void setEnumDataList(List<EnumItemEn> enumDataList) {
		this.enumDataList = enumDataList;
	}
	public JSONObject getProps() {
		return props;
	}
	public void setProps(JSONObject props) {
		this.props = props;
	}
	
	
}
