package org.weaver.query.entity;

import java.util.ArrayList;
import java.util.List;
import org.weaver.config.entity.ViewField;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class QueryFilter {
	
	public static final String TYPE_AND = "and";
	public static final String TYPE_OR = "or";

	String type = TYPE_AND;
	List<QueryCriteria> criteria;

	JSONObject jsonObject;
	
	List<QueryFilter> queryFilters = new ArrayList<>();

	String primarySearchValue;

	public QueryFilter(QueryCriteria... criteria) {
		super();
		this.criteria = new ArrayList<>();
		for (QueryCriteria item : criteria) {
			if (item != null)
				this.criteria.add(item);
		}
	}

	public QueryFilter(List<QueryCriteria> criteria) {
		super();
		this.criteria = criteria;
	}
	
	public QueryFilter(JSONObject jsonObject) {
		super();
		this.jsonObject = jsonObject;
	}

	public void addFilter(QueryFilter... filters) {
		for (QueryFilter filter : filters) {
			if (filter != null)
				queryFilters.add(filter);
		}		
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPrimarySearchValue() {
		return primarySearchValue;
	}

	public void setPrimarySearchValue(String primarySearchValue) {
		this.primarySearchValue = primarySearchValue;
	}

	public JSONObject toJSONObject() {
		JSONObject result = filter(this);
		if (this.jsonObject != null) {
			result = this.jsonObject;
		}
		if(queryFilters.size()>0) {
			JSONArray criteria = result.getJSONArray("criteria");
			for(QueryFilter queryFilter:queryFilters) {
				criteria.add(queryFilter.toJSONObject());
			}
		}
		return result;
	}

	private JSONObject filter(QueryFilter queryFilter) {
		if (queryFilter != null && queryFilter.criteria == null)
			return null;
		JSONArray criteria = new JSONArray();
		for (QueryCriteria item : queryFilter.criteria) {
			if (item.getCriteria() != null) {
				JSONObject crta = loopCriteria(item.getCriteria(), item.getType());
				criteria.add(crta);
			} else {
				JSONObject crta = new JSONObject();
				crta.put("field", item.getField());
				String op = item.getOp();
				if (item.isOpNot())
					op = "not" + op;
				crta.put("op", op);
				crta.put("value", item.getValue());
				criteria.add(crta);
			}
		}
		JSONObject filterJO = new JSONObject();
		filterJO.put("type", queryFilter.getType());
		filterJO.put("criteria", criteria);
		return filterJO;
	}

	private JSONObject loopCriteria(List<QueryCriteria> queryCriteria, String type) {
		JSONArray criteria = new JSONArray();
		for (QueryCriteria item : queryCriteria) {
			if (item.getCriteria() != null) {
				JSONObject crta = loopCriteria(item.getCriteria(), item.getType());
				criteria.add(crta);
			} else {
				JSONObject crta = new JSONObject();
				crta.put("field", item.getField());
				String op = item.getOp();
				if (item.isOpNot())
					op = "not" + op;
				crta.put("op", op);
				crta.put("value", item.getValue());
				criteria.add(crta);
			}
		}
		JSONObject filterJO = new JSONObject();
		filterJO.put("type", ViewField.FIELDTYPE_STRING.equals(type) ? QueryFilter.TYPE_AND : type);
		filterJO.put("criteria", criteria);
		return filterJO;
	}

}
