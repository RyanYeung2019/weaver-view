package org.weaver.view.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.weaver.config.entity.ViewEn;
import org.weaver.view.query.entity.KeyValueSettingEn;
import org.weaver.view.query.entity.QueryFilter;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.query.entity.TreeData;
import org.weaver.view.query.entity.ViewData;
import com.alibaba.fastjson.JSONObject;
import org.weaver.view.query.entity.RequestConfig;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

interface ViewService {
	
	<T> JSONObject readTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig);
	
	<T> Integer insertTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig);
	
	<T> Integer updateTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig);
	
	<T> Integer deleteTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig);

	String translateText(String text, RequestConfig viewReqConfig, Map<String, Object> tranParamMap);

	String translateKey(String text, RequestConfig viewReqConfig, Map<String, Object> tranParamMap);

	Object getSetting(String lang, String key);

	<T> ViewData<T> query(ViewEn viewEn, Map<String, Object> params, SortByField[] sortField, Integer pageNum,
			Integer pageSize, QueryFilter queryFilter, List<String> aggrList, RowMapper<T> rowMapper,
			RequestConfig viewReqConfig);

	<T> ViewData<T> queryViewData(ViewEn viewEn, Map<String, Object> params, SortByField[] sortField, Integer pageNum,
			Integer pageSize, QueryFilter queryFilter, RowMapper<T> rowMapper, RequestConfig viewReqConfig);

	<T> List<T> queryView(ViewEn viewEn, Map<String, Object> params, SortByField[] sortField, Integer pageNum,
			Integer pageSize, QueryFilter queryFilter, RowMapper<T> rowMapper, RequestConfig viewReqConfig);

	LinkedHashMap<String, Object> queryViewAggregate(ViewEn viewEn, Map<String, Object> params, QueryFilter queryFilter,
			List<String> aggrList, RequestConfig viewReqConfig);

	<T> void updateViewInfo(ViewEn viewEn, ViewData<T> data, RequestConfig viewReqConfig);

	<T> ViewData<TreeData<T>> queryTree(String viewId, Integer level, String parentValue, Map<String, Object> params,
			SortByField[] sortField, String search, RowMapper<T> rowMapper, RequestConfig viewReqConfig);

	<T> List<TreeData<T>> queryTree(ViewEn viewEn, String keyField, String parentField, Integer level,
			Integer currentLevel, String parentValue, Map<String, Object> params, SortByField[] sortField,
			String search, RowMapper<T> rowMapper, RequestConfig viewReqConfig);

	<T> ViewData<TreeData<T>> queryTreePath(String viewId, String keyValue, Map<String, Object> params,
			RowMapper<T> rowMapper, RequestConfig viewReqConfig);

	ViewEn getViewInfo(String viewId);

	ViewEn getViewInfo(String dataSource, String sql, Map<String, Object> critParams);
	
	String getValue(KeyValueSettingEn setting,String key);
	
	Integer setValue(KeyValueSettingEn setting,String key,String value);
	
	Integer setValue(KeyValueSettingEn setting,String key,String value,String userId);
	
	Map<String,Object> getData(KeyValueSettingEn setting,String key);
	
	Integer setData(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data,String userId);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
