package org.weaver.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import org.springframework.jdbc.support.JdbcUtils;
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
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.weaver.config.entity.ViewEn;
import org.weaver.config.entity.ViewField;
import org.weaver.query.entity.SortByField;
import org.weaver.query.entity.ViewData;
import org.weaver.table.entity.DatabaseType;
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

	@Autowired
	private TableDao tableDao;

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
		DatabaseType dataSourceType = viewEn.getSourceType();
		DataSource dataSource = this.applicationContext.getBean(dataSourreBeanName, DataSource.class);
		int pageSize = pSize==null?50:pSize;
		int pageNum = pNum==null?0:pNum;
		int limit = pageSize;
		int offset = pageNum > 1 ? pageSize * (pageNum - 1) : 0;
		String queryString;
		if (dataSourceType.getType().equals(SqlUtils.NAME_ORACLE)) {
			queryString = "SELECT * FROM(SELECT rownum rnum,a.* FROM(" + sql + " ORDER BY " + orderBy
					+ ")a WHERE rownum<=" + offset + "+" + limit + ")WHERE rnum>=" + (offset + 1);
		} else {
			queryString = "SELECT * FROM("+sql+")"+SqlUtils.varName()+" ORDER BY " + orderBy + " LIMIT " + limit + " OFFSET " + offset;
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

	public ViewEn getViewInfo(String dataSourreBeanName, String sql,Map<String, Object> critParams) {
		String dataSourceName = SqlUtils.getDataSourceName(dataSourreBeanName);
		DataSource ds = this.applicationContext.getBean(dataSourceName, DataSource.class);
		DatabaseType sourceType = tableDao.getDatabaseType(ds);
		ViewEn viewEn = new ViewEn();
		List<ViewField> listFields = tableDao.listFieldType(ds,sourceType, sql, critParams);
		viewEn.setListFields(listFields);
		viewEn.setDataSource(dataSourceName);
		viewEn.setSourceType(sourceType);
		return viewEn;
	}
	

	
}
