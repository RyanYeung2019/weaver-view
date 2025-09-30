package org.weaver.service;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.weaver.config.entity.ViewEn;
import org.weaver.query.entity.SortByField;

public interface ViewDao {

	ViewEn getViewInfo(String dataSource, String sql, Map<String, Object> critParams);

	LinkedHashMap<String, Object> queryViewAggregate(ViewEn viewEn, Map<String, Object> queryParams,
			FilterCriteria filter, List<String> aggrField);

	<T> List<T> queryData(ViewEn viewEn, Map<String, Object> queryParams, SortByField[] sortField, Integer pNum,
			Integer pSize, FilterCriteria filter, RowMapper<T> rowMapper);



	
}
