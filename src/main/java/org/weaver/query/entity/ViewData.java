package org.weaver.query.entity;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class ViewData<T> {

	public static final String AGGRS_SIZE = "size";
	
	private String name;
	private String desc;
	private List<T> data;
	private Map<String, Map<String, String>> valueMapping;// field,key,value
	private String message;
	private Date startTime;
	private Date endTime;
	private List<FieldInfo> fields;
	private LinkedHashMap<String,Object> aggrs;
	private JSONObject props;
	private String primarySearchValue;
	private String remark;

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

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public Map<String, Map<String, String>> getValueMapping() {
		return valueMapping;
	}

	public void setValueMapping(Map<String, Map<String, String>> valueMapping) {
		this.valueMapping = valueMapping;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public List<FieldInfo> getFields() {
		return fields;
	}

	public void setFields(List<FieldInfo> fields) {
		this.fields = fields;
	}

	public LinkedHashMap<String, Object> getAggrs() {
		return aggrs;
	}

	public void setAggrs(LinkedHashMap<String, Object> aggrs) {
		this.aggrs = aggrs;
	}

	public JSONObject getProps() {
		return props;
	}

	public void setProps(JSONObject props) {
		this.props = props;
	}

	public String getPrimarySearchValue() {
		return primarySearchValue;
	}

	public void setPrimarySearchValue(String primarySearchValue) {
		this.primarySearchValue = primarySearchValue;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
