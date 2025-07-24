package org.weaver.view.query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.weaver.config.LangDefine;
import org.weaver.config.entity.ViewEn;
import org.weaver.view.query.entity.QueryFilter;
import org.weaver.view.query.entity.SortByField;
import org.weaver.view.query.entity.TreeData;
import org.weaver.view.query.entity.ViewData;
import org.weaver.view.query.entity.RequestConfig;
import org.weaver.view.query.mapper.CamelFieldMapper;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class ViewStatementImpl implements ViewStatement {

	private ViewService viewService;
	private String viewId;
	private Map<String, Object> params;
	private SortByField[] sortField;


	private Integer pageNum;
	private Integer pageSize;
	private QueryFilter queryFilter;
	private List<String> aggrList;
	private RequestConfig viewReqConfig = new RequestConfig();
	private String dataSource = "dataSource";
	
	private String sql;
	private Integer level;
	private String search;
	private String value;	
	

	ViewStatementImpl(ViewService viewService) {
		super();
		this.viewService = viewService;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public void putParam(String key, Object value) {
		if (this.params == null)
			this.params = new HashMap<>();
		this.params.put(key, value);
	}

	public void setSortField(SortByField[] sortField) {
		this.sortField = sortField;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public void setQueryFilter(QueryFilter queryFilter) {
		this.queryFilter = queryFilter;
	}

	public void setAggrList(List<String> aggrList) {
		this.aggrList = aggrList;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public void setValue(String value) {
		this.value = value;
	}	
	
	public void setViewReqConfig(RequestConfig viewReqConfig) {
		String lang = viewReqConfig.getLanguage();
		SimpleDateFormat dateFormat = (SimpleDateFormat) viewService.getSetting(lang, LangDefine.FORMAT_DATE);
		if (dateFormat != null) {
			viewReqConfig.setDateFormat(dateFormat);
		}
		SimpleDateFormat timeFormat = (SimpleDateFormat) viewService.getSetting(lang, LangDefine.FORMAT_TIME);
		if (timeFormat != null) {
			viewReqConfig.setTimeFormat(timeFormat);
		}
		SimpleDateFormat datetimeFormat = (SimpleDateFormat) viewService.getSetting(lang, LangDefine.FORMAT_DATETIME);
		if (datetimeFormat != null) {
			viewReqConfig.setDatetimeFormat(datetimeFormat);
		}
		this.viewReqConfig = viewReqConfig;
	}

	public <T> ViewData<T> query(RowMapper<T> rowMapper) {
		if (viewId != null) {
			ViewEn viewEn = viewService.getViewInfo(viewId);
			if (viewEn == null)
				throw new RuntimeException(String.format("view %s is not exits! ", viewId));
			return viewService.query(viewEn, params, sortField, pageNum, pageSize, queryFilter, aggrList, rowMapper,
					viewReqConfig);
		} else {
			ViewEn viewEn = viewService.getViewInfo(sql);
			if (viewEn == null) {
				viewEn = viewService.getViewInfo(this.dataSource, sql, params);
			}
			return viewService.query(viewEn, params, sortField, pageNum, pageSize, queryFilter, aggrList, rowMapper,
					viewReqConfig);
		}
	}

	public ViewData<Map<String, Object>> query() {
		return query(new CamelFieldMapper());
	}

	
	public ViewData<TreeData<Map<String, Object>>> queryTree()throws Exception {
		return queryTree(new CamelFieldMapper());
	}
	
	public <T> ViewData<TreeData<T>> queryTree( RowMapper<T> rowMapper) {
		Date startTime = new Date();
		String keyValue = ParamUtils.stringDecoder(value);
		ViewData<TreeData<T>> data = new ViewData<>();
		if (sortField != null) {
			String searchValue = ParamUtils.stringDecoder(search);
			data = viewService.queryTree(viewId, level, keyValue, params, sortField, searchValue, rowMapper, viewReqConfig);
		} else {
			data = viewService.queryTreePath(viewId, keyValue, params, rowMapper, viewReqConfig);
		}
		ViewEn viewEn = viewService.getViewInfo(viewId);
		if (viewEn == null)
			throw new RuntimeException(String.format("view %s is not exits! ", viewId));
		viewService.updateViewInfo(viewEn, data, viewReqConfig);
		data.setStartTime(startTime);
		data.setEndTime(new Date());
		return data;	
	}	
}
