package org.weaver.query.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class QueryCriteria {
	
	public static String OP_EQUAL = "EQUAL";
	public static String OP_CONTAINS = "CONTAINS";
	public static String OP_STARTS_WITH = "STARTS_WITH";
	public static String OP_ENDS_WITH = "ENDS_WITH";
	public static String OP_LESS_THAN = "LESS_THAN";
	public static String OP_LESS_THAN_OR_EQUAL = "LESS_THAN_OR_EQUAL";
	public static String OP_LARGER_THAN = "LARGER_THAN";
	public static String OP_LARGER_THAN_OR_EQUAL = "LARGER_THAN_OR_EQUAL";
	public static String OP_IS_EMPTY = "IS_EMPTY";
	
	private String field;
	private boolean opNot = false;
	private String op = OP_EQUAL;
	private String value;

	private String type = QueryFilter.TYPE_AND;
	private List<QueryCriteria> criteria;

	public QueryCriteria() {
		super();
	}

	public QueryCriteria(QueryCriteria... criteriaArray) {
		super();
		this.criteria = new ArrayList<>();
		for (QueryCriteria item : criteriaArray) {
			if (item != null)
				this.criteria.add(item);
		}
	}

	public QueryCriteria(List<QueryCriteria> criteria) {
		super();
		this.criteria = criteria;
	}

	public void addCriteria(QueryCriteria... criteria) {
		for (QueryCriteria item : criteria) {
			if (item != null)
				this.criteria.add(item);
		}
	}

	public QueryCriteria(String field, String value) {
		super();
		this.field = field;
		this.value = value;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<QueryCriteria> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<QueryCriteria> criteria) {
		this.criteria = criteria;
	}

	public boolean isOpNot() {
		return opNot;
	}

	public void setOpNot(boolean opNot) {
		this.opNot = opNot;
	}

}
