package org.weaver.view.query;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.weaver.config.LangDefine;
import org.weaver.view.query.entity.QueryFilter;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.query.entity.ViewRequestConfig;

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
		ViewRequestConfig viewReqConfig = new ViewRequestConfig();
		viewReqConfig.setLanguage(lang);
		return viewService.translateText(text, viewReqConfig, params);
	}
	
	public String text(String lang,String text) {
		ViewRequestConfig viewReqConfig = new ViewRequestConfig();
		viewReqConfig.setLanguage(lang);
		return viewService.translateText(text, viewReqConfig, null);
	}

	public String i18n(String lang,String key,Map<String, Object> params,Map<String, Object> commonParams) {
		ViewRequestConfig viewReqConfig = new ViewRequestConfig();
		viewReqConfig.setLanguage(lang);
		viewReqConfig.setQueryParams(commonParams);
		return viewService.translateKey(key, viewReqConfig, params);
	}	
	
	public String i18n(String lang,String key,Map<String, Object> params) {
		ViewRequestConfig viewReqConfig = new ViewRequestConfig();
		viewReqConfig.setLanguage(lang);
		return viewService.translateKey(key, viewReqConfig, params);
	}	

	public String i18n(String lang,String key) {
		ViewRequestConfig viewReqConfig = new ViewRequestConfig();
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
