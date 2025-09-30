package org.weaver.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.weaver.config.LangDefine;
import org.weaver.query.entity.RequestConfig;
import org.weaver.table.entity.FieldEn;
import org.weaver.table.entity.PrimaryKeyEn;
import org.weaver.table.entity.TableEn;
import org.weaver.table.entity.UpdateCommand;
import org.weaver.view.util.Utils;
import com.alibaba.fastjson.JSONObject;

/**
*
* @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
* 
*/

@Component("tableService")
public class TableServiceImpl implements TableService {

	private static final Logger log = LoggerFactory.getLogger(TableService.class);

	@Autowired
	TableDao tableDao;
	
	@Autowired
	LangDefine langDefine;	
	@Autowired
	private ApplicationContext applicationContext;
	
	public void setTableReqConfig(RequestConfig tableReqConfig) {
		String lang = tableReqConfig.getLanguage();
		SimpleDateFormat dateFormat = (SimpleDateFormat) langDefine.getSetting(lang, LangDefine.FORMAT_DATE);
		if (dateFormat != null) {
			tableReqConfig.setDateFormat(dateFormat);
		}
		SimpleDateFormat timeFormat = (SimpleDateFormat) langDefine.getSetting(lang, LangDefine.FORMAT_TIME);
		if (timeFormat != null) {
			tableReqConfig.setTimeFormat(timeFormat);
		}
		SimpleDateFormat datetimeFormat = (SimpleDateFormat) langDefine.getSetting(lang, LangDefine.FORMAT_DATETIME);
		if (datetimeFormat != null) {
			tableReqConfig.setDatetimeFormat(datetimeFormat);
		}
	}	
	
	
	@SuppressWarnings("unchecked")
	public <T> List<T> listTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig, String... whereFields) {
		log.info("listTable");
		TableEn tableEn = tableDao.getTableInfo(dataSourceName, tableName);
		List<PrimaryKeyEn> keys = tableEn.getPrimaryKeyEns();
		if(data instanceof LinkedHashMap) {
			LinkedHashMap<String,Object> item = (LinkedHashMap<String, Object>)data;
			List<Object> values = new ArrayList<>();
			StringBuffer whereKey = new StringBuffer();
			boolean firstKey = true;
			mergeData(tableEn.getFieldEns(),requestConfig.getParams(),item);
			for(String field:item.keySet()) {
				FieldEn fieldEn = tableEn.getFieldEnMap().get(field);
				if(fieldEn!=null && item.get(field)!=null) {
					Object value = SqlUtils.convertObjVal(fieldEn.getType(),item.get(field), requestConfig,tableEn.getSourceType());
					item.put(field, value);
					if(keys.stream().anyMatch(e->e.getFieldDb().equals(fieldEn.getFieldDb()))) {
						if(!firstKey) {
							whereKey.append(" and ");
						}
						whereKey.append(fieldEn.getFieldDb() + "=? ");
						values.add(item.get(field));
						firstKey = false;
					}
				}
			}
			if(firstKey) throw new RuntimeException("key not found for table : "+tableName);
			
			String sql = "select * from "+tableEn.getTableNameSql()+" where "+whereKey;
			DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourceName), DataSource.class);
			return(List<T>) tableDao.listData(dataSource,values.toArray(), sql);
		}else {
			Map<String,Object> item = Utils.entityToMap(data);
			List<Map<String,Object>> datas = listTable(dataSourceName,tableName,item,requestConfig,whereFields);
			List<T> result = new ArrayList<>();
			for(Map<String,Object> _data:datas) {
				try {
					T val = (T) data.getClass().getDeclaredConstructor().newInstance();
					Utils.mapToEntity(_data, val);
					result.add(val);
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			}
			return result;
		}
	}
	
	public <T> JSONObject readTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig) {
		TableEn tableEn = tableDao.getTableInfo(dataSourceName, tableName);
		List<PrimaryKeyEn> keys = tableEn.getPrimaryKeyEns();
		String[] keyFields = keys.stream()
                .map(PrimaryKeyEn::getFieldDb)
                .toArray(String[]::new);
		if(data instanceof LinkedHashMap) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<String,Object> item = (LinkedHashMap<String, Object>)data;
			List<T> listData = listTable(dataSourceName,tableName,data,requestConfig,keyFields);
			if(listData.size()>1)throw new RuntimeException("Return more than one record!");
			if(listData.size()>0) {
				T listItem = listData.get(0);
				@SuppressWarnings("unchecked")
				Map<String,Object> dataItem = (Map<String, Object>)listItem;
				for(FieldEn fieldEn:tableEn.getFieldEns()) {
					Object val = dataItem.get(fieldEn.getFieldDb());
					item.put(fieldEn.getField(), val);
				}				
			}
			JSONObject result = new JSONObject();
			result.put("name", tableEn.getTableId());
			result.put("remark", tableEn.getRemark());
			result.put("fields", tableEn.getFieldEns());
			return result;			
		}else {
			Map<String,Object> item = Utils.entityToMap(data);
			JSONObject result = readTable(dataSourceName,tableName,item,requestConfig);
			Utils.mapToEntity(item, data);
			return result;
		}
	}
	
	public <T> void modifyDataWithTrx(String dataSourceName,List<UpdateCommand<T>> updateCommands, RequestConfig requestConfig) {
		requestConfig.setDataSourceName(SqlUtils.getDataSourceName(dataSourceName));
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourceName), DataSource.class);
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
        txDef.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);
        txDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus txStatus = transactionManager.getTransaction(txDef);
        try {
        	for(UpdateCommand<T> updateCommand:updateCommands) {
        		if("persisten".equals(updateCommand.getCommand())) {
        			int[] result = this.persistenTableBatch(dataSource, updateCommand.getTableName(), updateCommand.getDataList(), requestConfig);
        			updateCommand.setResult(result);
        		}
        		if("insert".equals(updateCommand.getCommand())) {
        			int result = this.insertTable(dataSource, updateCommand.getTableName(), updateCommand.getData(), requestConfig);
        			updateCommand.setResult(new int[]{result});
        		}
        		if("update".equals(updateCommand.getCommand())) {
        			int result = this.updateTable(dataSource, updateCommand.getTableName(), updateCommand.getData(), requestConfig);
        			updateCommand.setResult(new int[]{result});
        		}
        		if("updateBatch".equals(updateCommand.getCommand())) {
        			int result = this.updateTableBatch(dataSource, updateCommand.getTableName(), updateCommand.getData(),updateCommand.getAssertMaxRecordAffected(),requestConfig,updateCommand.getWhereFields());
        			updateCommand.setResult(new int[]{result});
        		}
        		if("delete".equals(updateCommand.getCommand())) {
        			int result = this.deleteTable(dataSource, updateCommand.getTableName(), updateCommand.getData(), requestConfig);
        			updateCommand.setResult(new int[]{result});
        		}
        		if("deleteBatch".equals(updateCommand.getCommand())) {
        			int result = this.deleteTableBatch(dataSource, updateCommand.getTableName(), updateCommand.getData(),updateCommand.getAssertMaxRecordAffected(),requestConfig,updateCommand.getWhereFields());
        			updateCommand.setResult(new int[]{result});
        		}
        	}
            transactionManager.commit(txStatus);
        } catch (Exception e) {
            transactionManager.rollback(txStatus);
        	log.error("rollback for exception",e);
            throw new RuntimeException(e);
        }
	}
	
	public <T> int[] persistenTableBatch(String dataSourceName, String tableName, List<T> dataList, RequestConfig requestConfig) {
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourceName),DataSource.class);
		requestConfig.setDataSourceName(SqlUtils.getDataSourceName(dataSourceName));
		return this.persistenTableBatch(dataSource, tableName, dataList, requestConfig);
	}
	
	@SuppressWarnings("unchecked")
	public <T> int[] persistenTableBatch(DataSource dataSource, String tableName, List<T> dataList, RequestConfig requestConfig) {
		TableEn tableEn = tableDao.getTableInfo(requestConfig.getDataSourceName(), tableName);
		List<MapSqlParameterSource> dataForInsert = new ArrayList<>();
		int[] result = new int[] {}; 
		for(T data:dataList) {
			Map<String,Object> item ;
			if(data instanceof Map ) {
				item = (Map<String, Object>)data;
				for(String field:item.keySet()) {
					FieldEn fieldEn = tableEn.getFieldEnMap().get(field);
					Object value = SqlUtils.convertObjVal(fieldEn.getType(),item.get(field), requestConfig,tableEn.getSourceType());
					item.put(field, value);
				}
			}else
				item = Utils.entityToMap(data);
			mergeData(tableEn.getFieldEns(),requestConfig.getParams(),item);
			MapSqlParameterSource msps = new MapSqlParameterSource();
			for(String key:tableEn.getFieldEnMap().keySet()) {
				FieldEn fieldEn = tableEn.getFieldEnMap().get(key);
				msps.addValue(key, item.get(key),fieldEn.getSqlType());
			}
			dataForInsert.add(msps);
		}
		if(dataForInsert.size()>0) {
			List<PrimaryKeyEn> keys = tableEn.getPrimaryKeyEns();
			List<String> keyFields = keys.stream ()
					.map (PrimaryKeyEn::getFieldDb)
					.collect (Collectors.toList ());
			MapSqlParameterSource item = dataForInsert.get(0);
			StringBuffer fields = new StringBuffer();
			StringBuffer values = new StringBuffer();
			StringBuffer upValues = new StringBuffer();
			StringBuffer keysString= new StringBuffer();
			boolean first = true;
			boolean firstVal = true;
			boolean firstKey = true;
			for(String field:item.getParameterNames()) {
				FieldEn fieldEn = tableEn.getFieldEnMap().get(field);
				if(fieldEn!=null) {
					if(!first) {
						fields.append(",");
						values.append(",");
					}
					first = false;
					fields.append(fieldEn.getFieldDbSql());
					values.append(":"+fieldEn.getField());
					if(keyFields.size()>0 ) {
						if(keyFields.contains(fieldEn.getFieldDb())) {
							if(!firstKey) {
								keysString.append(",");
							}
							keysString.append(fieldEn.getFieldDb());
							firstKey = false;						
						}else {
							if(!firstVal) {
								upValues.append(",");
							}
							upValues.append(fieldEn.getFieldDb() + "=:"+fieldEn.getField() );
							firstVal = false;						
						}
					}
				}
			}
			String sql = "INSERT INTO "+tableEn.getTableNameSql()+"("+fields+")VALUES("+values+")";
			if(keyFields.size()>0) {
				if (tableEn.getSourceType().equals(SqlUtils.NAME_MYSQL)) {
					sql = "INSERT INTO "+tableEn.getTableNameSql()+"("+fields+")VALUES("+values+")ON DUPLICATE KEY UPDATE "+upValues;
				} else if (tableEn.getSourceType().equals(SqlUtils.NAME_PGSQL)) {
					sql = "INSERT INTO "+tableEn.getTableNameSql()+"("+fields+")VALUES("+values+")ON CONFLICT ("+keysString+") DO UPDATE SET "+upValues;
				} else if (tableEn.getSourceType().equals(SqlUtils.NAME_SQLITE)) {
					sql = "INSERT OR REPLACE INTO "+tableEn.getTableNameSql()+"("+fields+")VALUES("+values+")";
				}
			}
			result = tableDao.executeSqlBatch(dataSource,dataForInsert,sql);
		}
		return result;
	}
	
	public <T> int insertTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig) {
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourceName), DataSource.class);
		requestConfig.setDataSourceName(SqlUtils.getDataSourceName(dataSourceName));
		return this.insertTable(dataSource, tableName, data, requestConfig);
	}
	
	public <T> int insertTable(DataSource dataSource, String tableName, T data, RequestConfig requestConfig) {
		TableEn tableEn = tableDao.getTableInfo(requestConfig.getDataSourceName(), tableName);
		FieldEn autoIncEn = tableEn.getFieldEns().stream().filter(item->item.getAutoInc()).findFirst().orElse(null);
		Integer result = 0;
		if(data instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String,Object> item = (Map<String, Object>)data;
			mergeData(tableEn.getFieldEns(),requestConfig.getParams(),item);
			StringBuffer fields = new StringBuffer();
			StringBuffer values = new StringBuffer();
			boolean first = true;
			for(String field:item.keySet()) {
				FieldEn fieldEn = tableEn.getFieldEnMap().get(field);
				if(fieldEn!=null && item.get(field)!=null) {
					Object value = SqlUtils.convertObjVal(fieldEn.getType(),item.get(field), requestConfig,tableEn.getSourceType());
					item.put(field, value);
					if(!first) {
						fields.append(",");
						values.append(",");
					}
					first = false;
					fields.append(fieldEn.getFieldDbSql());
					values.append(":"+fieldEn.getField());
				}
			}
			String sql = "insert into "+tableEn.getTableNameSql()+"("+fields+")VALUES("+values+")";
			result = tableDao.executeInsert(dataSource, item, sql, autoIncEn); 
		}else {
			Map<String,Object> item = Utils.entityToMap(data);
			result = insertTable(dataSource,tableName,item,requestConfig);
			Utils.mapToEntity(item, data);
		}
		return result;
	}
	
	public <T> int updateTableBatch(String dataSourceName, String tableName, T data,Long assertMaxRecordAffected, RequestConfig requestConfig,String... whereFields) {
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourceName), DataSource.class);
		requestConfig.setDataSourceName(SqlUtils.getDataSourceName(dataSourceName));
		return this.updateTableBatch(dataSource, tableName, data, assertMaxRecordAffected, requestConfig, whereFields);
	}

	public <T> int updateTableBatch(DataSource dataSource, String tableName, T data,Long assertMaxRecordAffected, RequestConfig requestConfig,String... whereFields) {
		TableEn tableEn = tableDao.getTableInfo(requestConfig.getDataSourceName(), tableName);
		Integer result = 0;
		if(data instanceof Map ) {
			@SuppressWarnings("unchecked")
			Map<String,Object> item = (Map<String, Object>)data;
			StringBuffer upKeys = new StringBuffer();
			StringBuffer upValues = new StringBuffer();
			boolean firstKey = true;
			boolean firstVal = true;
			mergeData(tableEn.getFieldEns(),requestConfig.getParams(),item);
			for(String field:item.keySet()) {
				FieldEn fieldEn = tableEn.getFieldEnMap().get(field);
				if(fieldEn!=null && item.get(field)!=null) {
					Object value = SqlUtils.convertObjVal(fieldEn.getType(),item.get(field), requestConfig,tableEn.getSourceType());
					item.put(field, value);
					if(Arrays.stream(whereFields).anyMatch(fieldEn.getField()::equals)) {
						if(!firstKey) {
							upKeys.append(" and ");
						}
						upKeys.append(fieldEn.getFieldDbSql() + "=:"+fieldEn.getField() );
						firstKey = false;
					}else {
						if(!firstVal) {
							upValues.append(",");
						}
						upValues.append(fieldEn.getFieldDbSql() + "=:"+fieldEn.getField() );
						firstVal = false;
					}
				}
			}
			if(firstKey) throw new RuntimeException("key not found for table : "+tableName);
			String sql = "update "+tableEn.getTableNameSql()+" set "+upValues+" where "+upKeys;
			String checkSql = "select count(*)from "+tableEn.getTableNameSql()+" where "+upKeys;
			result = tableDao.executeUpdate(dataSource, item, sql, checkSql,assertMaxRecordAffected); 
		}else {
			Map<String,Object> item = Utils.entityToMap(data);
			result = updateTableBatch(dataSource,tableName,item,assertMaxRecordAffected,requestConfig,whereFields);
			Utils.mapToEntity(item, data);
		}
		return result;		
	}
	
	public <T> int updateTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig) {
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourceName), DataSource.class);
		requestConfig.setDataSourceName(SqlUtils.getDataSourceName(dataSourceName));
		return this.updateTable(dataSource, tableName, data, requestConfig);
	}	
	
	public <T> int updateTable(DataSource dataSource, String tableName, T data, RequestConfig requestConfig) {
		TableEn tableEn = tableDao.getTableInfo(requestConfig.getDataSourceName(), tableName);
		List<PrimaryKeyEn> keys = tableEn.getPrimaryKeyEns();
		String[] keyFields = keys.stream()
                .map(PrimaryKeyEn::getField)
                .toArray(String[]::new);
		return updateTableBatch(dataSource,tableName,data,1l,requestConfig,keyFields);
	}	
	
	public <T> int deleteTableBatch(String dataSourceName, String tableName, T data,Long assertMaxRecordAffected, RequestConfig requestConfig,String... whereFields) {
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourceName), DataSource.class);
		requestConfig.setDataSourceName(SqlUtils.getDataSourceName(dataSourceName));
		return this.deleteTableBatch(dataSource, tableName, data, assertMaxRecordAffected, requestConfig, whereFields);
	}

	public <T> int deleteTableBatch(DataSource dataSource, String tableName, T data,Long assertMaxRecordAffected, RequestConfig requestConfig,String... whereFields) {
		TableEn tableEn = tableDao.getTableInfo(requestConfig.getDataSourceName(), tableName);
		Integer result = 0;
		if(data instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String,Object> item = (Map<String, Object>)data;
			StringBuffer delKeys = new StringBuffer();
			boolean firstKey = true;
			mergeData(tableEn.getFieldEns(),requestConfig.getParams(),item);
			for(String field:item.keySet()) {
				FieldEn fieldEn = tableEn.getFieldEnMap().get(field);
				if(fieldEn!=null && item.get(field)!=null) {
					Object value = SqlUtils.convertObjVal(fieldEn.getType(),item.get(field), requestConfig,tableEn.getSourceType());
					item.put(field, value);
					if(Arrays.stream(whereFields).anyMatch(fieldEn.getField()::equals)) {
						if(!firstKey) {
							delKeys.append(" and ");
						}
						delKeys.append(fieldEn.getFieldDbSql() + "=:"+fieldEn.getField() );
						firstKey = false;
					}
				}
			}
			if(firstKey) throw new RuntimeException("key not found for table : "+tableName);
			String sql = "delete from "+tableEn.getTableNameSql()+" where "+delKeys;
			String checkSql = "select count(*) from "+tableEn.getTableNameSql()+" where "+delKeys;
			result = tableDao.executeUpdate(dataSource, item, sql, checkSql,assertMaxRecordAffected); 
		}else {
			Map<String,Object> item = Utils.entityToMap(data);
			result = deleteTableBatch(dataSource,tableName,item,assertMaxRecordAffected,requestConfig,whereFields);
			Utils.mapToEntity(item, data);
		}
		return result;
	}

	public <T> int deleteTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig) {
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourceName), DataSource.class);
		requestConfig.setDataSourceName(SqlUtils.getDataSourceName(dataSourceName));
		return this.deleteTable(dataSource, tableName, data, requestConfig);
	}

	public <T> int deleteTable(DataSource dataSource, String tableName, T data, RequestConfig requestConfig) {
		TableEn tableEn = tableDao.getTableInfo(requestConfig.getDataSourceName(), tableName);
		List<PrimaryKeyEn> keys = tableEn.getPrimaryKeyEns();
		String[] keyFields = keys.stream()
                .map(PrimaryKeyEn::getField)
                .toArray(String[]::new);
		return deleteTableBatch(dataSource,tableName,data,1l,requestConfig,keyFields);		
	}

	private void mergeData(List<FieldEn> fieldEns,Map<String,Object> param,Map<String,Object> data){
		if(param==null || param.size()==0 ) return; 
		for(FieldEn item:fieldEns) {
			String fieldId = item.getField();
			Object dataValue = data.get(fieldId);
			if(dataValue==null) {
				Object paramValue = param.get(fieldId);
				if(paramValue!=null) {
					data.put(fieldId, paramValue);
				}
			}
		}
	}	

}
