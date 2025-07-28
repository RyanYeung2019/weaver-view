package org.weaver.view.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.weaver.config.LangDefine;
import org.weaver.config.ViewDefine;
import org.weaver.config.entity.EnumApiEn;
import org.weaver.config.entity.EnumDataEn;
import org.weaver.config.entity.ViewEn;
import org.weaver.config.entity.ViewField;
import org.weaver.view.query.entity.EnumItemEn;
import org.weaver.view.query.entity.FieldInfo;
import org.weaver.view.query.entity.KeyValueSettingEn;
import org.weaver.view.query.entity.QueryCriteria;
import org.weaver.view.query.entity.QueryFilter;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.query.entity.TreeData;
import org.weaver.view.query.entity.ViewData;
import org.weaver.view.query.entity.RequestConfig;
import org.weaver.view.query.mapper.BeanPropRowMapper;
import org.weaver.view.query.mapper.CamelFieldMapper;
import org.weaver.view.table.entity.FieldEn;
import org.weaver.view.table.entity.PrimaryKeyEn;
import org.weaver.view.table.entity.TableEn;
import org.weaver.view.util.FormatterUtils;
import org.weaver.view.util.Utils;

import com.alibaba.fastjson.JSONObject;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

@Component("viewService")
public class ViewServiceImpl implements ViewService {

	private static final Logger log = LoggerFactory.getLogger(ViewService.class);

	@Autowired
	ViewDao queryDao;

	@Autowired
	ViewDefine viewDefine;

	@Autowired
	LangDefine langDefine;
	

	public String getValue(KeyValueSettingEn setting,String key) {
		Map<String,Object> result = queryDao.getKeyValueTable(setting,key);
		if(result==null) return null;
		Object value = result.get(setting.getValue());
		if(value==null) return null;
		return value.toString();
	}
	
	public int setValue(KeyValueSettingEn setting,String key,String value) {
		LinkedHashMap<String,Object> data =  new LinkedHashMap<>();
		data.put(setting.getValue(), value);
		return setData(setting,key,data,null);
	}
	
	public int setValue(KeyValueSettingEn setting,String key,String value,String userId) {
		LinkedHashMap<String,Object> data =  new LinkedHashMap<>();
		data.put(setting.getValue(), value);
		return setData(setting,key,data,userId);
	}	
	
	public Map<String,Object> getData(KeyValueSettingEn setting,String key) {
		return queryDao.getKeyValueTable(setting,key);
	}	
	
	public int setData(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data,String userId) {
		if(getData(setting,key)==null) {
			if(setting.getCreateUser()!=null && userId != null)data.put(setting.getCreateUser(), userId);
			if(setting.getCreateDate()!=null)data.put(setting.getCreateDate(), new Date());
			return queryDao.insertKeyValueTable(setting, key, data);
		}else {
			if(setting.getUpdateUser()!=null && userId != null)data.put(setting.getUpdateUser(), userId);
			if(setting.getUpdateDate()!=null)data.put(setting.getUpdateDate(), new Date());
			return queryDao.updateKeyValueTable(setting, key, data);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> listTable(String dataSourceName, String tableName, T data, RequestConfig requestConfig, String... whereFields) {
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
			Map<String,Object> item = dataForInsert.get(0);
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
			String sql = "INSERT INTO "+tableName+"("+fields+")VALUES("+values+")";		
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
			String sql = "INSERT INTO "+tableName+"("+fields+")VALUES("+values+")";
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
						upKeys.append(fieldEn.getFieldDb() + "= :"+fieldEn.getField() );
						firstKey = false;
					}else {
						if(!firstVal) {
							upValues.append(" , ");
						}
						upValues.append(fieldEn.getFieldDb() + "= :"+fieldEn.getField() );
						firstVal = false;
					}
				}
			}
			if(firstKey) throw new RuntimeException("key not found for table : "+tableName);
			String sql = "update "+tableName+" set "+upValues+" where "+upKeys;
			String checkSql = "select count(*) from "+tableName+" where "+upKeys;
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
			boolean fstKey = true;
			mergeData(tableEn.getFieldEns(),requestConfig.getParams(),item);
			for(String field:item.keySet()) {
				FieldEn fieldEn = tableEn.getFieldEnMap().get(field);
				if(fieldEn!=null && item.get(field)!=null) {
					Object value = SqlUtils.convertObjVal(fieldEn.getType(),item.get(field), requestConfig);
					item.put(field, value);
					if(Arrays.stream(whereFields).anyMatch(fieldEn.getFieldDb()::equals)) {
						if(!fstKey) {
							delKeys.append(" and ");
						}
						delKeys.append(fieldEn.getFieldDb() + "= :"+fieldEn.getField() );
						fstKey = false;
					}
				}
			}
			if(fstKey) throw new RuntimeException("key not found for table : "+tableName);
			String sql = "delete from  "+tableName+" where "+delKeys;
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
	
	public String translateText(String text, RequestConfig viewReqConfig, Map<String, Object> tranParamMap) {
		for(String key:viewReqConfig.getParams().keySet()) {
			if(tranParamMap==null)tranParamMap = new HashMap<>();
			tranParamMap.put(key, viewReqConfig.getParams().get(key));
		}
		Translator translator = new Translator(queryDao, langDefine, viewDefine, viewReqConfig, tranParamMap);
		return translator.tranText(text);
	}
	
	public String translateKey(String text, RequestConfig viewReqConfig, Map<String, Object> tranParamMap) {
		Translator translator = new Translator(queryDao, langDefine, viewDefine, viewReqConfig, viewReqConfig.getParams());
		return translator.tranKey(text,tranParamMap);
	}

	public Object getSetting(String lang,String key) {
		return langDefine.getSetting(lang,key);
	}
	
	public <T> ViewData<T> query(ViewEn viewEn, Map<String, Object> params, SortByField[] sortField,
			Integer pageNum, Integer pageSize, QueryFilter queryFilter,List<String> aggrList, RowMapper<T> rowMapper,
			RequestConfig viewReqConfig) {
		Date startTime = new Date();
		ViewData<T> data = new ViewData<>();
		if(pageNum!=null && pageSize!=null) {
			data = queryViewData(viewEn,params,sortField,pageNum,pageSize,queryFilter,rowMapper,viewReqConfig);
			if (pageNum.equals(0) || pageNum.equals(1)) {
				updateViewInfo(viewEn, data, viewReqConfig);
			}
		}
		if(aggrList!=null) {
			LinkedHashMap<String, Object> aggregate = queryViewAggregate(viewEn, params, queryFilter,
					aggrList, viewReqConfig);
			data.setAggrs(aggregate);
		}
		if (sortField == null && pageNum==null && pageSize==null && queryFilter==null && aggrList == null) {
			updateViewInfo(viewEn, data, viewReqConfig);
		}
		data.setStartTime(startTime);
		data.setEndTime(new Date());
		return data;
	}
	
	
	public <T> ViewData<T> queryViewData(ViewEn viewEn, Map<String, Object> params, SortByField[] sortField,
			Integer pageNum, Integer pageSize, QueryFilter queryFilter, RowMapper<T> rowMapper,
			RequestConfig viewReqConfig) {
		ViewData<T> viewData = new ViewData<>();
		List<T> data = queryView(viewEn, params, sortField, pageNum, pageSize, queryFilter, rowMapper, viewReqConfig);
		if(queryFilter!=null)viewData.setPrimarySearchValue(queryFilter.getPrimarySearchValue());
		Map<String, Map<String, String>> valueMapping = getValueMapping(rowMapper,params,viewReqConfig);
		viewData.setValueMapping(valueMapping);
		viewData.setData(data);
		return viewData;
	}

	public <T> List<T> queryView(ViewEn viewEn, Map<String, Object> params, SortByField[] sortField,
			Integer pageNum, Integer pageSize, QueryFilter queryFilter, RowMapper<T> rowMapper,
			RequestConfig viewReqConfig) {
		log.debug("queryView:"+viewEn.getViewId());
		FilterCriteria filter = SqlUtils.paramFilter(queryFilter, viewEn.getListFields(), viewEn.getSourceType(),viewReqConfig);
		if(filter!=null) queryFilter.setPrimarySearchValue(filter.getPrimarySearchValue());
		Map<String, Object> queryParams = combineParam(viewEn, params, viewReqConfig);
		if (rowMapper instanceof CamelFieldMapper) {
			CamelFieldMapper camelFieldMapper = (CamelFieldMapper) rowMapper;
			camelFieldMapper.setFieldList(viewEn.getListFields());
			Translator translator = new Translator(queryDao, langDefine, viewDefine, viewReqConfig, queryParams);
			camelFieldMapper.setTranslator(translator);
			camelFieldMapper.setFieldMap(viewEn.getFieldMap());
		}
		if (rowMapper instanceof BeanPropRowMapper) {
			BeanPropRowMapper<T> beanPropRowMapper = (BeanPropRowMapper<T>) rowMapper;
			beanPropRowMapper.setFieldList(viewEn.getListFields());
			Translator translator = new Translator(queryDao, langDefine, viewDefine, viewReqConfig, queryParams);
			beanPropRowMapper.setTranslator(translator);
			beanPropRowMapper.setFieldMap(viewEn.getFieldMap());
		}
		return queryDao.queryData(viewEn, queryParams, sortField, pageNum, pageSize, filter, rowMapper);
	}

	public LinkedHashMap<String, Object> queryViewAggregate(ViewEn viewEn, Map<String, Object> params, QueryFilter queryFilter,
			List<String> aggrList, RequestConfig viewReqConfig) {
		log.debug("queryViewAggregate:"+viewEn.getViewId());
		FilterCriteria filter = SqlUtils.paramFilter(queryFilter, viewEn.getListFields(), viewEn.getSourceType(),viewReqConfig);
		Map<String, Object> queryParams = combineParam(viewEn, params, viewReqConfig);
		return queryDao.queryViewAggregate(viewEn, queryParams, filter, aggrList);
	}

	public <T> void updateViewInfo(ViewEn viewEn, ViewData<T> data, RequestConfig viewReqConfig) {
		String viewId = viewEn.getViewId();
		List<ViewField> viewFields = viewEn.getListFields();
		List<FieldInfo> fieldInfos = new ArrayList<>();
		for (ViewField viewField : viewFields) {
			FieldInfo fieldInfo = new FieldInfo();
			String field = viewField.getField();
			fieldInfo.setField(field);
			String langKeyName = String.format("%s.field.%s", viewId, field);
			String fieldName = langDefine.getLangDef(viewReqConfig.getLanguage(), langKeyName, field);
			fieldInfo.setName(fieldName);
			String langKeyDesc = String.format("%s.field.%s.desc", viewId, field);
			String fieldDesc = langDefine.getLangDef(viewReqConfig.getLanguage(), langKeyDesc, field);
			fieldInfo.setDesc(fieldDesc);
			fieldInfo.setType(viewField.getType());
			fieldInfo.setPreci(viewField.getPreci());
			fieldInfo.setScale(viewField.getScale());
			fieldInfo.setNullable(viewField.getNullable());
			fieldInfo.setEnumApi(viewField.getEnumApi());
			List<EnumItemEn> enumItemEns = viewField.getEnumDataList();
			if ((enumItemEns == null || enumItemEns.size() == 0) && viewField.getEnumDataString() != null) {
				Object val = langDefine.getSetting(viewReqConfig.getLanguage(), viewField.getEnumDataString());
				if (val != null && val instanceof EnumDataEn) {
					EnumDataEn valEn = (EnumDataEn) val;
					enumItemEns = new ArrayList<>();
					for (String value : valEn.getData().keySet()) {
						String text = valEn.getData().get(value);
						EnumItemEn enumItemEn = new EnumItemEn(value, text);
						enumItemEns.add(enumItemEn);
					}
				}
			}
			fieldInfo.setEnumDataList(enumItemEns);
			JSONObject props = null;
			for (String key : viewField.getProps().keySet()) {
				if(props==null)props = new JSONObject();
				props.put(key, viewField.getProps().get(key));
			}
			fieldInfo.setProps(props);
			fieldInfos.add(fieldInfo);
		}
		data.setFields(fieldInfos);
		String viewTitleKey = String.format("%s.name", viewId);
		String viewName = langDefine.getLangDef(viewReqConfig.getLanguage(), viewTitleKey, viewId);
		if (viewId.equals(viewName)) {
			String viewTitle = viewEn.getName();
			if(viewTitle!=null) viewName = viewTitle;
		}
		data.setName(viewName);
		String viewDescKey = String.format("%s.desc", viewId);
		String viewDesc = langDefine.getLangDef(viewReqConfig.getLanguage(), viewDescKey, viewId);
		data.setDesc(viewDesc);
		if (viewEn.getProps().keySet().size() > 0) {
			data.setProps(viewEn.getProps());
		}
	}
	
	public <T> ViewData<TreeData<T>> queryTree(String viewId, Integer level, String parentValue, Map<String, Object> params,
			SortByField[] sortField, String search, RowMapper<T> rowMapper, RequestConfig viewReqConfig) {
		if (level == null)
			level = Integer.MAX_VALUE;
		ViewEn viewEn = viewDefine.getView(viewId);
		if (viewEn == null)
			throw new RuntimeException(String.format("view %s is not exits! ", viewId));
		String treeId=viewEn.getTreeId();
		String treeParent=viewEn.getTreeParent();
		if(treeId==null||treeParent==null) {
			throw new RuntimeException(String.format("View %s is not support for query tree. One of id field, parent field is undefined!", viewId));
		}
		ViewData<TreeData<T>> treeData = new ViewData<>();
		List<TreeData<T>> data = queryTree(viewEn,treeId,treeParent,level,0,parentValue,params,
				 sortField,search,rowMapper,viewReqConfig);
		Map<String, Map<String, String>> valueMapping = getValueMapping(rowMapper,params,viewReqConfig);
		treeData.setValueMapping(valueMapping);
		treeData.setData(data);
		return treeData;
	}
	
	
	public <T> List<TreeData<T>> queryTree(ViewEn viewEn, String keyField, String parentField, Integer level, Integer currentLevel, String parentValue, Map<String, Object> params,
			SortByField[] sortField,String search, RowMapper<T> rowMapper, RequestConfig viewReqConfig){
		currentLevel++;
		List<TreeData<T>> result = new ArrayList<>();
		if (currentLevel > level) return result;
		
		QueryFilter mainQueryFilter = null;
		if (StringUtils.hasText(parentValue)) {
			mainQueryFilter = new QueryFilter(new QueryCriteria(parentField, parentValue));
		} else {
			QueryCriteria queryCriteria = new QueryCriteria();
			queryCriteria.setOp(QueryCriteria.OP_IS_EMPTY);
			queryCriteria.setField(parentField);
			mainQueryFilter = new QueryFilter(queryCriteria);
		}
		
		List<T> nodes = queryView(viewEn, params, sortField, 0, 1000, mainQueryFilter, rowMapper, viewReqConfig);
		
		for (T node : nodes) {
			TreeData<T> treeData = new TreeData<>();
			treeData.setNode(node);
			
			String key = getValue(node,keyField);	
			List<TreeData<T>> subItems = queryTree(viewEn, keyField, parentField, level,
					currentLevel, key, params, sortField, search, rowMapper, viewReqConfig);
			if (subItems.size() > 0) {
				treeData.setChildren(subItems);
				result.add(treeData);
			}else {
				if(StringUtils.hasText(search)) {
					if(viewEn.getTreeSearch()==null||viewEn.getTreeSearch().size()==0) {
						throw new RuntimeException(String.format("View %s did not support search. Please define tree.search!", viewEn.getViewId()));
					}
					for(String field:viewEn.getTreeSearch()) {
						String value = getValue(node,field);
						if(value!=null && value.contains(search)) {
							result.add(treeData);
							break;
						}
					}
				}else {
					result.add(treeData);	
				}
			}
		}
		return result;
	}
	
	public <T> ViewData<TreeData<T>> queryTreePath(String viewId, String keyValue, Map<String, Object> params,
			RowMapper<T> rowMapper,RequestConfig viewReqConfig) {
		log.debug("queryTreePath:" + viewId);
		if (keyValue == null)
			throw new RuntimeException("Value is not allow null!");
		ViewEn viewEn = viewDefine.getView(viewId);
		if (viewEn == null)
			throw new RuntimeException(String.format("view %s is not exits! ", viewId));
		String treeId=viewEn.getTreeId();
		String treeParent=viewEn.getTreeParent();
		if(treeId==null||treeParent==null) {
			throw new RuntimeException(String.format("View %s is not support for query tree. One of id field, parent field is undefined!", viewId));
		}
		ViewData<TreeData<T>> treeData = new ViewData<>();
		List<TreeData<T>> data = queryTreePath(viewEn,treeId,treeParent,keyValue,params,rowMapper,viewReqConfig);
		Map<String, Map<String, String>> valueMapping = getValueMapping(rowMapper,params,viewReqConfig);
		treeData.setValueMapping(valueMapping);
		treeData.setData(data);
		return treeData;
	}
	
	
	public ViewEn getViewInfo(String viewId) {
		return viewDefine.getView(viewId);
	}
	
	public ViewEn getViewInfo(String dataSource, String sql,Map<String, Object> critParams) {
		ViewEn viewEn = queryDao.getViewInfo(dataSource, sql, critParams);
		Map<String, ViewField> fieldMap = new HashMap<>();
		for (ViewField viewField : viewEn.getListFields()) {
			fieldMap.put(viewField.getField(), viewField);
		}
		LinkedHashMap<String,String> paramMap = new LinkedHashMap<>();
		if(critParams!=null) {
			for(String param:critParams.keySet()) {
				Object value = critParams.get(param);
				String type = convertFieldType(param, value.getClass().getName());
				paramMap.put(param, type);
			}
		}
		viewEn.setFieldMap(fieldMap);
		viewEn.setViewId(sql);
		viewEn.setParam(paramMap);
		viewEn.setDataSource(dataSource);
		viewEn.setSql(sql);
		viewDefine.putView(sql, viewEn);
		return viewEn;
	}
	
	private String convertFieldType(String name, String javaType) {
		String fieldType = null;
		if (javaType.equals("java.lang.String")) {
			fieldType = ViewField.FIELDTYPE_STRING;
		} else if (javaType.equals("java.lang.Boolean")) {
			fieldType = ViewField.FIELDTYPE_BOOLEAN;
		} else if (javaType.equals("java.sql.Date")||javaType.equals("java.time.LocalDate")) {
			fieldType = ViewField.FIELDTYPE_DATE;
		} else if (javaType.equals("java.sql.Time")||javaType.equals("java.time.LocalTime")) {
			fieldType = ViewField.FIELDTYPE_TIME;
		} else if (javaType.equals("java.sql.Timestamp")||javaType.equals("java.time.LocalDateTime")) {
			fieldType = ViewField.FIELDTYPE_DATETIME;
			String fieldName = name.toLowerCase();
			if (fieldName.endsWith(ViewField.FIELDTYPE_TIME + "_only")) {
				fieldType = ViewField.FIELDTYPE_TIME;
			}
			if (fieldName.endsWith(ViewField.FIELDTYPE_DATE + "_only")) {
				fieldType = ViewField.FIELDTYPE_DATE;
			}
		} else
			fieldType = ViewField.FIELDTYPE_NUMBER;
		if (fieldType == null) {
			throw new RuntimeException("Not recognize field:[" + name + "] type:[" + javaType + "]!");
		}
		return fieldType;
	}	

	private  <T> List<TreeData<T>> queryTreePath(ViewEn viewEn, String keyField, String parentField, String keyValue, Map<String, Object> params, RowMapper<T> rowMapper,
			RequestConfig viewReqConfig) {
		QueryFilter queryFilter = new QueryFilter(new QueryCriteria(keyField, keyValue));
		List<T> nodes = queryView(viewEn, params, null, 0, 2, queryFilter, rowMapper,
				viewReqConfig);
		List<TreeData<T>> result = new ArrayList<>();
		if (nodes.size() > 1) throw new RuntimeException("More than one record returned!");
		if (nodes.size() == 0) return result;
		T node = nodes.get(0);
		TreeData<T> tree = new TreeData<>();
		tree.setNode(node);
		result.add(tree);
		String parentVal = getValue(node,parentField);
		if (parentVal == null) {
			return result;
		} else {
			List<TreeData<T>> subNotes = queryTreePath(viewEn, keyField, parentField, parentVal,
					params,rowMapper, viewReqConfig);
			result.addAll(subNotes);
			return result;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private String getValue(Object object,String field) {
		try {
			if(object instanceof LinkedHashMap) {
				return FormatterUtils.objectToString(((LinkedHashMap) object).get(field));
			}else {
				Class<?> clazz = object.getClass();
				return FormatterUtils.objectToString(clazz.getField(field).get(object));
			}			
		}catch(Exception e) {
			throw new RuntimeException(String.format("Cannot get value from %s!",field));
		}
	}

	private Map<String, Object> combineParam(ViewEn viewEn, Map<String, Object> params,
			RequestConfig viewReqConfig) {
		Map<String, Object> queryParams = new HashMap<>();
		if (viewReqConfig.getParams() != null) {
			for (String key : viewReqConfig.getParams().keySet()) {
					queryParams.put(key, viewReqConfig.getParams().get(key));
			}
		}
		if (viewEn.getParam() == null) return queryParams;
		LinkedHashMap<String, String> viewParam = viewEn.getParam();
		if (viewParam.keySet().size()>0 && params == null)
			throw new RuntimeException(
					String.format("ViewId %s need to input param! %s", viewEn.getViewId(), viewParam));
		for (String key : viewParam.keySet()) {
			String type = viewParam.get(key);
			if (type == null)
				type = ViewField.FIELDTYPE_STRING;
			Object value = params.get(key);
			if (value == null) {
				if(viewParam.get(key)==null )
				throw new RuntimeException(
						String.format("ViewId %s Param %s is require!", viewEn.getViewId(), key));
			} else {
				if(value instanceof String) {
					queryParams.put(key, SqlUtils.convertObjVal(type, value,viewReqConfig));
				}else {
					queryParams.put(key, value);
				}
			}
		}
		return queryParams;
	}
	
	private <T> Map<String, Map<String, Map<String, Object>>> getEnumFieldsValues(RowMapper<T> rowMapper){
		if(rowMapper instanceof CamelFieldMapper) {
			CamelFieldMapper camelFieldMapper = (CamelFieldMapper) rowMapper;
			return camelFieldMapper.getEnumFieldsValues();
		}
		if(rowMapper instanceof BeanPropRowMapper) {
			BeanPropRowMapper<T> beanPropRowMapper = (BeanPropRowMapper<T>) rowMapper;
			return beanPropRowMapper.getEnumFieldsValues();
		}
		return null;
	}
	
	private <T> List<ViewField> getFieldList(RowMapper<T> rowMapper){
		if(rowMapper instanceof CamelFieldMapper) {
			CamelFieldMapper camelFieldMapper = (CamelFieldMapper) rowMapper;
			return camelFieldMapper.getFieldList();
		}
		if(rowMapper instanceof BeanPropRowMapper) {
			BeanPropRowMapper<T> beanPropRowMapper = (BeanPropRowMapper<T>) rowMapper;
			return beanPropRowMapper.getFieldList();
		}
		return null;
	}
	

	private <T> Map<String, Map<String, String>> getValueMapping(RowMapper<T> rowMapper, Map<String, Object> params,RequestConfig viewReqConfig){
		Map<String, Map<String, Map<String, Object>>> enumFieldsValues = getEnumFieldsValues(rowMapper);
		List<ViewField> fieldList = getFieldList(rowMapper);
		Map<String, Map<String, String>> valueMapping = null;
		if(fieldList==null || enumFieldsValues==null)return valueMapping;
		for (ViewField viewField : fieldList) {
			if ((viewField.getEnumDataMap() != null || viewField.getEnumDataString() != null)) {
				String field = viewField.getField();
				Map<String, Map<String, Object>> values = enumFieldsValues.get(field);
				Map<String, String> enumDataMap = new HashMap<>();
				Map<String, String> valueMapper = new HashMap<>();
				if (viewField.getEnumDataMap() != null) {
					valueMapper.putAll(viewField.getEnumDataMap());
				} else {
					Object val = langDefine.getSetting(viewReqConfig.getLanguage(),
							viewField.getEnumDataString());
					if (val != null && val instanceof EnumDataEn) {
						EnumDataEn valEn = (EnumDataEn) val;
						valueMapper.putAll(valEn.getData());
					}
				}
				if(values!=null) {
					for (String vkey : values.keySet()) {
						String vval = valueMapper.get(vkey);
						enumDataMap.put(vkey, vval);
					}
				}
				if (valueMapping == null)
					valueMapping = new HashMap<>();
				valueMapping.put(viewField.getField(), enumDataMap);
			}
			if (viewField.getEnumApi() != null && enumFieldsValues != null) {
				String field = viewField.getField();
				EnumApiEn enumApiFieldsJO = viewField.getEnumApi();
				String enumViewId = enumApiFieldsJO.getViewId();
				String textField = enumApiFieldsJO.getTextField();
				String valueField = enumApiFieldsJO.getValueField();
				Map<String, Map<String, Object>> values = enumFieldsValues.get(field);
				for (String vkey : values.keySet()) {
					Map<String, Object> item = values.get(vkey);
					Map<String, Object> enumApiParams = new HashMap<>();
					if (params != null)
						enumApiParams.putAll(params);
					LinkedHashMap<String, String> paramJa = enumApiFieldsJO.getParam();
					StringBuffer valueBuffer = new StringBuffer();
					if (paramJa != null) {
						for (String key : paramJa.keySet()) {
							String paramField = paramJa.get(key);
							Object value = item.get(paramField);
							String paramValueStr = FormatterUtils.objectToString(value);
							enumApiParams.put(key, paramValueStr);
							valueBuffer.append(paramValueStr + "_");
						}
					}
					Object filterValue = item.get(field);
					String filterValueString = FormatterUtils.objectToString(filterValue);
					valueBuffer.append(filterValueString);
					QueryFilter enumQueryFilter = new QueryFilter(new QueryCriteria(valueField, filterValueString));
					ViewEn enumViewEn = viewDefine.getView(enumViewId);
					if (enumViewEn == null)
						throw new RuntimeException(String.format("view %s is not exits! ", enumViewId));		
					List<Map<String, Object>> enumApiData = queryView(enumViewEn, enumApiParams, null, 0, 1,
							enumQueryFilter, new CamelFieldMapper(), viewReqConfig);
					if (valueMapping == null)
						valueMapping = new HashMap<>();
					Map<String, String> valueMappingData = valueMapping.get(field);
					if (valueMappingData == null)
						valueMappingData = new HashMap<>();
					for (Map<String, Object> enumApiDataItem : enumApiData) {
						Object textValue = enumApiDataItem.get(textField);
						String textValueStr = FormatterUtils.objectToString(textValue);
						valueMappingData.put(valueBuffer.toString(), textValueStr);
					}
					valueMapping.put(field, valueMappingData);
				}
			}
		}
		return valueMapping;
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
