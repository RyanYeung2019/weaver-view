package org.weaver.config.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class ViewEn {

	private String viewId;

	private String name;

	private String remark;// 查询备注

	private String dataSource;// 使用的数据源

	private String sourceType;// 数据源类型

	private LinkedHashMap<String, String> param;

	private String sql;// 查询语句

	private List<ViewField> listFields = new ArrayList<>();// 查询字段信息

	private Map<String, ViewField> fieldMap;// 索引字段信息 key is CamelCase Field Style

	private JSONObject meta = new JSONObject();// 内容只供java端使用

	private JSONObject props = new JSONObject();// 通过api直接输出到前端
	
	//for tree
	private String treeId;
	
	private String treeParent;
	
	private List<String> treeSearch;//searching CamelCase field list
	
	private int status = 0 ;//0 load success, -1 error
	

	public String getViewId() {
		return viewId;
	}

	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public LinkedHashMap<String, String> getParam() {
		return param;
	}

	public void setParam(LinkedHashMap<String, String> param) {
		this.param = param;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<ViewField> getListFields() {
		return listFields;
	}

	public void setListFields(List<ViewField> listFields) {
		this.listFields = listFields;
	}

	public Map<String, ViewField> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(Map<String, ViewField> fieldMap) {
		this.fieldMap = fieldMap;
	}

	public JSONObject getMeta() {
		return meta;
	}

	public void setMeta(JSONObject meta) {
		this.meta = meta;
	}

	public JSONObject getProps() {
		return props;
	}

	public void setProps(JSONObject props) {
		this.props = props;
	}

	public String getTreeId() {
		return treeId;
	}

	public void setTreeId(String treeId) {
		this.treeId = treeId;
	}

	public String getTreeParent() {
		return treeParent;
	}

	public List<String> getTreeSearch() {
		return treeSearch;
	}

	public void setTreeSearch(List<String> treeSearch) {
		this.treeSearch = treeSearch;
	}

	public void setTreeParent(String treeParent) {
		this.treeParent = treeParent;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}


}
