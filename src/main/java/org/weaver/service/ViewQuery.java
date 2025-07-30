package org.weaver.service;

import java.util.Map;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public interface ViewQuery {
	
	public String getLang(String lang,String key) ;
	
	public String text(String lang,String text,Map<String, Object> params) ;
	
	public String text(String lang,String text);

	public String i18n(String lang,String key,Map<String, Object> params,Map<String, Object> commonParams) ;
	
	public String i18n(String lang,String key,Map<String, Object> params) ;

	public String i18n(String lang,String key) ;
	
	
	public ViewStatement prepareView(String view) ;
	
	public ViewStatement prepareView(String view, String[] sort, Integer pageNum, Integer pageSize, String filter, String[] aggrs);
	
	public ViewStatement prepareSql(String sql) ;
	
	public ViewStatement prepareTable(String tableName) ;
	
	public ViewStatement prepareTable(String tableName, String[] sort, Integer pageNum, Integer pageSize, String filter,  String[] aggrs);
	
	public ViewStatement prepareTree(String view);
	
	public ViewStatement prepareTree(String view,String[] sort);
	
	
	
	
}
