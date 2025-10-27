package org.weaver.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.weaver.query.entity.KeyValueSettingEn;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

@Component("keyValueService")
public class KeyValueServiceImpl implements KeyValueService {
	
	private static final Logger log = LoggerFactory.getLogger(KeyValueService.class);

	@Autowired
	KeyValueDao keyValueDao;
	
	public Long getNextSerialId(KeyValueSettingEn setting,String key) {
        String val = this.getValue(setting,key);
        Long next = (val==null?0l:Long.valueOf(val))+1;
        this.setValue(setting,key, String.valueOf(next));
        return next;
	}
	
	public String getValue(KeyValueSettingEn setting,String key) {
		log.info("getValue");
		Map<String,Object> result = keyValueDao.getKeyValueTable(setting,key);
		if(result==null) return null;
		Object value = result.get(setting.getValue());
		if(value==null) return null;
		return value.toString();
	}
	
	public int setValue(KeyValueSettingEn setting,String key,String value) {
		LinkedHashMap<String,Object> data =  new LinkedHashMap<>();
		data.put(setting.getValue(), value);
		return setData(setting,key,data,null);
	}
	
	public int setValue(KeyValueSettingEn setting,String key,String value,String userId) {
		LinkedHashMap<String,Object> data =  new LinkedHashMap<>();
		data.put(setting.getValue(), value);
		return setData(setting,key,data,userId);
	}	
	
	public Map<String,Object> getData(KeyValueSettingEn setting,String key) {
		return keyValueDao.getKeyValueTable(setting,key);
	}	
	
	public int setData(KeyValueSettingEn setting,String key,LinkedHashMap<String,Object> data,String userId) {
		if(getData(setting,key)==null) {
			if(setting.getCreateUser()!=null && userId != null)data.put(setting.getCreateUser(), userId);
			if(setting.getCreateDate()!=null)data.put(setting.getCreateDate(), new Date());
			return keyValueDao.insertKeyValueTable(setting, key, data);
		}else {
			if(setting.getUpdateUser()!=null && userId != null)data.put(setting.getUpdateUser(), userId);
			if(setting.getUpdateDate()!=null)data.put(setting.getUpdateDate(), new Date());
			return keyValueDao.updateKeyValueTable(setting, key, data);
		}
	}

}
