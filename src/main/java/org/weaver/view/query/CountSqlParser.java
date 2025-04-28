package org.weaver.view.query;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

class CountSqlParser {
	public static final String KEEP_ORDERBY = "/*keep orderby*/";

	private static final Alias TABLE_ALIAS;

	static {
		TABLE_ALIAS = new Alias("table_count");
		TABLE_ALIAS.setUseAs(false);
	}
	private String defaultCountFieldName;

	CountSqlParser() {
		super();
	}

	CountSqlParser(String defaultCountFieldName) {
		super();
		this.defaultCountFieldName = defaultCountFieldName;
	}

	String getSmartCountSql(String sql) {
		return getSmartCountSql(sql, "0");
	}

	String getSmartAgg(String sql, List<String> aggregate) {
		return getSmartAggSql(sql, aggregate);
	}

	String getSmartAggSql(String sql, List<String> aggregate) {
		Statement stmt = null;
		if (sql.indexOf(KEEP_ORDERBY) >= 0) {
			return getSimpleCountAggSql(sql, aggregate);
		}
		try {
			stmt = CCJSqlParserUtil.parse(sql);
		} catch (Throwable e) {
			return getSimpleCountAggSql(sql, aggregate);
		}
		Select select = (Select) stmt;
		SelectBody selectBody = select.getSelectBody();
		try {
			processSelectBody(selectBody);
		} catch (Exception e) {
			return getSimpleCountAggSql(sql, aggregate);
		}
		processWithItemsList(select.getWithItemsList());
		sqlToAggregate(select, aggregate);
		String result = select.toString();
		return result;
	}

	String getSmartCountSql(String sql, String name) {
		Statement stmt = null;
		if (sql.indexOf(KEEP_ORDERBY) >= 0) {
			return getSimpleCountSql(sql);
		}
		try {
			stmt = CCJSqlParserUtil.parse(sql);
		} catch (Throwable e) {
			return getSimpleCountSql(sql);
		}
		Select select = (Select) stmt;
		SelectBody selectBody = select.getSelectBody();
		try {
			processSelectBody(selectBody);
		} catch (Exception e) {
			return getSimpleCountSql(sql);
		}
		processWithItemsList(select.getWithItemsList());
		sqlToCount(select, name);
		String result = select.toString();
		return result;
	}

	String getSimpleCountAggSql(final String sql, List<String> aggregate) {
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

	String getSimpleCountSql(final String sql) {
		return getSimpleCountSql(sql, "0");
	}

	String getSimpleCountSql(final String sql, String name) {
		StringBuilder stringBuilder = new StringBuilder(sql.length() + 40);
		stringBuilder.append("select count(");
		stringBuilder.append(name);
		stringBuilder.append(") from (");
		stringBuilder.append(sql);
		stringBuilder.append(") " + SqlUtils.varName());
		return stringBuilder.toString();
	}

	void sqlToAggregate(Select select, List<String> aggregate) {
		SelectBody selectBody = select.getSelectBody();
		List<SelectItem> COUNT_ITEM = new ArrayList<>();
		for (String fieldAggr : aggregate) {
			String[] fieldAggrArr = fieldAggr.split("-");
			String field = fieldAggrArr[0];
			String aggType = fieldAggrArr[1];
			COUNT_ITEM.add(new SelectExpressionItem(new Column(
					String.format("%s(%s) as %s", aggType, field, field.equals("0") ? defaultCountFieldName : (field+"_"+aggType.toLowerCase()) ))));
		}
		if (selectBody instanceof PlainSelect && isSimpleCount((PlainSelect) selectBody)) {
			((PlainSelect) selectBody).setSelectItems(COUNT_ITEM);
		} else {
			PlainSelect plainSelect = new PlainSelect();
			SubSelect subSelect = new SubSelect();
			subSelect.setSelectBody(selectBody);
			subSelect.setAlias(TABLE_ALIAS);
			plainSelect.setFromItem(subSelect);
			plainSelect.setSelectItems(COUNT_ITEM);
			select.setSelectBody(plainSelect);
		}
	}

	void sqlToCount(Select select, String name) {
		SelectBody selectBody = select.getSelectBody();
		List<SelectItem> COUNT_ITEM = new ArrayList<>();
		COUNT_ITEM.add(new SelectExpressionItem(new Column("count(" + name + ")")));
		if (selectBody instanceof PlainSelect && isSimpleCount((PlainSelect) selectBody)) {
			((PlainSelect) selectBody).setSelectItems(COUNT_ITEM);
		} else {
			PlainSelect plainSelect = new PlainSelect();
			SubSelect subSelect = new SubSelect();
			subSelect.setSelectBody(selectBody);
			subSelect.setAlias(TABLE_ALIAS);
			plainSelect.setFromItem(subSelect);
			plainSelect.setSelectItems(COUNT_ITEM);
			select.setSelectBody(plainSelect);
		}
	}

	boolean isSimpleCount(PlainSelect select) {
		if ((select.getGroupByColumnReferences() != null) || (select.getDistinct() != null)) {
			return false;
		}
		for (SelectItem item : select.getSelectItems()) {
			if (item.toString().contains("?")) {
				return false;
			}
			if (item instanceof SelectExpressionItem) {
				if (((SelectExpressionItem) item).getExpression() instanceof Function) {
					return false;
				}
			}
		}
		return true;
	}

	void processSelectBody(SelectBody selectBody) {
		if (selectBody instanceof PlainSelect) {
			processPlainSelect((PlainSelect) selectBody);
		} else if (selectBody instanceof WithItem) {
			WithItem withItem = (WithItem) selectBody;
			if (withItem.getSelectBody() != null) {
				processSelectBody(withItem.getSelectBody());
			}
		} else {
			SetOperationList operationList = (SetOperationList) selectBody;
			if (operationList.getSelects() != null && operationList.getSelects().size() > 0) {
				List<SelectBody> plainSelects = operationList.getSelects();
				for (SelectBody plainSelect : plainSelects) {
					processSelectBody(plainSelect);
				}
			}
			if (!orderByHashParameters(operationList.getOrderByElements())) {
				operationList.setOrderByElements(null);
			}
		}
	}

	void processPlainSelect(PlainSelect plainSelect) {
		if (!orderByHashParameters(plainSelect.getOrderByElements())) {
			plainSelect.setOrderByElements(null);
		}
		if (plainSelect.getFromItem() != null) {
			processFromItem(plainSelect.getFromItem());
		}
		if (plainSelect.getJoins() != null && plainSelect.getJoins().size() > 0) {
			List<Join> joins = plainSelect.getJoins();
			for (Join join : joins) {
				if (join.getRightItem() != null) {
					processFromItem(join.getRightItem());
				}
			}
		}
	}

	void processWithItemsList(List<WithItem> withItemsList) {
		if (withItemsList != null && withItemsList.size() > 0) {
			for (WithItem item : withItemsList) {
				processSelectBody(item.getSelectBody());
			}
		}
	}

	void processFromItem(FromItem fromItem) {
		if (fromItem instanceof SubJoin) {
			SubJoin subJoin = (SubJoin) fromItem;
			if (subJoin.getJoin() != null) {
				if (subJoin.getJoin().getRightItem() != null) {
					processFromItem(subJoin.getJoin().getRightItem());
				}
			}
			if (subJoin.getLeft() != null) {
				processFromItem(subJoin.getLeft());
			}
		} else if (fromItem instanceof SubSelect) {
			SubSelect subSelect = (SubSelect) fromItem;
			if (subSelect.getSelectBody() != null) {
				processSelectBody(subSelect.getSelectBody());
			}
		} else if (fromItem instanceof ValuesList) {

		} else if (fromItem instanceof LateralSubSelect) {
			LateralSubSelect lateralSubSelect = (LateralSubSelect) fromItem;
			if (lateralSubSelect.getSubSelect() != null) {
				SubSelect subSelect = lateralSubSelect.getSubSelect();
				if (subSelect.getSelectBody() != null) {
					processSelectBody(subSelect.getSelectBody());
				}
			}
		}
	}

	boolean orderByHashParameters(List<OrderByElement> orderByElements) {
		if (orderByElements == null) {
			return false;
		}
		for (OrderByElement orderByElement : orderByElements) {
			if (orderByElement.toString().contains("?")) {
				return true;
			}
		}
		return false;
	}
}
