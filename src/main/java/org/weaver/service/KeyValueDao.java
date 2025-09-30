package org.weaver.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.weaver.query.entity.KeyValueSettingEn;
/**
*
* @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
* 
*/

public interface KeyValueDao {
	
	Map<String,Object> getKeyValueTable(KeyValueSettingEn setting,String key);
	
	int updateKeyValueTable(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data);
	
	int insertKeyValueTable(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data);
}
