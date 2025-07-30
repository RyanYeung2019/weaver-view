package org.weaver.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.weaver.query.entity.RequestConfig;
import org.weaver.table.entity.FieldEn;
import org.weaver.table.entity.PrimaryKeyEn;
import org.weaver.table.entity.TableEn;
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
	ViewDao queryDao;
	
	@SuppressWarnings("unchecked")
	public <T> List<T> listTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig, String... whereFields) {
		log.info("listTable");
		TableEn tableEn = queryDao.getTableInfo(dataSourceName, tableName);
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
					Object value = SqlUtils.convertObjVal(fieldEn.getType(),item.get(field), requestConfig);
					item.put(field, value);
					if(keys.stream().anyMatch(e->e.getDbField().equals(fieldEn.getFieldDb()))) {
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
			String sql = "select * from "+tableName+" where "+whereKey;
			return(List<T>) queryDao.listData(dataSourceName,values.toArray(Object[]::new), sql);
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
		TableEn tableEn = queryDao.getTableInfo(dataSourceName, tableName);
		List<PrimaryKeyEn> keys = tableEn.getPrimaryKeyEns();
		String[] keyFields = keys.stream()
                .map(PrimaryKeyEn::getDbField)
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

	@SuppressWarnings("unchecked")
	public <T> int[] insertTableBatch(String dataSourceName, String tableName, List<T> dataList, RequestConfig requestConfig) {
		TableEn tableEn = queryDao.getTableInfo(dataSourceName, tableName);
		List<Map<String,Object>> dataForInsert = new ArrayList<>();
		int[] result = new int[] {}; 
		if(requestConfig.getParams()!=null && requestConfig.getParams().size()>0) {
			for(T data:dataList) {
				Map<String,Object> item ;
				if(data instanceof Map )
					item = (Map<String, Object>)data;				
				else
					item = Utils.entityToMap(data);
				mergeData(tableEn.getFieldEns(),requestConfig.getParams(),item);
				dataForInsert.add(item);
			}
		}
		if(dataForInsert.size()>0) {
			List<PrimaryKeyEn> keys = tableEn.getPrimaryKeyEns();
			List<String> keyFields = keys.stream ()
					.map (PrimaryKeyEn::getDbField)
					.collect (Collectors.toList ());
			
			Map<String,Object> item = dataForInsert.get(0);
			StringBuffer fields = new StringBuffer();
			StringBuffer values = new StringBuffer();
			StringBuffer upValues = new StringBuffer();
			StringBuffer keysString= new StringBuffer();
			boolean first = true;
			boolean firstVal = true;
			boolean firstKey = true;
			for(String field:item.keySet()) {
				FieldEn fieldEn = tableEn.getFieldEnMap().get(field);
				if(fieldEn!=null && item.get(field)!=null) {
					Object value = SqlUtils.convertObjVal(fieldEn.getType(),item.get(field), requestConfig);
					item.put(field, value);
					if(!first) {
						fields.append(",");
						values.append(",");
					}
					first = false;
					fields.append(fieldEn.getFieldDb());
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
			String sql = "INSERT INTO "+tableName+"("+fields+")VALUES("+values+")";
			if(keyFields.size()>0) {
				if (tableEn.getSourceType().equals(SqlUtils.NAME_MYSQL)) {
					sql = "INSERT INTO "+tableName+"("+fields+")VALUES("+values+")ON DUPLICATE KEY UPDATE "+upValues;
				} else if (tableEn.getSourceType().equals(SqlUtils.NAME_PGSQL)) {
					sql = "INSERT INTO "+tableName+"("+fields+")VALUES("+values+")ON CONFLICT ("+keysString+") DO UPDATE SET"+upValues;
				}
			}
			result = queryDao.executeSqlBatch(dataSourceName,dataForInsert,sql);
		}
		return result;
	}	
	
	public <T> int insertTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig) {
		TableEn tableEn = queryDao.getTableInfo(dataSourceName, tableName);
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
				FieldEn feildEn = tableEn.getFieldEnMap().get(field);
				if(feildEn!=null && item.get(field)!=null) {
					Object value = SqlUtils.convertObjVal(feildEn.getType(),item.get(field), requestConfig);
					item.put(field, value);
					if(!first) {
						fields.append(",");
						values.append(",");
					}
					first = false;
					fields.append(feildEn.getFieldDb());
					values.append(":"+feildEn.getField());
				}
			}
			String sql = "insert into "+tableName+"("+fields+")VALUES("+values+")";
			result = queryDao.executeInsert(dataSourceName, item, sql, autoIncEn); 
		}else {
			Map<String,Object> item = Utils.entityToMap(data);
			result = insertTable(dataSourceName,tableName,item,requestConfig);
			Utils.mapToEntity(item, data);
		}
		return result;
	}

	public <T> int updateTableBatch(String dataSourceName, String tableName, T data,Long assertMaxRecordAffected, RequestConfig requestConfig,String... whereFields) {
		TableEn tableEn = queryDao.getTableInfo(dataSourceName, tableName);
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
					Object value = SqlUtils.convertObjVal(fieldEn.getType(),item.get(field), requestConfig);
					item.put(field, value);
					if(Arrays.stream(whereFields).anyMatch(fieldEn.getFieldDb()::equals)) {
						if(!firstKey) {
							upKeys.append(" and ");
						}
						upKeys.append(fieldEn.getFieldDb() + "=:"+fieldEn.getField() );
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
			if(firstKey) throw new RuntimeException("key not found for table : "+tableName);
			String sql = "update "+tableName+" set "+upValues+" where "+upKeys;
			String checkSql = "select count(*)from "+tableName+" where "+upKeys;
			result = queryDao.executeUpdate(dataSourceName, item, sql, checkSql,assertMaxRecordAffected); 
		}else {
			Map<String,Object> item = Utils.entityToMap(data);
			result = updateTableBatch(dataSourceName,tableName,item,assertMaxRecordAffected,requestConfig,whereFields);
			Utils.mapToEntity(item, data);
		}
		return result;		
	}
	
	public <T> int updateTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig) {
		TableEn tableEn = queryDao.getTableInfo(dataSourceName, tableName);
		List<PrimaryKeyEn> keys = tableEn.getPrimaryKeyEns();
		String[] keyFields = keys.stream()
                .map(PrimaryKeyEn::getDbField)
                .toArray(String[]::new);
		return updateTableBatch(dataSourceName,tableName,data,1l,requestConfig,keyFields);
	}	

	public <T> int deleteTableBatch(String dataSourceName, String tableName, T data,Long assertMaxRecordAffected, RequestConfig requestConfig,String... whereFields) {
		TableEn tableEn = queryDao.getTableInfo(dataSourceName, tableName);
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
					Object value = SqlUtils.convertObjVal(fieldEn.getType(),item.get(field), requestConfig);
					item.put(field, value);
					if(Arrays.stream(whereFields).anyMatch(fieldEn.getFieldDb()::equals)) {
						if(!firstKey) {
							delKeys.append(" and ");
						}
						delKeys.append(fieldEn.getFieldDb() + "=:"+fieldEn.getField() );
						firstKey = false;
					}
				}
			}
			if(firstKey) throw new RuntimeException("key not found for table : "+tableName);
			String sql = "delete from "+tableName+" where "+delKeys;
			String checkSql = "select count(*) from "+tableName+" where "+delKeys;
			result = queryDao.executeUpdate(dataSourceName, item, sql, checkSql,assertMaxRecordAffected); 
		}else {
			Map<String,Object> item = Utils.entityToMap(data);
			result = deleteTableBatch(dataSourceName,tableName,item,assertMaxRecordAffected,requestConfig,whereFields);
			Utils.mapToEntity(item, data);
		}
		return result;
	}
	
	public <T> int deleteTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig) {
		TableEn tableEn = queryDao.getTableInfo(dataSourceName, tableName);
		List<PrimaryKeyEn> keys = tableEn.getPrimaryKeyEns();
		String[] keyFields = keys.stream()
                .map(PrimaryKeyEn::getDbField)
                .toArray(String[]::new);
		return deleteTableBatch(dataSourceName,tableName,data,1l,requestConfig,keyFields);		
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
