package org.weaver.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.weaver.config.LangDefine;
import org.weaver.query.entity.QueryFilter;
import org.weaver.query.entity.RequestConfig;
import org.weaver.query.entity.SortByField;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

@Component("viewQuery")
public class ViewQueryImpl implements ViewQuery {

	@Autowired
	private ViewService viewService;
	
	@Autowired
	private LangDefine langDefine;
	

	public String getLang(String lang,String key) {
		return langDefine.getLang(lang,key);
	}
	
	public String text(String lang,String text,Map<String, Object> params) {
		RequestConfig viewReqConfig = new RequestConfig();
		viewReqConfig.setLanguage(lang);
		return viewService.translateText(text, viewReqConfig, params);
	}
	
	public String text(String lang,String text) {
		RequestConfig viewReqConfig = new RequestConfig();
		viewReqConfig.setLanguage(lang);
		return viewService.translateText(text, viewReqConfig, null);
	}

	public String i18n(String lang,String key,Map<String, Object> params,Map<String, Object> commonParams) {
		RequestConfig viewReqConfig = new RequestConfig();
		viewReqConfig.setLanguage(lang);
		viewReqConfig.setParams(commonParams);
		return viewService.translateKey(key, viewReqConfig, params);
	}	
	
	public String i18n(String lang,String key,Map<String, Object> params) {
		RequestConfig viewReqConfig = new RequestConfig();
		viewReqConfig.setLanguage(lang);
		return viewService.translateKey(key, viewReqConfig, params);
	}	

	public String i18n(String lang,String key) {
		RequestConfig viewReqConfig = new RequestConfig();
		viewReqConfig.setLanguage(lang);
		return viewService.translateKey(key, viewReqConfig, null);
	}
	
	public ViewStatement prepareView(String view) {
		ViewStatement viewStatement = new ViewStatementImpl(viewService);
		viewStatement.setViewId(view);
		return viewStatement;
	}
	
	public ViewStatement prepareView(String view,
			String[] sort,
			Integer pageNum,
			Integer pageSize,
			String filter, 
			String[] aggrs
			) {
		SortByField[] sortField = ParamUtils.sortFieldConver(sort);
		QueryFilter queryFilter = ParamUtils.filterConver(filter);
		List<String> aggrList = ParamUtils.aggrConver(aggrs);
		ViewStatement statement = prepareView(view);
		statement.setSortField(sortField);
		statement.setPageNum(pageNum);
		statement.setPageSize(pageSize);
		statement.setQueryFilter(queryFilter);
		statement.setAggrList(aggrList);
		return statement;
	}
	
	public ViewStatement prepareSql(String sql) {
		ViewStatement statement = new ViewStatementImpl(viewService);
		statement.setSql(sql);
		return statement;
	}
	
	public ViewStatement prepareTable(String tableName) {
		ViewStatement statement = new ViewStatementImpl(viewService);
		statement.setTableId(tableName);
		return statement;
	} 
	
	public ViewStatement prepareTable(String tableName,
			String[] sort,
			Integer pageNum,
			Integer pageSize,
			String filter, 
			String[] aggrs			
			) {
		SortByField[] sortField = ParamUtils.sortFieldConver(sort);
		QueryFilter queryFilter = ParamUtils.filterConver(filter);
		List<String> aggrList = ParamUtils.aggrConver(aggrs);
		ViewStatement statement = prepareTable(tableName);
		statement.setSortField(sortField);
		statement.setPageNum(pageNum);
		statement.setPageSize(pageSize);
		statement.setQueryFilter(queryFilter);
		statement.setAggrList(aggrList);
		return statement;
	} 	
	
	public ViewStatement prepareTree(String view) {
		ViewStatement statement = new ViewStatementImpl(viewService);
		statement.setViewId(view);
		return statement;
	}
	
	public ViewStatement prepareTree(String view,String[] sort) {
		SortByField[] sortField = ParamUtils.sortFieldConver(sort);
		
		ViewStatement statement = prepareTree(view);
		statement.setSortField(sortField);
		return statement;
	}



}
