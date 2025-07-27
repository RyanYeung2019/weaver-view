package org.weaver.view.query;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.weaver.config.entity.ViewEn;
import org.weaver.config.entity.ViewField;
import org.weaver.view.query.entity.KeyValueSettingEn;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.query.entity.ViewData;
import org.weaver.view.table.entity.FieldEn;
import org.weaver.view.table.entity.ForeignKeyEn;
import org.weaver.view.table.entity.ForeignRefFieldEn;
import org.weaver.view.table.entity.IndexEn;
import org.weaver.view.table.entity.IndexFieldEn;
import org.weaver.view.table.entity.PrimaryKeyEn;
import org.weaver.view.table.entity.TableEn;
import org.weaver.view.table.entity.TableFK;
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
	
	private final Map<String, TableEn> cacheTableMap = new ConcurrentHashMap<>();
	
	private static final Logger log = LoggerFactory.getLogger(ViewDao.class);

	@Autowired
	private ApplicationContext applicationContext;

	public String getDataType(String dataSourceName) {
		try {
			DataSource dataSource = this.applicationContext.getBean(dataSourceName, DataSource.class);
			try (Connection conn = dataSource.getConnection();) {
				return getDatabaseType(conn);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public int[] executeSqlBatch(String dataSourceName, List<Map<String,Object>> data, String sql) {
		String dataSourreBeanName = dataSourceName;
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName==null?"dataSource":dataSourreBeanName, DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        return batchUpdateLargeData(namedParameterJdbcTemplate,sql,data);
	}
	
	private int[] batchUpdateLargeData(NamedParameterJdbcTemplate namedParameterJdbcTemplate,String sql, List<Map<String, Object>> entityList) {
		int batchSize = 500;
		int[] result = null;
	    for (int i = 0; i < entityList.size(); i += batchSize) {
	        List<Map<String, Object>> subList = entityList.subList(i, Math.min(i + batchSize, entityList.size()));
	        SqlParameterSource[] batchArgs = subList.stream()
	            .map(MapSqlParameterSource::new)
	            .toArray(SqlParameterSource[]::new);
	        int[] rows = namedParameterJdbcTemplate.batchUpdate(sql, batchArgs);
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
		if(setting.getTypeData()==null)setting.setTypeData(new LinkedHashMap<>());
		setting.getTypeData().put(setting.getKey(), key);
		StringBuffer sql = new StringBuffer("select ");
		sql.append("*");
		sql.append(" from ");
		sql.append(setting.getTable().replace("'","''"));
		sql.append(" where ");
		sql.append(setting.getTypeData().keySet().stream().map(s->s+"=? ").collect(Collectors.joining(" and ")).replace("'","''"));
		Object[] values = setting.getTypeData().values().stream().toArray(Object[]::new);
 		try {
 			return readData(dataSourreBeanName,values,sql.toString());
	    } catch (EmptyResultDataAccessException e) {
	    	return null;	
	    }
	}
	
	public Map<String,Object> readData(String dataSourreBeanName,Object[] values,String sql){
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName==null?"dataSource":dataSourreBeanName, DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
 		try {
 			return jdbcTemplate.queryForMap(sql, values);
	    } catch (EmptyResultDataAccessException e) {
	    	return null;	
	    }
	}
	
	public Integer updateKeyValueTable(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data){
		String dataSourreBeanName = setting.getDataSourceName();
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName==null?"dataSource":dataSourreBeanName, DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		if(setting.getTypeData()==null)setting.setTypeData(new LinkedHashMap<>());
		setting.getTypeData().put(setting.getKey(), key);

		StringBuffer sql = new StringBuffer("update ");
		sql.append(setting.getTable().replace("'","''"));
		sql.append(" set ");
		sql.append(data.keySet().stream().map(s->s+"=? ").collect(Collectors.joining(",")).replace("'","''"));
		sql.append(" where ");
		sql.append(setting.getTypeData().keySet().stream().map(s->s+"=? ").collect(Collectors.joining(" and ")).replace("'","''"));

		List<Object> result = new ArrayList<>(data.values());
		result.addAll(new ArrayList<>(setting.getTypeData().values()));

 		try {
 			return jdbcTemplate.update(sql.toString(),result.stream().toArray(Object[]::new));
	    } catch (EmptyResultDataAccessException e) {
	    	return 0;	
	    }
	}	
	
	public Integer insertKeyValueTable(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data){
		String dataSourreBeanName = setting.getDataSourceName();
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName==null?"dataSource":dataSourreBeanName, DataSource.class);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		if(setting.getTypeData()==null)setting.setTypeData(new LinkedHashMap<>());
		setting.getTypeData().put(setting.getKey(), key);
		
		StringBuffer sql = new StringBuffer("insert into ");
		sql.append(setting.getTable().replace("'","''"));
		sql.append("(");
		sql.append(String.join(",", setting.getTypeData().keySet()).replace("'","''")+",");
		sql.append(String.join(",", data.keySet()).replace("'","''"));
		sql.append(")values(");
		sql.append(setting.getTypeData().keySet().stream().map(s->"?").collect(Collectors.joining(","))+",");
		sql.append(data.keySet().stream().map(s->"?").collect(Collectors.joining(",")));
		sql.append(")");
		
		List<Object> result = new ArrayList<>(setting.getTypeData().values());
		result.addAll(new ArrayList<>(data.values()));
 		try {
 			return jdbcTemplate.update(sql.toString(),result.stream().toArray(Object[]::new));
	    } catch (EmptyResultDataAccessException e) {
	    	return 0;	
	    }
	}
	
	public Integer executeSql(String dataSourceName, Map<String,Object> data, String sql,FieldEn autoIncrementField) {
		String dataSourreBeanName = dataSourceName;
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName==null?"dataSource":dataSourreBeanName, DataSource.class);
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
		boolean isFirst = true;
		for (String fieldStr : fields) {
			if (!isFirst) {
				stringBuilder.append(",");
			}
			stringBuilder.append(fieldStr);
			isFirst = false;
		}
		stringBuilder.append(" from (");
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
		Object[] params = NamedParameterUtils.buildValueArray(queryString, param);
		String sqlRun = NamedParameterUtils.parseSqlStatementIntoString(queryString);
		List<T> data = new ArrayList<>();
		try (Connection conn = dataSource.getConnection(); PreparedStatement psIns = conn.prepareStatement(sqlRun)) {
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
		} catch (Exception e) {
			log.error("error:",e);
			throw new RuntimeException(e);
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
				if (!firstLoop)
					orderBySb.append(",");
				if (SqlUtils.NAME_MYSQL.equals(viewEn.getSourceType())) {
					orderBySb.append("" + field + " " + sort.getType());
				} else {
					orderBySb.append("\"" + field + "\" " + sort.getType());
				}
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
		ViewEn viewEn = new ViewEn();
		List<ViewField> listFields = listFieldType(dataSource, sql, critParams);
		viewEn.setListFields(listFields);
		viewEn.setDataSource(dataSource);
		String sourceType = getDataType(dataSource);
		viewEn.setSourceType(sourceType);
		return viewEn;
	}
	
	public TableEn getTableInfo(String dataSourreBeanName,String table) {
		String dsName = dataSourreBeanName==null?"dataSource":dataSourreBeanName;
		String cacheKey = dsName+"|"+table;
		TableEn tableEn = this.cacheTableMap.get(cacheKey);
		if(tableEn!=null) return tableEn;
		tableEn = new TableEn(table);
		final Map<String,List<TableFK>> tableForeig = new HashMap<>();
		tableForeig.put(tableEn.getTableId(), new ArrayList<>());
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName==null?"dataSource":dataSourreBeanName, DataSource.class);
    	try(Connection conn =  dataSource.getConnection();){
    		DatabaseMetaData dbMeta = conn.getMetaData();
    		String[] tableArray = table.split("[.]");
    		String catalog = conn.getCatalog();
    		String database = null;
    		String tableName = null;
    		if(tableArray.length==1) {
        		tableName = tableArray[0];
    		}else {
    			catalog = tableArray[0];
        		tableName = tableArray[1];
    		}
        	List<String> pk = new ArrayList<>();
        	try(ResultSet tableList = dbMeta.getTables(catalog, database,tableName,new String[]{"TABLE"})){
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
                    String dbField = keys.getString("COLUMN_NAME");
                    int keySeq = keys.getInt("KEY_SEQ");
                    pk.add(keys.getString("pk_name"));
                    String field = FormatterUtils.toCamelCase(dbField.toLowerCase());
                    PrimaryKeyEn genDataPkEn = new PrimaryKeyEn(field,dbField);
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
                	Map<String,FieldEn> fieldEnMap = tableEn.getFieldEnMap();
                    while(columns.next()){
                    	
                        String colName = columns.getString("COLUMN_NAME");
                        String fieldLabel = columns.getString("REMARKS");
                        String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
                        String typeDb = columns.getString("TYPE_NAME");
						Integer precision = columns.getInt("COLUMN_SIZE");
						Integer scale = columns.getInt("DECIMAL_DIGITS");
						String isNullable = columns.getString("IS_NULLABLE");
						String defaultValue = columns.getString("COLUMN_DEF");
                        int sqlType = columns.getInt("DATA_TYPE");
                        String fieldId = FormatterUtils.toCamelCase(colName);
                        
                        FieldEn genDataFieldEn = new FieldEn(fieldId);
                        genDataFieldEn.setType(FormatterUtils.convertSqlType(colName, sqlType));
                        genDataFieldEn.setComment(StringUtils.hasText(fieldLabel)?fieldLabel:null);
                        genDataFieldEn.setFieldDb(colName);
                        genDataFieldEn.setAutoInc("YES".equals(isAutoIncrement));
						genDataFieldEn.setNullable("YES".equals(isNullable));
                        genDataFieldEn.setSqlType(sqlType);
                        genDataFieldEn.setType(FormatterUtils.convertSqlType(colName, sqlType));
                        genDataFieldEn.setTypeDb(typeDb);
                        //genDataFieldEn.setTypeJava(type);
                        genDataFieldEn.setPreci(precision);
						genDataFieldEn.setScale(scale);
						genDataFieldEn.setDefaultValue(defaultValue);
                        
                        fieldEns.add(genDataFieldEn);
                        fieldEnMap.put(fieldId, genDataFieldEn);
                    }
                    tableEn.setFieldEns(fieldEns);
                    tableEn.setFieldEnMap(fieldEnMap);
                }                
            }
    	}catch(SQLException e) {
			throw new RuntimeException(e);
		}
    	this.cacheTableMap.put(cacheKey,tableEn);
    	return tableEn;
	}	
	
	private List<ViewField> listFieldType(String dataSourceName, String queryStr,
			Map<String, Object> critParams) {
		List<ViewField> listFields = new ArrayList<>();
		try {
			Object[] params = NamedParameterUtils.buildValueArray(queryStr, critParams);
			String sql = NamedParameterUtils.parseSqlStatementIntoString(queryStr);
			DataSource dataSource = this.applicationContext.getBean(dataSourceName, DataSource.class);
			try (Connection conn = dataSource.getConnection(); PreparedStatement psIns = conn.prepareStatement(sql)) {
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
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return listFields;
	}	
	
	
}
