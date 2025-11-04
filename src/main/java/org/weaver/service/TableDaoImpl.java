package org.weaver.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.weaver.config.entity.ViewField;
import org.weaver.table.entity.DatabaseType;
import org.weaver.table.entity.FieldEn;
import org.weaver.table.entity.ForeignKeyEn;
import org.weaver.table.entity.ForeignRefFieldEn;
import org.weaver.table.entity.IndexEn;
import org.weaver.table.entity.IndexFieldEn;
import org.weaver.table.entity.PrimaryKeyEn;
import org.weaver.table.entity.TableEn;
import org.weaver.table.entity.TableFK;
import org.weaver.view.util.FormatterUtils;

/**
*
* @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
* 
*/

@Component("tableDao")
public class TableDaoImpl implements TableDao{

	
	private static final Logger log = LoggerFactory.getLogger(TableDao.class);

	@Autowired
	private ApplicationContext applicationContext;
	
	
	public DatabaseType getDatabaseType(DataSource dataSource) {
		Connection conn = DataSourceUtils.getConnection(dataSource);
		try {
			return getDatabaseType(conn);
		}catch (Exception ex) {
			DataSourceUtils.releaseConnection(conn, dataSource);
			conn = null;
			log.error("error:",ex);
			throw new RuntimeException(ex);
		}finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}			
	}

	private DatabaseType getDatabaseType(Connection conn) {
		DatabaseType result = new DatabaseType();
		String type = null;
		try {
			if (conn != null && (!conn.isClosed())) {
				DatabaseMetaData metaData = conn.getMetaData();
				String databaseProductName = metaData.getDatabaseProductName().toLowerCase();
				result.setMajorVersion(metaData.getDatabaseMajorVersion());
				result.setMinorVersion(metaData.getDatabaseMinorVersion());
				if (databaseProductName.matches("(?i).*mysql.*")) {
					type = SqlUtils.NAME_MYSQL;
				} else if (databaseProductName.matches("(?i).*oracle.*")) {
					type = SqlUtils.NAME_ORACLE;
				} else if (databaseProductName.matches("(?i).*microsoft.*")) {
					type = SqlUtils.NAME_MSSQL;
				} else if (databaseProductName.matches("(?i).*postgres.*")) {
					type = SqlUtils.NAME_PGSQL;
				} else if (databaseProductName.matches("(?i).*sqlite*")) {
					type = SqlUtils.NAME_SQLITE;
					
				} else {
					String msg = "Unsupported database: " + databaseProductName;
					throw new RuntimeException(msg, null);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Failed to create tables for dashboards database.", e);
		}
		result.setType(type);
		return result;
	}
	
	public int[] executeSqlBatch(DataSource dataSource, List<MapSqlParameterSource> data, String sql) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return batchUpdateLargeData(namedParameterJdbcTemplate,sql,data);
	}
	
	public int[] executeSqlBatch(String dataSourreBeanName, List<MapSqlParameterSource> data, String sql) {
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourreBeanName), DataSource.class);
		return this.executeSqlBatch(dataSource, data, sql);
	}
	
	private int[] batchUpdateLargeData(NamedParameterJdbcTemplate namedParameterJdbcTemplate,String sql, List<MapSqlParameterSource> entityList) {
		int batchSize = 500;
		int[] result = null;
	    for (int i = 0; i < entityList.size(); i += batchSize) {
	    	MapSqlParameterSource[] subList = entityList.subList(i, Math.min(i + batchSize, entityList.size())).toArray(new MapSqlParameterSource[0]);
	        int[] rows = namedParameterJdbcTemplate.batchUpdate(sql,subList);
	        result=result==null?rows:mergeArrays(result,rows);
	    }
	    return result;
	}
	
	private int[] mergeArrays(int[] arr1, int[] arr2) {
	    int[] result = new int[arr1.length + arr2.length];
	    System.arraycopy(arr1, 0, result, 0, arr1.length);
	    System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
	    return result;
	}	
	
	public List<Map<String,Object>> listData(DataSource dataSource,Object[] values,String sql){
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.queryForList(sql, values);
	}
	
	public List<Map<String,Object>> listData(String dataSourreBeanName,Object[] values,String sql){
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourreBeanName), DataSource.class);
		return this.listData(dataSource, values, sql);
	}
	
	public int executeInsert(DataSource dataSource, Map<String,Object> data, String sql, FieldEn autoIncrementField) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        SqlParameterSource parameterSource = new MapSqlParameterSource(data);
        Integer result=0;
        if(autoIncrementField!=null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            result = namedParameterJdbcTemplate.update(sql, parameterSource, keyHolder, new String[]{autoIncrementField.getFieldDb()});
            Number keys = keyHolder.getKey();
           	data.put(autoIncrementField.getField(), keys.longValue());            	
        }else {
            result = namedParameterJdbcTemplate.update(sql, parameterSource);
        }
       	return result;
	}	
	
	public int executeInsert(String dataSourreBeanName, Map<String,Object> data, String sql, FieldEn autoIncrementField) {
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourreBeanName), DataSource.class);
		return this.executeInsert(dataSource, data, sql, autoIncrementField);
	}
	
	public int executeUpdate(DataSource dataSource, Map<String,Object> data, String sql,String checkSql,Long assertMaxRecordAffected) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        if(assertMaxRecordAffected==null) {
        	assertMaxRecordAffected = 1l;
        }
    	Long count = namedParameterJdbcTemplate.queryForObject(checkSql, data, Long.class);
    	if(Long.compare(count,assertMaxRecordAffected)>0) {
    		throw new RuntimeException(String.format("Affected records exceed the asserted number. assert max %s actual %s",assertMaxRecordAffected,count));
    	}
    	return namedParameterJdbcTemplate.update(sql, data);
	}	
	
	public int executeUpdate(String dataSourreBeanName, Map<String,Object> data, String sql,String checkSql,Long assertMaxRecordAffected) {
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourreBeanName), DataSource.class);
		return this.executeUpdate(dataSource, data, sql, checkSql, assertMaxRecordAffected);
	}

	public TableEn getTableInfo(String dataSourreBeanName,String table) {
		String dsName = SqlUtils.getDataSourceName(dataSourreBeanName);
		String cacheKey = dsName+"|"+table;
		TableEn tableEn = CacheUtils.cacheTableMap.get(cacheKey);
		if(tableEn!=null) return tableEn;
		tableEn = new TableEn(table);
		tableEn.setDataSource(dsName);
		final Map<String,List<TableFK>> tableForeig = new HashMap<>();
		tableForeig.put(tableEn.getTableId(), new ArrayList<>());
		DataSource dataSource = this.applicationContext.getBean(dsName, DataSource.class);
		DatabaseType sourceType = this.getDatabaseType(dataSource);
		tableEn.setSourceType(sourceType);
		Connection conn = DataSourceUtils.getConnection(dataSource);

		Map<String,String> tableInfo = SqlUtils.tableNameInfo(table,tableEn.getSourceType());
		String tableName = tableInfo.get("tableName");
		String catalog = tableInfo.get("catalog");
		
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
    		DatabaseMetaData dbMeta = conn.getMetaData();
    		String database = null;
        	List<String> pk = new ArrayList<>();
        	try(ResultSet tableList = dbMeta.getTables(catalog, database,tableName,null)){
                while(tableList.next()){
                	String tableDisp =(String) tableList.getObject("REMARKS");
                    if(tableDisp!=null && tableDisp.trim().equals("")){
                        tableDisp = null;
                    }
                    tableEn.setRemark(tableDisp);
                }
        	}
            try(ResultSet keys = dbMeta.getPrimaryKeys(catalog, database,tableName)){
            	List<PrimaryKeyEn> primaryKeyEns = new ArrayList<>();
                while(keys.next()){
                    String fieldDb = keys.getString("COLUMN_NAME");
                    int keySeq = keys.getInt("KEY_SEQ");
                    pk.add(keys.getString("pk_name"));
                    String field = FormatterUtils.toCamelCase(fieldDb);
                    PrimaryKeyEn genDataPkEn = new PrimaryKeyEn(field,fieldDb);
                    genDataPkEn.setSortField(Integer.valueOf(keySeq));
                    primaryKeyEns.add(genDataPkEn);
                }
                Collections.sort(primaryKeyEns,new Comparator<PrimaryKeyEn>(){
                    @Override
					public int compare(PrimaryKeyEn arg0, PrimaryKeyEn arg1) {
                        return arg0.getSortField().compareTo(arg1.getSortField());
                    }
                });
                tableEn.setPrimaryKeyEns(primaryKeyEns);
            }
            try(ResultSet idxs = dbMeta.getIndexInfo(catalog, database, tableName, false, true)){
            	List<IndexEn> indexEns = new ArrayList<>();
                while(idxs.next()){
                	String index_name = idxs.getString("index_name");
                	if(!pk.contains(index_name) && index_name!=null){
                		String fieldName = idxs.getString("column_name");
                		Integer sortField = idxs.getInt("ordinal_position");
                    	String nonUnique = idxs.getString("non_unique");
                    	String ascOrDesc = idxs.getString("asc_or_desc");
                    	
                		IndexEn indexEn = new IndexEn(index_name);
                        if(indexEns.contains(indexEn)){
                        	indexEn = indexEns.get(indexEns.indexOf(indexEn));
                        }
						String field = FormatterUtils.toCamelCase(fieldName);
                		IndexFieldEn indexFieldEn = new IndexFieldEn(field,fieldName);
						indexFieldEn.setSortField(sortField);
						indexFieldEn.setField(field);
						indexEn.getFieldIds().add(indexFieldEn);
						indexEn.setNonUnique(nonUnique);
						indexEn.setAscOrDesc(ascOrDesc);
                        if(!indexEns.contains(indexEn)){
                        	indexEns.add(indexEn);
                        }
                	}
                }
                for(IndexEn indexEn:indexEns){
                	List<IndexFieldEn> indexFieldEns = indexEn.getFieldIds();
                    Collections.sort(indexFieldEns,new Comparator<IndexFieldEn>(){
                        @Override
						public int compare(IndexFieldEn arg0, IndexFieldEn arg1) {
                            return arg0.getSortField().compareTo(arg1.getSortField());
                        }
                    });
                }
                tableEn.setIndexEns(indexEns);
                //Import foreign key
                try(ResultSet impkey = dbMeta.getImportedKeys(catalog, database, tableName)){
                	List<ForeignKeyEn> foreignKeyEns = new ArrayList<>();
                    while(impkey.next()){
                        String fkName = impkey.getString("fk_name").toLowerCase();
                        String fkcolumnName = impkey.getString("fkcolumn_name");
                        String fkcolumnNameField = FormatterUtils.toCamelCase(fkcolumnName);

                        String pktableName = impkey.getString("pktable_name");
                        String pkcolumnName = impkey.getString("pkcolumn_name");
                        String pkcolumnNameField = FormatterUtils.toCamelCase(pkcolumnName);
                        int keySeq = impkey.getInt("key_seq");
                        ForeignKeyEn foreignKeyEn = new ForeignKeyEn(fkName);
                        if(foreignKeyEns.contains(foreignKeyEn)){
                        	foreignKeyEn = foreignKeyEns.get(foreignKeyEns.indexOf(foreignKeyEn));
                        }
                        ForeignRefFieldEn foreignRefFieldEn = new ForeignRefFieldEn(pkcolumnNameField,fkcolumnName);
                        foreignRefFieldEn.setRefTable(pktableName);
                        foreignRefFieldEn.setRefField(pkcolumnNameField,pkcolumnName);
                        foreignRefFieldEn.setField(fkcolumnNameField);
                        foreignRefFieldEn.setSortField(Integer.valueOf(keySeq));
						foreignKeyEn.getForeignRefFieldEns().add(foreignRefFieldEn);
                        if(!foreignKeyEns.contains(foreignKeyEn)){
                        	foreignKeyEns.add(foreignKeyEn);
                        }
                    }
                    tableEn.setForeignKeyEns(foreignKeyEns);
                }
                try(ResultSet columns = dbMeta.getColumns(catalog, database, tableName, null)){
                	List<FieldEn> fieldEns = new ArrayList<>();
                    while(columns.next()){
                        String colName = columns.getString("COLUMN_NAME");
                        String fieldLabel = columns.getString("REMARKS");
                        String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
                        String typeDb = columns.getString("TYPE_NAME");
						Integer precision = columns.getInt("COLUMN_SIZE");
						Integer scale = columns.getInt("DECIMAL_DIGITS");
						String isNullable = columns.getString("IS_NULLABLE");
                        String fieldId = FormatterUtils.toCamelCase(colName);
                        FieldEn genDataFieldEn = new FieldEn(fieldId);
                        genDataFieldEn.setRemark(StringUtils.hasText(fieldLabel)?fieldLabel:null);
                        genDataFieldEn.setFieldDb(colName);
                        genDataFieldEn.setAutoInc("YES".equals(isAutoIncrement));
						genDataFieldEn.setNullable("YES".equals(isNullable));
                        genDataFieldEn.setTypeDb(typeDb);
                        genDataFieldEn.setPreci(precision);
						genDataFieldEn.setScale(scale);
                        fieldEns.add(genDataFieldEn);
                    }
                    tableEn.setFieldEns(fieldEns);
                }                
            }	
		}
		catch (SQLException ex) {
			JdbcUtils.closeStatement(stmt);
			stmt = null;			
			DataSourceUtils.releaseConnection(conn, dataSource);
			conn = null;
			throw new RuntimeException(ex);
		}
		finally {
			JdbcUtils.closeStatement(stmt);
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
        String tableNameSql = tableInfo.get("tableNameSql");
		String sql = "select * from " + tableNameSql;
		tableEn.setSql(sql);
		//这种方式获取类型更加准确，发现sqlite用这种方式才能获取日期型
		Map<String, Object> critParams = new HashMap<String, Object>();
		List<ViewField> viewFields = this.listFieldType(dataSource,sourceType, sql  + " where 1=2", critParams);
		List<FieldEn> tableFields = tableEn.getFieldEns();
    	Map<String,FieldEn> fieldEnMap = tableEn.getFieldEnMap();
		for(FieldEn tableField:tableFields) {
			for(ViewField viewField:viewFields) {
				if(tableField.getFieldDb().equals(viewField.getFieldDb())) {
					tableField.setSqlType(viewField.getSqlType());
					tableField.setType(viewField.getType());
					tableField.setFieldDbSql(SqlUtils.sqlDbKeyWordEscape(tableField.getFieldDb(),tableEn.getSourceType()).replace("'", "''"));
				}
			}
			fieldEnMap.put(tableField.getField(), tableField);
        }
        tableEn.setFieldEns(tableFields);
        tableEn.setFieldEnMap(fieldEnMap);
        tableEn.setTableNameSql(tableNameSql);
    	CacheUtils.cacheTableMap.put(cacheKey,tableEn);
    	return tableEn;
	}	


	public List<ViewField> listFieldType(DataSource dataSource,DatabaseType sourceType, String queryStr,
			Map<String, Object> critParams) {
		Object[] params = NamedParameterUtils.buildValueArray(queryStr, critParams);
		String sql = NamedParameterUtils.parseSqlStatementIntoString(queryStr);
		List<ViewField> listFields = new ArrayList<>();
		Connection conn = DataSourceUtils.getConnection(dataSource);
		PreparedStatement psIns = null;
		try {
			if (sourceType.getType().equals(SqlUtils.NAME_ORACLE)) {
				psIns = conn.prepareStatement("SELECT * FROM("+sql+")WHERE rownum=0");
			} else {
				psIns = conn.prepareStatement("SELECT * FROM("+sql+")"+SqlUtils.varName()+" LIMIT 0");
			}
			PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(params);
			pss.setValues(psIns);
			List<String> checkExistsField = new ArrayList<>();
			try (ResultSet rSet = psIns.executeQuery()) {
				ResultSetMetaData rem = rSet.getMetaData();
				for (int i = 1; i <= rem.getColumnCount(); i++) {
					String name = rem.getColumnName(i);
					if (checkExistsField.contains(name)) {
						throw new RuntimeException(queryStr + " has duplicate fields!");
					}
					checkExistsField.add(name);
					int sqlType = rem.getColumnType(i);
					String type = rem.getColumnClassName(i);
					Integer precision = rem.getPrecision(i);
					Integer scale = rem.getScale(i);
					String typeDb = rem.getColumnTypeName(i);
					int nullable = rem.isNullable(i);
					String fieldName = FormatterUtils.toCamelCase(name);
					ViewField viewField = new ViewField(fieldName);
					viewField.setFieldDb(name);
					viewField.setSqlType(sqlType);
					viewField.setType(FormatterUtils.convertSqlType(name, sqlType));
					viewField.setTypeDb(typeDb);
					viewField.setTypeJava(type);
					
					viewField.setPreci(precision);
					viewField.setScale(scale);
					if (nullable == ResultSetMetaData.columnNoNulls) {
						viewField.setNullable(false);
					}
					if (nullable == ResultSetMetaData.columnNullable) {
						viewField.setNullable(true);
					}
					listFields.add(viewField);
				}
			}			
		}catch (SQLException ex) {
			JdbcUtils.closeStatement(psIns);
			psIns = null;			
			DataSourceUtils.releaseConnection(conn, dataSource);
			conn = null;
			log.error("error:",ex);
			throw new RuntimeException(ex);
		}finally {
			JdbcUtils.closeStatement(psIns);
			DataSourceUtils.releaseConnection(conn, dataSource);
		}			
		return listFields;
	}	
	
}
