package org.weaver.view.query;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.weaver.view.query.entity.QueryFilter;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.query.entity.TreeData;
import org.weaver.view.query.entity.ViewData;
import org.weaver.view.query.entity.RequestConfig;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public interface ViewStatement {
	
	void setSql(String sql);

	void setViewId(String viewId);

	void setDataSource(String dataSource);

	public void setParams(Map<String, Object> params);

	public void putParam(String key, Object value);
	
	public void setSortField(SortByField[] sortField) ;

	public void setPageNum(Integer pageNum);

	public void setPageSize(Integer pageSize);

	public void setQueryFilter(QueryFilter queryFilter);

	public void setAggrList(List<String> aggrList);

	public void setLevel(Integer level) ;

	public void setSearch(String search);

	public void setValue(String value);
	
	public void setViewReqConfig(RequestConfig viewReqConfig);

	public <T> ViewData<T> query(RowMapper<T> rowMapper);

	public ViewData<Map<String, Object>> query();

	
	public ViewData<TreeData<Map<String, Object>>> queryTree()throws Exception ;
	
	public <T> ViewData<TreeData<T>> queryTree( RowMapper<T> rowMapper);
}
