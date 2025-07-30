package org.weaver.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.weaver.config.entity.ViewField;
import org.weaver.query.entity.QueryCriteria;
import org.weaver.query.entity.QueryFilter;
import org.weaver.query.entity.RequestConfig;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

class SqlUtils {

	private static final Logger log = LoggerFactory.getLogger(SqlUtils.class);



	public static final String NAME_ORACLE = "oracle";
	public static final String NAME_PGSQL = "postgres";
	public static final String NAME_MSSQL = "mssql";
	public static final String NAME_MYSQL = "mysql";

	private static String[] OPS = { 
			QueryCriteria.OP_EQUAL, 
			QueryCriteria.OP_CONTAINS, 
			QueryCriteria.OP_STARTS_WITH,
			QueryCriteria.OP_ENDS_WITH, 
			QueryCriteria.OP_LESS_THAN,
			QueryCriteria.OP_LESS_THAN_OR_EQUAL,
			QueryCriteria.OP_LARGER_THAN, 
			QueryCriteria.OP_LARGER_THAN_OR_EQUAL, 
			QueryCriteria.OP_IS_EMPTY };

	public static String varName() {
		return "A"+UUID.randomUUID().toString().replace("-", "");
	}

	public static FilterCriteria paramFilter(QueryFilter queryFilter, List<ViewField> viewFields, String dataSourceType,RequestConfig viewReqConfig) {
		if(queryFilter==null)return null;
		JSONObject jsonObject = queryFilter.toJSONObject();
		if(jsonObject==null)return null;
		FilterCriteria filterCriteria = new FilterCriteria();
		if (!jsonObject.containsKey("criteria")) {
			log.warn("JSON FilterCriteria not contains keyword:[criteria]");
			filterCriteria.setDbSql("1=2");
			return filterCriteria;
		} else {
			log.debug("filter:"+jsonObject.toString());
			Map<String, String> result = loopFilterCriteria(jsonObject, filterCriteria, viewFields, dataSourceType,viewReqConfig);
			filterCriteria.setPrimarySearchValue(result.get("primarySearchValue"));
			return filterCriteria;
		}
	}

	private static Map<String, String> loopFilterCriteria(JSONObject jsonObject, FilterCriteria filterCriteria,
			List<ViewField> viewFields, String dataSourceType,RequestConfig viewReqConfig) {
		Map<String, String> result = new HashMap<>();
		if (jsonObject.containsKey("criteria")) {
			filterCriteria.setType(jsonObject.getString("type"));// [and/or]
			JSONArray jsonArray = jsonObject.getJSONArray("criteria");
			if (jsonArray.size() > 0) {
				List<FilterCriteria> filterCriteriaList = new ArrayList<>();
				StringBuilder subSql = new StringBuilder();
				Map<String, Object> paramMap = new HashMap<>();
				boolean first = true;
				for (int idx = 0; idx < jsonArray.size(); idx++) {
					JSONObject subJsonObject = jsonArray.getJSONObject(idx);
					FilterCriteria subFilterCriteria = new FilterCriteria();
					result.putAll(loopFilterCriteria(subJsonObject, subFilterCriteria, viewFields, dataSourceType,viewReqConfig));
					if (subFilterCriteria.getFilterCriteria() != null
							&& subFilterCriteria.getFilterCriteria().size() > 0) {
						if (subFilterCriteria.getParamMap() != null
								&& subFilterCriteria.getParamMap().keySet().size() > 0) {
							paramMap.putAll(subFilterCriteria.getParamMap());
						}
					} else {
						if (subFilterCriteria.getDbKey() != null) {
							paramMap.put(subFilterCriteria.getDbKey(), subFilterCriteria.getDbValue());
						}
					}
					if (first) {
						subSql.append(subFilterCriteria.getDbSql());
					} else {
						subSql.append(" " + filterCriteria.getType() + " " + subFilterCriteria.getDbSql());
					}
					filterCriteriaList.add(subFilterCriteria);
					first = false;
				}
				if (jsonArray.size() > 1) {
					subSql.insert(0, "(").append(")");
				}
				filterCriteria.setDbSql(subSql.toString());
				filterCriteria.setParamMap(paramMap);
				filterCriteria.setFilterCriteria(filterCriteriaList);
			}
		} else {
			String field = jsonObject.getString("field");
			int idx = viewFields.indexOf(new ViewField(field));
			if (idx < 0)
				throw new RuntimeException("field not found " + field);
			field = viewFields.get(idx).getFieldDb();
			filterCriteria.setField(field);
			String fieldType = viewFields.get(idx).getType();
			String opSelect = jsonObject.getString("op");
			filterCriteria.setType(fieldType);
			Object schValue = null;
			if (jsonObject.containsKey("value"))
				schValue = jsonObject.get("value");
			if (schValue instanceof String) {
				String val = (String) schValue;
				if (StringUtils.hasText(val)) {
					if (jsonObject.containsKey("primarySearch") && jsonObject.getBoolean("primarySearch"))
						result.put("primarySearchValue", val);
				} else
					schValue = null;
			}
			if (opSelect.startsWith(FilterCriteria.NOT)) {
				filterCriteria.setNot(true);
				filterCriteria.setOp(opSelect.replace(FilterCriteria.NOT, ""));
			} else {
				filterCriteria.setNot(false);
				filterCriteria.setOp(opSelect);
			}
			filterCriteria.setVal(convertObjVal(fieldType, schValue, viewReqConfig));
			calDbSqlVal(filterCriteria, dataSourceType);
		}
		return result;
	}

	public static Object convertObjVal(String type, Object val,RequestConfig viewReqConfig) {
		if(val ==null) return null;
		Object result = val;
		if (ViewField.FIELDTYPE_BOOLEAN.equals(type)) {
			result = Boolean.valueOf(val.toString());
		}
		if (ViewField.FIELDTYPE_DATE.equals(type) && val instanceof String ) {
			try {
				result = viewReqConfig.getDateFormat().parse(val.toString());
			} catch (ParseException e) {
				throw new RuntimeException(String.format("Cannot parse %s to %s ",val,ViewField.FIELDTYPE_DATE));
			}
		}
		if ( ViewField.FIELDTYPE_TIME.equals(type) && val instanceof String ) {
			try {
				result = viewReqConfig.getTimeFormat().parse(val.toString());
			} catch (ParseException e) {
				throw new RuntimeException(String.format("Cannot parse %s to %s ",val,ViewField.FIELDTYPE_TIME));
			}
		}
		if (ViewField.FIELDTYPE_DATETIME.equals(type) && val instanceof String ) {
			try {
				result = viewReqConfig.getDatetimeFormat().parse(val.toString());
			} catch (ParseException e) {
				throw new RuntimeException(String.format("Cannot parse %s to %s ",val,ViewField.FIELDTYPE_DATETIME));
			}
		}
		if (ViewField.FIELDTYPE_NUMBER.equals(type) && val instanceof String) {
			if (val.toString().contains(".")) {
				result = Double.parseDouble(val.toString());
			} else {
				result = Long.parseLong(val.toString());
			}
		}
		if (ViewField.FIELDTYPE_STRING.equals(type) && !StringUtils.hasText((String) val)) {
			result = null;
		}
	
		return result;
	}

	private static void calDbSqlVal(FilterCriteria filterCriteria, String dataSourceType) {
		String field = filterCriteria.getField();
		String type = filterCriteria.getType();
		Object val = filterCriteria.getVal();
		if (type == null)
			type = ViewField.FIELDTYPE_STRING;
		String operation = filterCriteria.getOp().toUpperCase();
		if (!(Arrays.asList(OPS).contains(operation))) {
			throw new RuntimeException("adv sch operation not exists:" + operation);
		}

		if (field == null) {
			throw new RuntimeException("adv sch field not exists:" + field);
		}
		String fieldStr = NAME_MYSQL.equals(dataSourceType) ? field : "\"" + field + "\"";
		if (operation.equalsIgnoreCase(QueryCriteria.OP_IS_EMPTY)) {
			if (filterCriteria.getNot()) {
				filterCriteria.setDbSql(fieldStr + " is not null");
			} else {
				filterCriteria.setDbSql(fieldStr + " is null");
			}
		} else {
			String op = null;
			if (type.equals(ViewField.FIELDTYPE_STRING)) {
				if (operation.equalsIgnoreCase(QueryCriteria.OP_EQUAL)) {
					if (val == null)
						throw new RuntimeException(String.format("field [%s] not allow empty!", field));
					op = "=";
					if (filterCriteria.getNot()) {
						op = "!=";
					}
				} else {
					op = dataSourceType.equals(NAME_PGSQL) ? "ilike" : "like";
					if (filterCriteria.getNot()) {
						op = dataSourceType.equals(NAME_PGSQL) ? "not ilike" : "not like";
					}
					if (operation.equalsIgnoreCase(QueryCriteria.OP_CONTAINS)) {
						val = "%" + (val == null ? "" : val) + "%";
					}
					if (operation.equalsIgnoreCase(QueryCriteria.OP_STARTS_WITH)) {
						val = (val == null ? "" : val) + "%";
					}
					if (operation.equalsIgnoreCase(QueryCriteria.OP_ENDS_WITH)) {
						val = "%" + (val == null ? "" : val);
					}
				}
			} else {
				if (val == null)
					throw new RuntimeException(String.format("field [%s] not allow empty!", field));
				if (operation.equalsIgnoreCase(QueryCriteria.OP_EQUAL)) {
					op = "=";
					if (filterCriteria.getNot()) {
						op = "!=";
					}
				}
				if (operation.equalsIgnoreCase(QueryCriteria.OP_LESS_THAN)) {
					op = "<";
				}
				if (operation.equalsIgnoreCase(QueryCriteria.OP_LESS_THAN_OR_EQUAL)) {
					op = "<=";
				}
				if (operation.equalsIgnoreCase(QueryCriteria.OP_LARGER_THAN)) {
					op = ">";
				}
				if (operation.equalsIgnoreCase(QueryCriteria.OP_LARGER_THAN_OR_EQUAL)) {
					op = ">=";
				}
			}
			String id = varName();
			filterCriteria.setDbSql(fieldStr + " " + op + " :" + id);
			filterCriteria.setDbValue(val);
			filterCriteria.setDbKey(id);
		}
	}

}
