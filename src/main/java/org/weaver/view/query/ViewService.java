package org.weaver.view.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.weaver.config.entity.ViewEn;
import org.weaver.view.query.entity.QueryFilter;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.query.entity.TreeData;
import org.weaver.view.query.entity.ViewData;
import org.weaver.view.query.entity.RequestConfig;

interface ViewService {
	<T> Integer insertViewTable(String view, T data);
	
	<T> Integer updateViewTable(String view, T data);
	
	<T> Integer deleteViewTable(String view, T data);

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

}
