package org.weaver.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.weaver.query.entity.KeyValueSettingEn;

public interface KeyValueService {
	
	String getValue(KeyValueSettingEn setting,String key);
	
	int setValue(KeyValueSettingEn setting,String key,String value);
	
	int setValue(KeyValueSettingEn setting,String key,String value,String userId);
	
	Map<String,Object> getData(KeyValueSettingEn setting,String key);
	
	int setData(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data,String userId);
}
