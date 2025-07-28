package org.weaver.view.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.weaver.view.query.entity.KeyValueSettingEn;
import org.weaver.view.query.entity.RequestConfig;
import com.alibaba.fastjson.JSONObject;

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
	
	public <T> JSONObject readViewTable(String view, T data,RequestConfig requestConfig); 

	public <T> int insertViewTable(String view, T data,RequestConfig requestConfig); 
	
	public <T> int updateViewTable(String view, T data,RequestConfig requestConfig);
	
	public <T> int deleteViewTable(String view, T data,RequestConfig requestConfig);
	
	public <T> JSONObject readTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig);
	
	public <T> int insertTable(String datasource, String table, T data, RequestConfig requestConfig);
	
	public <T> int updateTable(String datasource, String table, T data, RequestConfig requestConfig);
	
	public <T> int deleteTable(String datasource, String table, T data, RequestConfig requestConfig);
	
	public <T> List<T> listTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig, String... whereFields);
	
	public <T> int[] insertTableBatch(String dataSourceName, String tableName, List<T> dataList, RequestConfig requestConfig);
	
	public <T> int updateTableBatch(String dataSourceName, String tableName, T data,Long assertMaxRecordAffected, RequestConfig requestConfig,String... whereFields);
	
	public <T> int deleteTableBatch(String dataSourceName, String tableName, T data,Long assertMaxRecordAffected, RequestConfig requestConfig,String... whereFields);	
	
	public String getValue(KeyValueSettingEn setting,String key);
	
	public int setValue(KeyValueSettingEn setting,String key,String value);	
	
	public int setValue(KeyValueSettingEn setting,String key,String value,String userId);
	
	public Map<String,Object> getData(KeyValueSettingEn setting,String key);
	
	public int setData(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data,String userId);
	
	
	
	
	
	
}
