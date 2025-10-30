package org.weaver.service;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.weaver.config.entity.ViewField;
import org.weaver.table.entity.DatabaseType;
import org.weaver.table.entity.FieldEn;
import org.weaver.table.entity.TableEn;
/**
*
* @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
* 
*/

public interface TableDao {
	
	DatabaseType getDatabaseType(DataSource dataSource);
	
	TableEn getTableInfo(String dataSource, String table); 
	
	int executeInsert(DataSource dataSource, Map<String,Object> data, String sql, FieldEn autoIncrementField);
	
	int executeUpdate(DataSource dataSource, Map<String,Object> data, String sql,String checkSql,Long assertMaxRecordAffected);
	
	int[] executeSqlBatch(DataSource dataSource, List<MapSqlParameterSource> data, String sql);
	
	List<Map<String,Object>> listData(DataSource dataSource,Object[] values,String sql);
	
	List<ViewField> listFieldType(DataSource dataSource,DatabaseType sourceType, String queryStr,Map<String, Object> critParams);
}
