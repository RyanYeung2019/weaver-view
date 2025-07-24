package org.weaver.view.query;

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
import org.weaver.view.query.entity.KeyValueSettingEn;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.table.entity.FieldEn;
import org.weaver.view.table.entity.TableEn;

public interface ViewDao {

	String getDataType(String dataSourceName);

	ViewEn getViewInfo(String dataSource, String sql, Map<String, Object> critParams);

	LinkedHashMap<String, Object> queryViewAggregate(ViewEn viewEn, Map<String, Object> queryParams,
			FilterCriteria filter, List<String> aggrField);

	<T> List<T> queryData(ViewEn viewEn, Map<String, Object> queryParams, SortByField[] sortField, Integer pNum,
			Integer pSize, FilterCriteria filter, RowMapper<T> rowMapper);

	TableEn getTableInfo(String dataSourreBeanName,String table); 
	
	Integer executeSql(String dataSourceName, Map<String,Object> data, String sql,FieldEn autoIncrementField);
	
	int[] executeSqlBatch(String dataSourceName, List<Map<String,Object>> data, String sql);
	
	String getKeyValueTable(KeyValueSettingEn setting,String key);
	Integer updateKeyValueTable(KeyValueSettingEn setting,String key,String value);
	Integer insertKeyValueTable(KeyValueSettingEn setting,String key,String value);
}
