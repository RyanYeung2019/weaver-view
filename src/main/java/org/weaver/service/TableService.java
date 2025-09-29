package org.weaver.service;

import java.util.List;

import org.weaver.query.entity.RequestConfig;
import org.weaver.table.entity.UpdateCommand;

import com.alibaba.fastjson.JSONObject;

/**
*
* @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
* 
*/

public interface TableService {
	
	void setTableReqConfig(RequestConfig tableReqConfig);
	
	<T> List<T> listTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig, String... whereFields);

	<T> JSONObject readTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig);
	
	<T> void modifyDataWithTrx(String dataSourceName,List<UpdateCommand<T>> updateCommands, RequestConfig requestConfig);
	
	<T> int insertTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig);
	
	<T> int updateTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig);
	
	<T> int deleteTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig);
	
	<T> int[] persistenTableBatch(String dataSourceName, String tableName, List<T> dataList, RequestConfig requestConfig);
	
	<T> int updateTableBatch(String dataSourceName, String tableName, T data,Long assertMaxRecordAffected, RequestConfig requestConfig,String... whereFields);
	
	<T> int deleteTableBatch(String dataSourceName, String tableName, T data,Long assertMaxRecordAffected, RequestConfig requestConfig,String... whereFields);	

}
