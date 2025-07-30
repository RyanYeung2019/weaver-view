package org.weaver.service;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.weaver.config.entity.ViewEn;
import org.weaver.query.entity.KeyValueSettingEn;
import org.weaver.query.entity.SortByField;
import org.weaver.table.entity.FieldEn;
import org.weaver.table.entity.TableEn;

public interface ViewDao {

	String getDataType(String dataSourceName);

	ViewEn getViewInfo(String dataSource, String sql, Map<String, Object> critParams);

	LinkedHashMap<String, Object> queryViewAggregate(ViewEn viewEn, Map<String, Object> queryParams,
			FilterCriteria filter, List<String> aggrField);

	<T> List<T> queryData(ViewEn viewEn, Map<String, Object> queryParams, SortByField[] sortField, Integer pNum,
			Integer pSize, FilterCriteria filter, RowMapper<T> rowMapper);

	TableEn getTableInfo(String dataSourreBeanName,String table); 
	
	
	int executeInsert(String dataSourceName, Map<String,Object> data, String sql, FieldEn autoIncrementField);
	
	int executeUpdate(String dataSourceName, Map<String,Object> data, String sql,String checkSql,Long assertMaxRecordAffected);
	
	int[] executeSqlBatch(String dataSourceName, List<Map<String,Object>> data, String sql);
	
	List<Map<String,Object>> listData(String dataSourreBeanName,Object[] values,String sql);

	Map<String,Object> getKeyValueTable(KeyValueSettingEn setting,String key);
	
	int updateKeyValueTable(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data);
	
	int insertKeyValueTable(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data);
	
}
