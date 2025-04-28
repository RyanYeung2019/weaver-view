package org.weaver.view.query;



import java.util.Map;


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
	
	public ViewStatement prepareTree(String view);
	
	public ViewStatement prepareTree(String view,String[] sort);
}
