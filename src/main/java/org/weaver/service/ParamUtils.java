package org.weaver.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.weaver.query.entity.QueryFilter;
import org.weaver.query.entity.SortByField;
import org.weaver.view.util.Utils;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

class ParamUtils {

	static String stringDecoder(String value) {
		if (value == null)
			return null;
		return Utils.urlDecoder(value);
	}

	static List<String> aggrConver(String[] aggrs) {
		if (aggrs == null)
			return null;
		List<String> result = new ArrayList<>();
		for (String aggr : aggrs) {
			String[] aggrArray = aggr.split("-");
			String field = aggrArray[0];
			try {
				String sumType = aggrArray[1];
				result.add(field + "-" + sumType);
			} catch (Exception e) {
			}

		}
		return result;
	}

	static SortByField[] sortFieldConver(String[] sort) {
		if (sort == null)
			return null;
		List<SortByField> sortField = new ArrayList<SortByField>();
		for (String item : sort) {
			String[] sortArray = item.split("-");
			String key = sortArray[0];
			String sortType = SortByField.ASC;
			try {
				String value = sortArray[1];
				if (value.equals("d"))
					sortType = SortByField.DESC;
			} catch (Exception e) {
			}
			sortField.add(new SortByField(key, sortType));
		}
		return sortField.toArray(new SortByField[sortField.size()]);
	}


	static QueryFilter filterConver(String filter) {
		JSONObject filterCriteria = null;
		if (StringUtils.hasText(filter)) {
			//filterCriteria = JSON.parseObject(Utils.urlDecoder(filter));
			filterCriteria = SafeJson.safeParseToJSONObject(Utils.urlDecoder(filter));
		}
		return  filterCriteria != null ? new QueryFilter(filterCriteria) : null;
	}
}
