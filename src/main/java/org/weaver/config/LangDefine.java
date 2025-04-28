package org.weaver.config;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.weaver.config.entity.EnumApiEn;
import org.weaver.config.entity.EnumDataEn;
import org.weaver.view.util.Utils;
import org.weaver.view.util.YamlRecursion;

@Component("langDefine")
public class LangDefine {

	@Autowired
	private Environment environment;

	private static final Logger log = LoggerFactory.getLogger(LangDefine.class);

	private ConcurrentHashMap<String, Object> langMap = new ConcurrentHashMap<>();
	
	public final static String DEFAULT_LANG = "zh";
	
	public final static String FORMAT_DATE = "dateFormat";
	public final static String FORMAT_TIME = "timeFormat";
	public final static String FORMAT_DATETIME = "datetimeFormat";

	public String getLang(String key) {
		return getLang(null, key);
	}
	
	public String getLang(String lang, String key) {
		Object value = getLangMap(lang,key);
		return value==null?null:value.toString();
	}	

	public String getLangDef(String key, String def) {
		return getLangDef(null,key,def);
	}

	public String getLangDef(String lang, String key, String def) {
		String val = getLang(lang, key);
		return StringUtils.hasText(val)?val:def;
	}	
	
	public Object getSetting(String lang, String key) {
		Object val = this.getLangMap(lang, key);
		if (val == null) return null;
		return (val instanceof String)?getSetting(lang, (String) val):val;
	}
	
	private Object getLangMap(String lang, String key) {
		Object value = this.langMap.get((lang==null?DEFAULT_LANG:lang) + "|" + key);
		return value == null ? this.langMap.get(DEFAULT_LANG + "|" + key) : value;
	}
	
	public Object loadEnumData(LinkedHashMap<String, Object> subLangMap) {
		String enumViewId = (String) subLangMap.get("viewId");
		String enumValue = (String) subLangMap.get("value");
		String enumText = (String) subLangMap.get("text");
		if (enumViewId != null && enumValue != null && enumText != null) {
			EnumApiEn enumApiEn = new EnumApiEn();
			enumApiEn.setTextField(enumText);
			enumApiEn.setValueField(enumValue);
			enumApiEn.setViewId(enumViewId);
			Object enumParam = subLangMap.get("param");
			if (enumParam != null && enumParam instanceof LinkedHashMap) {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, Object> enumParamMap = (LinkedHashMap<String, Object>) enumParam;
				LinkedHashMap<String, String> param = new LinkedHashMap<String, String>();
				for (String enumParamMapkey : enumParamMap.keySet()) {
					param.put(enumParamMapkey, (String) enumParamMap.get(enumParamMapkey));
				}
				enumApiEn.setParam(param);
			}
			return enumApiEn;
		} else {
			boolean foundMap = false;
			for (String key : subLangMap.keySet()) {
				if (subLangMap.get(key) instanceof LinkedHashMap) {
					foundMap = true;
					break;
				}
			}
			if(!foundMap && subLangMap.keySet().size()>1) {
				EnumDataEn enumDataEn = new EnumDataEn();
				LinkedHashMap<String, String> data = new LinkedHashMap<>();
				for (String enumKey : subLangMap.keySet()) {
					data.put(enumKey==null?"null":enumKey, (String) subLangMap.get(enumKey));
				}
				enumDataEn.setData(data);
				return enumDataEn;
			}
			return null;
		}
	}

	public void loadLang() throws Exception {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		String settingStr = "weaver-view.lang.path";
		String defPath = "/lang";
		String searchFile = "/*.yml";
		String langPath = environment.getProperty(settingStr);
		if (langPath == null) {
			log.info(
					String.format("Config setting [%s] not found. Default path of lang is:[%s]", settingStr, defPath));
			try {
				resolver.getResources(defPath+searchFile);
			}catch(FileNotFoundException e){
				return;
			}
			langPath = defPath;
		}
		Resource[] resources = resolver.getResources(Utils.toPath(langPath) + searchFile);
		for (Resource file : resources) {
			String name = file.getFilename();
			if (!name.endsWith(".yml"))
				continue;
			String lang = name.substring(0, name.length() - 4);
			YamlRecursion yr = new YamlRecursion(file.getInputStream()){
				@Override
				public Map<String, Object> putData(LinkedHashMap<String, Object> mapData, String resultPath) {
					Map<String, Object> result = super.procee(mapData, resultPath);
					Object enumData = loadEnumData(mapData);
					if(enumData!=null) result.put(resultPath, enumData);
					return result;
				}
			};
			Map<String, Object> langMap = yr.getYmlMap();
			for (String key : langMap.keySet()) {
				if(key==null)continue;
				Object value = langMap.get(key);
				String langKey = lang + "|" + key;
				if(value instanceof String) {
					String checkIfKey = (String) value;
					for(String subKey : langMap.keySet()) {
						if(subKey==null)continue;
						Object subValue = langMap.get(subKey);
						if(subKey.startsWith(checkIfKey) && !subKey.equals(checkIfKey)) {
							String newKey = subKey.replaceFirst(checkIfKey, key);
							putLangMapValue(lang + "|" +newKey,subKey,subValue);
						}
					}
				}
				putLangMapValue(langKey,key,value);
			}
			log.debug("Loaded langage file:" + name);
		}
	}
	
	private void putLangMapValue(String langKey,String key, Object value) {
		if(key.equals(FORMAT_DATE)||key.equals(FORMAT_TIME)||key.equals(FORMAT_DATETIME)) {
			this.langMap.put(langKey, new SimpleDateFormat((String) value));
		}else {
			this.langMap.put(langKey, value);
		}
	}


}
