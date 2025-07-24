package org.weaver.view.query;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.weaver.config.LangDefine;
import org.weaver.config.entity.ViewEn;
import org.weaver.view.query.entity.KeyValueSettingEn;
import org.weaver.view.query.entity.QueryFilter;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.query.entity.RequestConfig;

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
	
	public String getValue(KeyValueSettingEn setting,String key) {
		return viewService.getValue(setting, key);
	}
	
	public Integer setValue(KeyValueSettingEn setting,String key,String value) {
		return viewService.setValue(setting, key, value);
	}	
	
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
	
	public <T> Integer insertTable(String datasource, String table, T data, RequestConfig requestConfig) {
		return viewService.insertTable(datasource,table,data,requestConfig);
	}
	
	public <T> Integer updateTable(String datasource, String table, T data, RequestConfig requestConfig) {
		return viewService.updateTable(datasource,table,data,requestConfig);
	}
	
	public <T> Integer deleteTable(String datasource, String table, T data, RequestConfig requestConfig) {
		return viewService.deleteTable(datasource,table,data,requestConfig);
	}
	
	public <T> Integer insertViewTable(String view, T data,RequestConfig requestConfig) {
		ViewEn viewEn = viewService.getViewInfo(view);
		String tables = viewEn.getMeta().getString("tables");
		if (tables==null)
			throw new RuntimeException("meta.tables not defined in "+viewEn.getViewId());
		String table = tables.split(",")[0].trim();
		return viewService.insertTable(viewEn.getDataSource(), table , data,  requestConfig);
	}
	
	public <T> Integer updateViewTable(String view, T data,RequestConfig requestConfig) {
		ViewEn viewEn = viewService.getViewInfo(view);
		String tables = viewEn.getMeta().getString("tables");
		if (tables==null)
			throw new RuntimeException("meta.tables not defined in "+viewEn.getViewId());
		String table = tables.split(",")[0].trim();
		return viewService.updateTable(viewEn.getDataSource(),table,data,requestConfig);
	}	
	
	public <T> Integer deleteViewTable(String view, T data,RequestConfig requestConfig) {
		ViewEn viewEn = viewService.getViewInfo(view);
		String tables = viewEn.getMeta().getString("tables");
		if (tables==null)
			throw new RuntimeException("meta.tables not defined in "+viewEn.getViewId());
		String table = tables.split(",")[0].trim();
		return viewService.deleteTable(viewEn.getDataSource(),table,data,requestConfig);
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
		String sql = "select * from "+tableName;
		statement.setSql(sql);
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
