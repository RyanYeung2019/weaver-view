package org.weaver.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.weaver.query.entity.KeyValueSettingEn;

/**
*
* @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
* 
*/

@Component("keyValueDao")
public class KeyValueDaoImpl implements KeyValueDao{
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private TableDao tableDao;
	
	public Map<String,Object> getKeyValueTable(KeyValueSettingEn setting,String key){
		String dataSourreBeanName = setting.getDataSourceName();
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourreBeanName), DataSource.class);
		if(setting.getSourceType()==null)setting.setSourceType(tableDao.getDatabaseType(dataSource));
		if(setting.getTypeData()==null)setting.setTypeData(new LinkedHashMap<>());
		setting.getTypeData().put(setting.getKey(), key);
		String selectSql = setting.getSelectSql();
		if(selectSql==null) {
			Map<String,String> tableInfo = SqlUtils.tableNameInfo(setting.getTable(),setting.getSourceType());
			String tableNameSql = tableInfo.get("tableNameSql");
			StringBuffer sql = new StringBuffer("select ");
			sql.append("*");
			sql.append(" from ");
			sql.append(tableNameSql.replace("'","''"));
			sql.append(" where ");
			sql.append(setting.getTypeData().keySet().stream().map(s->SqlUtils.sqlDbKeyWordEscape(s,setting.getSourceType())+"=? ").collect(Collectors.joining(" and ")).replace("'","''"));
			selectSql = sql.toString();
			setting.setSelectSql(selectSql);
		}
		Object[] values = setting.getTypeData().values().stream().toArray();
		List<Map<String,Object>> datas = tableDao.listData(dataSource,values,selectSql);
		if(datas.size()>1)throw new RuntimeException("Return more than one record!");
		if(datas.size()==0) return null;
		return datas.get(0);
	}
	

	public int updateKeyValueTable(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data){
		String dataSourreBeanName = setting.getDataSourceName();
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourreBeanName), DataSource.class);
		if(setting.getSourceType()==null)setting.setSourceType(tableDao.getDatabaseType(dataSource));
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		if(setting.getTypeData()==null)setting.setTypeData(new LinkedHashMap<>());
		setting.getTypeData().put(setting.getKey(), key);
		String updateSql = setting.getUpdateSql();
		if(updateSql==null) {
			Map<String,String> tableInfo = SqlUtils.tableNameInfo(setting.getTable(),setting.getSourceType());
			String tableNameSql = tableInfo.get("tableNameSql");
			StringBuffer sql = new StringBuffer("update ");
			sql.append(tableNameSql.replace("'","''"));
			sql.append(" set ");
			sql.append(data.keySet().stream().map(s->SqlUtils.sqlDbKeyWordEscape(s,setting.getSourceType())+"=? ").collect(Collectors.joining(",")).replace("'","''"));
			sql.append(" where ");
			sql.append(setting.getTypeData().keySet().stream().map(s->SqlUtils.sqlDbKeyWordEscape(s,setting.getSourceType())+"=? ").collect(Collectors.joining(" and ")).replace("'","''"));
			updateSql = sql.toString();
			setting.setUpdateSql(updateSql);
		}
		List<Object> result = new ArrayList<>(data.values());
		result.addAll(new ArrayList<>(setting.getTypeData().values()));
		return jdbcTemplate.update(updateSql,result.stream().toArray());
	}	
	
	public int insertKeyValueTable(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data){
		String dataSourreBeanName = setting.getDataSourceName();
		DataSource dataSource = this.applicationContext.getBean(SqlUtils.getDataSourceName(dataSourreBeanName), DataSource.class);
		if(setting.getSourceType()==null)setting.setSourceType(tableDao.getDatabaseType(dataSource));
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		if(setting.getTypeData()==null)setting.setTypeData(new LinkedHashMap<>());
		setting.getTypeData().put(setting.getKey(), key);
		String insertSql = setting.getInsertSql();
		if(insertSql==null) {
			Map<String,String> tableInfo = SqlUtils.tableNameInfo(setting.getTable(),setting.getSourceType());
			String tableNameSql = tableInfo.get("tableNameSql");
			StringBuffer sql = new StringBuffer("insert into ");
			sql.append(tableNameSql.replace("'","''"));
			sql.append("(");
			sql.append(setting.getTypeData().keySet().stream().map(s->SqlUtils.sqlDbKeyWordEscape(s,setting.getSourceType())).collect(Collectors.joining(",")).replace("'","''")+","); 
			sql.append(data.keySet().stream().map(s->SqlUtils.sqlDbKeyWordEscape(s,setting.getSourceType())).collect(Collectors.joining(",")).replace("'","''")); 
			sql.append(")values(");
			sql.append(setting.getTypeData().keySet().stream().map(s->"?").collect(Collectors.joining(","))+",");
			sql.append(data.keySet().stream().map(s->"?").collect(Collectors.joining(",")));
			sql.append(")");
			insertSql = sql.toString();
			setting.setInsertSql(insertSql);
		}
		List<Object> result = new ArrayList<>(setting.getTypeData().values());
		result.addAll(new ArrayList<>(data.values()));
		return jdbcTemplate.update(insertSql,result.stream().toArray());
	}
	
}
