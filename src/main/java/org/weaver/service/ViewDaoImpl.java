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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.weaver.config.entity.ViewEn;
import org.weaver.config.entity.ViewField;
import org.weaver.query.entity.KeyValueSettingEn;
import org.weaver.query.entity.SortByField;
import org.weaver.query.entity.ViewData;
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

@Component("queryDao")
public class ViewDaoImpl implements ViewDao {

	private final static String AGGRTYPE_AVG = "avg";
	private final static String AGGRTYPE_SUM = "sum";
	private final static String AGGRTYPE_COUNT = "count";
	private final static String AGGRTYPE_MAX = "max";
	private final static String AGGRTYPE_MIN = "min";
	
	
	
	private static final Logger log = LoggerFactory.getLogger(ViewDao.class);

	@Autowired
	private ApplicationContext applicationContext;

	public String getDataType(String dataSourceName) {
		DataSource dataSource = this.applicationContext.getBean(dataSourceName==null?"dataSource":dataSourceName, DataSource.class);
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

	public int[] executeSqlBatch(String dataSourceName, List<MapSqlParameterSource> data, String sql) {
		String dataSourreBeanName = dataSourceName;
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName==null?"dataSource":dataSourreBeanName, DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return batchUpdateLargeData(namedParameterJdbcTemplate,sql,data);
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

	public Map<String,Object> getKeyValueTable(KeyValueSettingEn setting,String key){
		String dataSourreBeanName = setting.getDataSourceName();
		if(setting.getSourceType()==null)setting.setSourceType(this.getDataType(dataSourreBeanName));
		
		if(setting.getTypeData()==null)setting.setTypeData(new LinkedHashMap<>());
		setting.getTypeData().put(setting.getKey(), key);
		String selectSql = setting.getSelectSql();
		if(selectSql==null) {
			Map<String,String> tableInfo = tableNameInfo(setting.getTable(),setting.getSourceType());
			String tableNameSql = tableInfo.get("tableNameSql");
			StringBuffer sql = new StringBuffer("select ");
			sql.append("*");
			sql.append(" from ");
			sql.append(tableNameSql.replace("'","''"));
			sql.append(" where ");
			sql.append(setting.getTypeData().keySet().stream().map(s->SqlUtils.sqlDbKeyWordEscape(s,setting.getSourceType())+"=? ").collect(Collectors.joining(" and ")).replace("'","''"));
			selectSql = sql.toString();
			setting.setSelectSql(selectSql);
		}
		Object[] values = setting.getTypeData().values().stream().toArray();
		List<Map<String,Object>> datas = listData(dataSourreBeanName,values,selectSql);
		if(datas.size()>1)throw new RuntimeException("Return more than one record!");
		if(datas.size()==0) return null;
		return datas.get(0);
	}
	
	public List<Map<String,Object>> listData(String dataSourreBeanName,Object[] values,String sql){
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName==null?"dataSource":dataSourreBeanName, DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.queryForList(sql, values);
	}
	
	public int updateKeyValueTable(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data){
		String dataSourreBeanName = setting.getDataSourceName();
		if(setting.getSourceType()==null)setting.setSourceType(this.getDataType(dataSourreBeanName));
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName==null?"dataSource":dataSourreBeanName, DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		if(setting.getTypeData()==null)setting.setTypeData(new LinkedHashMap<>());
		setting.getTypeData().put(setting.getKey(), key);
		String updateSql = setting.getUpdateSql();
		if(updateSql==null) {
			Map<String,String> tableInfo = tableNameInfo(setting.getTable(),setting.getSourceType());
			String tableNameSql = tableInfo.get("tableNameSql");
			StringBuffer sql = new StringBuffer("update ");
			sql.append(tableNameSql.replace("'","''"));
			sql.append(" set ");
			sql.append(data.keySet().stream().map(s->SqlUtils.sqlDbKeyWordEscape(s,setting.getSourceType())+"=? ").collect(Collectors.joining(",")).replace("'","''"));
			sql.append(" where ");
			sql.append(setting.getTypeData().keySet().stream().map(s->SqlUtils.sqlDbKeyWordEscape(s,setting.getSourceType())+"=? ").collect(Collectors.joining(" and ")).replace("'","''"));
			updateSql = sql.toString();
			setting.setUpdateSql(updateSql);
		}
		List<Object> result = new ArrayList<>(data.values());
		result.addAll(new ArrayList<>(setting.getTypeData().values()));
		return jdbcTemplate.update(updateSql,result.stream().toArray());
	}	
	
	public int insertKeyValueTable(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data){
		String dataSourreBeanName = setting.getDataSourceName();
		if(setting.getSourceType()==null)setting.setSourceType(this.getDataType(dataSourreBeanName));
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName==null?"dataSource":dataSourreBeanName, DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		if(setting.getTypeData()==null)setting.setTypeData(new LinkedHashMap<>());
		setting.getTypeData().put(setting.getKey(), key);
		String insertSql = setting.getInsertSql();
		if(insertSql==null) {
			Map<String,String> tableInfo = tableNameInfo(setting.getTable(),setting.getSourceType());
			String tableNameSql = tableInfo.get("tableNameSql");
			StringBuffer sql = new StringBuffer("insert into ");
			sql.append(tableNameSql.replace("'","''"));
			sql.append("(");
			sql.append(setting.getTypeData().keySet().stream().map(s->SqlUtils.sqlDbKeyWordEscape(s,setting.getSourceType())).collect(Collectors.joining(",")).replace("'","''")+","); 
			sql.append(data.keySet().stream().map(s->SqlUtils.sqlDbKeyWordEscape(s,setting.getSourceType())).collect(Collectors.joining(",")).replace("'","''")); 
			sql.append(")values(");
			sql.append(setting.getTypeData().keySet().stream().map(s->"?").collect(Collectors.joining(","))+",");
			sql.append(data.keySet().stream().map(s->"?").collect(Collectors.joining(",")));
			sql.append(")");
			insertSql = sql.toString();
			setting.setInsertSql(insertSql);
		}
		List<Object> result = new ArrayList<>(setting.getTypeData().values());
		result.addAll(new ArrayList<>(data.values()));
		return jdbcTemplate.update(insertSql,result.stream().toArray());
	}
	
	public int executeInsert(String dataSourceName, Map<String,Object> data, String sql, FieldEn autoIncrementField) {
		DataSource dataSource = this.applicationContext.getBean(dataSourceName==null?"dataSource":dataSourceName, DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        SqlParameterSource parameterSource = new MapSqlParameterSource(data);
        Integer result=0;
        if(autoIncrementField!=null) {
        	String aiField = autoIncrementField.getField();
            KeyHolder keyHolder = new GeneratedKeyHolder();
            result = namedParameterJdbcTemplate.update(sql, parameterSource, keyHolder, new String[]{aiField} );
            Number keys = keyHolder.getKey();
           	data.put(autoIncrementField.getField(), keys.longValue());            	
        }else {
            result = namedParameterJdbcTemplate.update(sql, parameterSource);
        }
       	return result;
	}
	
	public int executeUpdate(String dataSourceName, Map<String,Object> data, String sql,String checkSql,Long assertMaxRecordAffected) {
		DataSource dataSource = this.applicationContext.getBean(dataSourceName==null?"dataSource":dataSourceName, DataSource.class);
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

	public LinkedHashMap<String, Object> queryViewAggregate(ViewEn viewEn, Map<String, Object> queryParams, FilterCriteria filter,
			List<String> aggrField) {
		List<String> aggParam = new ArrayList<>();
		Map<String, String> fieldToCamel = new HashMap<>();
		aggParam.add("0-count");
		if (aggrField != null) {
			Map<String, ViewField> fieldMap = viewEn.getFieldMap();
			for (String fieldAggType : aggrField) {
				String[] fieldAggTypeArr = fieldAggType.split("-");
				String field = fieldAggTypeArr[0];
				String aggType = fieldAggTypeArr[1];
				ViewField viewField = fieldMap.get(field);
				if (aggType.equalsIgnoreCase(AGGRTYPE_AVG)
						|| aggType.equalsIgnoreCase(AGGRTYPE_COUNT) || aggType.equalsIgnoreCase(AGGRTYPE_SUM)
						|| aggType.equalsIgnoreCase(AGGRTYPE_MAX) || aggType.equalsIgnoreCase(AGGRTYPE_MIN)) {
					String fieldDb = viewField.getFieldDb();
					String fieldName = fieldDb + "_" + aggType.toLowerCase();
					fieldToCamel.put(fieldName, FormatterUtils.toCamelCase(fieldName));
					aggParam.add(fieldDb+"-"+aggType.toUpperCase());
				}
			}
		}
		String vName = SqlUtils.varName();
		String queryString = "select * from (" + viewEn.getSql().trim() + ") " + vName
				+ (filter != null ? " where " + filter.getDbSql() : "");
		Map<String, Object> param = new HashMap<>();
		param.putAll(queryParams);
		if (filter != null)
			param.putAll(filter.getParamMap());
		if (log.isDebugEnabled()) {
			for (String key : param.keySet()) {
				log.debug(key + ":" + param.get(key));
			}
		}
		String dataSourceName = viewEn.getDataSource();
		String dataSourreBeanName = dataSourceName;
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName, DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		NamedParameterJdbcTemplate nameParmJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		String defaultCountFieldName = SqlUtils.varName();
		String sqlCount = getSimpleCountAggSql(defaultCountFieldName,queryString, aggParam);
		log.debug("sqlCount:" + sqlCount);

		long startTotal = (new Date()).getTime();
		long totalTime = 0l;
		Map<String, Object> data = nameParmJdbcTemplate.queryForMap(sqlCount, param);
		LinkedHashMap<String, Object> result = new LinkedHashMap<>();
		result.put(ViewData.AGGRS_SIZE, data.get(defaultCountFieldName));
		for (String key : fieldToCamel.keySet()) {
			String camelField = fieldToCamel.get(key);
			result.put(camelField, data.get(key));
		}
		totalTime = (new Date()).getTime() - startTotal;
		log.debug("totalTime:" + totalTime);
		return result;
	}
	
	private String getSimpleCountAggSql(String defaultCountFieldName, final String sql, List<String> aggregate) {
		List<String> fields = new ArrayList<>();
		for (String fieldAggr : aggregate) {
			String[] fieldAggrArr = fieldAggr.split("-");
			String field = fieldAggrArr[0];
			String aggType = fieldAggrArr[1];
			String fieldStr = String.format("%s(%s) AS %s", aggType, field,
					field.equals("0") ? defaultCountFieldName : (field+"_"+aggType.toLowerCase()));
			fields.add(fieldStr);
		}
		StringBuilder stringBuilder = new StringBuilder(sql.length() + 40);
		stringBuilder.append("select ");
		stringBuilder.append(fields.stream ().collect (Collectors.joining (",")));
		stringBuilder.append(" from(");
		stringBuilder.append(sql);
		stringBuilder.append(") " + SqlUtils.varName());
		return stringBuilder.toString();
	}	

	public <T> List<T> queryData(ViewEn viewEn, Map<String, Object> queryParams, SortByField[] sortField,
			Integer pNum, Integer pSize, FilterCriteria filter, RowMapper<T> rowMapper) {
		String vName = SqlUtils.varName();
		String sql = "select * from (" + viewEn.getSql().trim() + ") " + vName
				+ (filter != null ? " where " + filter.getDbSql() : "");
		String orderBy = makeOrderBy(viewEn, sortField);
		Map<String, Object> param = new HashMap<>();
		param.putAll(queryParams);
		if (filter != null)
			param.putAll(filter.getParamMap());
		if (log.isDebugEnabled()) {
			for (String key : param.keySet()) {
				log.debug(key + ":" + param.get(key));
			}
		}
		String dataSourceName = viewEn.getDataSource();
		String dataSourreBeanName = dataSourceName;
		String dataSourceType = viewEn.getSourceType();
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName, DataSource.class);
		int pageSize = pSize==null?50:pSize;
		int pageNum = pNum==null?0:pNum;
		int limit = pageSize;
		int offset = pageNum > 1 ? pageSize * (pageNum - 1) : 0;
		String queryString;
		if (dataSourceType.equals(SqlUtils.NAME_ORACLE)) {
			queryString = "SELECT * FROM(SELECT rownum rnum,a.* FROM(" + sql + " ORDER BY " + orderBy
					+ ")a WHERE rownum<=" + offset + "+" + limit + ")WHERE rnum>=" + (offset + 1);
		} else {
			queryString = sql + " ORDER BY " + orderBy + " LIMIT " + limit + " OFFSET " + offset;
		}
		log.debug("sql:\n" + queryString);
		long startTotal = (new Date()).getTime();
		long totalTime = 0l;
		long loopTime = 0l;
		MapSqlParameterSource msps = new MapSqlParameterSource(param);
		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(sql);
		Object[] params = NamedParameterUtils.buildValueArray(parsedSql, msps, null);
		String sqlRun = NamedParameterUtils.parseSqlStatementIntoString(queryString);
		List<T> data = new ArrayList<>();
		Connection conn = DataSourceUtils.getConnection(dataSource);
		PreparedStatement psIns = null;
		try {
			psIns = conn.prepareStatement(sqlRun);
			PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(params);
			pss.setValues(psIns);
			try (ResultSet rSet = psIns.executeQuery()) {
				long startLoop = (new Date()).getTime();
				int rowNum = 0;
				while (rSet.next()) {
					T object = rowMapper.mapRow(rSet, rowNum++);
					data.add(object);
				}
				loopTime = (new Date()).getTime() - startLoop;
			}
			totalTime = (new Date()).getTime() - startTotal;
			log.debug("loopTime:" + loopTime+" totalTime:" + totalTime);
			log.debug("data:\n"+data.toString());
			return data;		
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
	}

	private String makeOrderBy(ViewEn viewEn, SortByField[] sortField) {
		String orderBy = null;
		if (sortField == null || sortField.length == 0) {
			orderBy = " 1 ASC";
		} else {
			boolean firstLoop = true;
			StringBuilder orderBySb = new StringBuilder();
			for (SortByField sort : sortField) {
				String field = sort.getFieldName();
				List<ViewField> fieldList = viewEn.getListFields();
				if (fieldList != null) {
					int idx = fieldList.indexOf(new ViewField(field));
					if (idx < 0) {
						throw new RuntimeException("field not found:" + field);
					}
					field = fieldList.get(idx).getFieldDb();
				}
				if (!firstLoop) orderBySb.append(",");
				orderBySb.append(SqlUtils.sqlDbKeyWordEscape(field,viewEn.getSourceType()) + " " + sort.getType());
				firstLoop = false;
			}
			orderBy = orderBySb.toString();
		}
		return orderBy;
	}

	private String getDatabaseType(Connection conn) {
		String type = null;
		try {
			if (conn != null && (!conn.isClosed())) {
				DatabaseMetaData metaData = conn.getMetaData();
				String databaseProductName = metaData.getDatabaseProductName().toLowerCase();
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
		return type;
	}

	public ViewEn getViewInfo(String dataSource, String sql,Map<String, Object> critParams) {
		String dataSourceName = dataSource==null?"dataSource":dataSource;
		ViewEn viewEn = new ViewEn();
		List<ViewField> listFields = listFieldType(dataSourceName, sql, critParams);
		viewEn.setListFields(listFields);
		viewEn.setDataSource(dataSourceName);
		String sourceType = getDataType(dataSourceName);
		viewEn.setSourceType(sourceType);
		return viewEn;
	}
	
	private Map<String,String> tableNameInfo(String table,String sourceType){
		String[] tableArray = table.split("[.]");
		String tableName = null;
		String catalog = null;
		if(tableArray.length==1) {
    		tableName = tableArray[0];
		}else {
			catalog = tableArray[0];
    		tableName = tableArray[1];
		}
        String tableNameSql = (
        		catalog==null?"":(
        				SqlUtils.sqlDbKeyWordEscape(catalog,sourceType)+"." 
        				)
        		) + SqlUtils.sqlDbKeyWordEscape(tableName,sourceType);
		Map<String,String> result = new HashMap<>();
		result.put("catalog", catalog);	
		result.put("tableName", tableName);	
		result.put("tableNameSql", tableNameSql);
		return result;
	}
	
	
	public TableEn getTableInfo(String dataSourreBeanName,String table) {
		String dsName = dataSourreBeanName==null?"dataSource":dataSourreBeanName;
		String cacheKey = dsName+"|"+table;
		TableEn tableEn = CacheUtils.cacheTableMap.get(cacheKey);
		if(tableEn!=null) return tableEn;
		tableEn = new TableEn(table);
		tableEn.setDataSource(dsName);
		tableEn.setSourceType(this.getDataType(dsName));
		final Map<String,List<TableFK>> tableForeig = new HashMap<>();
		tableForeig.put(tableEn.getTableId(), new ArrayList<>());
		DataSource dataSource = this.applicationContext.getBean(dsName, DataSource.class);
		Connection conn = DataSourceUtils.getConnection(dataSource);

		Map<String,String> tableInfo = tableNameInfo(table,tableEn.getSourceType());
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
                    String field = FormatterUtils.toCamelCase(fieldDb.toLowerCase());
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
						String field = FormatterUtils.toCamelCase(fieldName.toLowerCase());
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
                        String fkcolumnNameField = FormatterUtils.toCamelCase(fkcolumnName.toLowerCase());

                        String pktableName = impkey.getString("pktable_name");
                        String pkcolumnName = impkey.getString("pkcolumn_name");
                        String pkcolumnNameField = FormatterUtils.toCamelCase(pkcolumnName.toLowerCase());
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
						String defaultValue = columns.getString("COLUMN_DEF");
                        String fieldId = FormatterUtils.toCamelCase(colName);
                        FieldEn genDataFieldEn = new FieldEn(fieldId);
                        genDataFieldEn.setRemark(StringUtils.hasText(fieldLabel)?fieldLabel:null);
                        genDataFieldEn.setFieldDb(colName);
                        genDataFieldEn.setAutoInc("YES".equals(isAutoIncrement));
						genDataFieldEn.setNullable("YES".equals(isNullable));
                        genDataFieldEn.setTypeDb(typeDb);
                        genDataFieldEn.setPreci(precision);
						genDataFieldEn.setScale(scale);
						genDataFieldEn.setDefaultValue(defaultValue);
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
		List<ViewField> viewFields = listFieldType(dataSourreBeanName, sql  + " where 1=2", critParams);
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
	
	private List<ViewField> listFieldType(String dataSourceName, String queryStr,
			Map<String, Object> critParams) {
		Object[] params = NamedParameterUtils.buildValueArray(queryStr, critParams);
		String sql = NamedParameterUtils.parseSqlStatementIntoString(queryStr);
		String dsName = dataSourceName==null?"dataSource":dataSourceName;
		List<ViewField> listFields = new ArrayList<>();
		DataSource dataSource = this.applicationContext.getBean(dsName, DataSource.class);
		Connection conn = DataSourceUtils.getConnection(dataSource);
		PreparedStatement psIns = null;
		try {
			psIns = conn.prepareStatement(sql);
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
