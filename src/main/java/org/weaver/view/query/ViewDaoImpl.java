package org.weaver.view.query;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.stereotype.Component;
import org.weaver.config.entity.ViewEn;
import org.weaver.config.entity.ViewField;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.query.entity.ViewData;
import org.weaver.view.util.FormatterUtils;

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
		try {
			DataSource dataSource = this.applicationContext.getBean(dataSourceName, DataSource.class);
			try (Connection conn = dataSource.getConnection();) {
				return getDatabaseType(conn);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
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
						String type = rem.getColumnClassName(i);
						Integer precision = rem.getPrecision(i);
						Integer scale = rem.getScale(i);
						String typeDb = rem.getColumnTypeName(i);
						int nullable = rem.isNullable(i);
						String fieldName = FormatterUtils.toCamelCase(name);
						ViewField viewField = new ViewField(fieldName);
						viewField.setFieldDb(name);
						viewField.setType(FormatterUtils.convertFieldType(name, type));
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
		CountSqlParser countSqlParser = new CountSqlParser(defaultCountFieldName);
		String sqlCount = countSqlParser.getSmartAgg(queryString, aggParam);
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

}
