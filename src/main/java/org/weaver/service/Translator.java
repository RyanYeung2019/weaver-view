package org.weaver.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSONObject;

import org.weaver.config.LangDefine;
import org.weaver.config.ViewDefine;
import org.weaver.config.entity.EnumApiEn;
import org.weaver.config.entity.EnumDataEn;
import org.weaver.config.entity.ViewEn;
import org.weaver.query.entity.QueryCriteria;
import org.weaver.query.entity.QueryFilter;
import org.weaver.query.entity.RequestConfig;
import org.weaver.query.mapper.CamelFieldMapper;
import org.weaver.view.util.FormatterUtils;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class Translator {
	private static final Logger log = LoggerFactory.getLogger(Translator.class);

	private ViewDao queryDao;
	private LangDefine langDefine;
	private ViewDefine viewDefine;

	private StringBuilder tranFieldsInput = new StringBuilder();
	private StringBuilder tranFieldsOutput = new StringBuilder();
	private StringBuilder newTemplate = new StringBuilder();
	private List<Object> valueList = new ArrayList<>();
	private Pattern patternText = Pattern.compile("\\{\\{\\s*(.*?)\\s*}}");
	private Pattern patternJson = Pattern.compile("\\{\\s*(.*?)\\s*}");
	private Pattern patternNamed = Pattern.compile("[$][{](\\w+)}");
	private Map<String, String> tranMapView = new HashMap<>();
	private Map<String, Object> tranParamMap = new HashMap<>();
	private RequestConfig viewReqConfig;

	public Translator(ViewDao queryDao, LangDefine langDefine, ViewDefine viewDefine,
			RequestConfig viewReqConfig) {
		super();
		this.queryDao = queryDao;
		this.langDefine = langDefine;
		this.viewDefine = viewDefine;
		this.viewReqConfig = viewReqConfig;
	}

	public Translator(ViewDao queryDao, LangDefine langDefine, ViewDefine viewDefine, 
			RequestConfig viewReqConfig,Map<String, Object> tranParamMap) {
		super();
		this.queryDao = queryDao;
		this.langDefine = langDefine;
		this.viewDefine = viewDefine;
		if(tranParamMap!=null)this.tranParamMap = tranParamMap ;
		this.viewReqConfig = viewReqConfig;
	}

	public static String showEnumValue(String value, String tranStr) {
		JSONObject json = new JSONObject();
		json.put("value", value);
		return String.format(tranStr, json.toString());
	}

	public static String showFileValue(String value) {
		JSONObject json = new JSONObject();
		json.put("fileId", value);
		return String.format("{{show.file %s }} ", json.toString());
	}

	public static String showFileGroupValue(String[] values) {
		StringBuffer buff = new StringBuffer();
		for (String value : values) {
			buff.append(showFileValue(value));
		}
		return buff.toString();
	}

	public static String showImageValue(String value) {
		JSONObject json = new JSONObject();
		json.put("imageId", value);
		return String.format("{{show.image %s }} ", json.toString());
	}

	public static String showImageGroupValue(String[] values) {
		StringBuffer buff = new StringBuffer();
		for (String value : values) {
			buff.append(showImageValue(value));
		}
		return buff.toString();
	}

	public String tranText(String original) {
		if(!viewReqConfig.isTranslate()) return original;
		if (!original.contains("{{"))
			return original;
		if (!original.contains("}}"))
			return original;
		int start = 0;
		tranFieldsInput.setLength(0);
		String searchString = "}}}";

		int end = original.indexOf(searchString, start);
		if (end == -1) {
			tranFieldsInput.append(original);
		} else {
			int replLength = searchString.length();
			while (end != -1) {
				tranFieldsInput.append(original.substring(start, end)).append("} }}");
				start = end + replLength;
				end = original.indexOf(searchString, start);
			}
			tranFieldsInput.append(original.substring(start));
		}
		tranFieldsOutput.setLength(0);
		int lastIndex = 0;
		Matcher matcher = patternText.matcher(tranFieldsInput);
		while (matcher.find()) {
			String key = matcher.group(1);
			Map<String,Object> tranParam = null;
			Matcher matcherJson = patternJson.matcher(key);
			if (matcherJson.find()) {
				String json = matcherJson.group(0);
				key = key.replace(json, "").trim();
				json = json.replace("\\\"", "\"");
				tranParam = JSONObject.parseObject(json);
			}
			String val = tranKey(key,tranParam);
			tranFieldsOutput.append(tranFieldsInput, lastIndex, matcher.start()).append(val);
			lastIndex = matcher.end();
		}
		if (lastIndex < tranFieldsInput.length()) {
			tranFieldsOutput.append(tranFieldsInput, lastIndex, tranFieldsInput.length());
		}
		return tranFieldsOutput.toString();
	}
	
	public String tranKey(String key,Map<String,Object> tranParam) {
		String val = getEnumVal(key, tranParam).replace("\"", "\\\"");
		val = val == null ? "" : val;
		valueList.clear();
		this.newTemplate.replace(0, newTemplate.length(), val);
		Matcher matcherNamed = patternNamed.matcher(val);
		while (matcherNamed.find()) {
			String keyNamed = matcherNamed.group(1);
			String paramName = "${" + keyNamed + "}";
			int index = newTemplate.indexOf(paramName);
			if (index != -1) {
				newTemplate.replace(index, index + paramName.length(), "%s");
				Object value = this.tranParamMap.get(keyNamed);
				if (value != null) {
					valueList.add(value);
				} else {
					valueList.add(tranParam.get(keyNamed));
				}
			}
		}
		return String.format(newTemplate.toString(), valueList.toArray());
	}
	
	private String getEnumVal(String key, Map<String,Object> tranParam) {
		String lang = this.viewReqConfig.getLanguage();
		if(tranParam==null) {
			return this.langDefine.getLang(lang, key);
		}
		String vkey = (String) tranParam.get("value");
		if(!StringUtils.hasText(vkey)) {
			return this.langDefine.getLang(lang, key);
		}
		Object setting = this.langDefine.getSetting(lang, key);
		if (setting == null) return "";
		String val = "";
		if (setting instanceof EnumApiEn) {
			EnumApiEn enumApiEn = (EnumApiEn) setting;
			String viewId = enumApiEn.getViewId();
			// cached
			val = this.tranMapView.get(key + "_" + vkey);
			if (val == null) {
				val = getViewValue(key, vkey, viewId, enumApiEn, tranParam);
				this.tranMapView.put(key + "_" + vkey, val == null ? "" : val);
			}
		}
		if (setting instanceof EnumDataEn) {
			EnumDataEn enumDataEn = (EnumDataEn) setting;
			val = enumDataEn.getData().get(vkey);
		}
		return val;
	}

	private String getViewValue(String langKeyStr, String vkey, String viewId, EnumApiEn view,  Map<String,Object> tranParam) {
		String mkey = view.getValueField();
		String mVal = view.getTextField();
		if (vkey == null) {
			log.warn("lang[" + langKeyStr + "]: Cannot Get 'value' Param From Json!");
			return "";
		}
		QueryFilter queryFilter = new QueryFilter(new QueryCriteria(mkey, vkey));
		Map<String, Object> param = new HashMap<>();
		for (String key : tranParam.keySet()) {
			if(key.equals("value"))continue;
			String value = (String)tranParam.get(key);
			param.put(key, value);
		}
		param.putAll(this.tranParamMap);
		ViewEn viewEn = this.viewDefine.getView(viewId);
		if (viewEn == null)
			throw new RuntimeException(String.format("view %s is not exits! ", viewId));
		log.debug("getViewValue:"+viewEn.getViewId());
		FilterCriteria filter = SqlUtils.paramFilter(queryFilter, viewEn.getListFields(), viewEn.getSourceType(),viewReqConfig);
		CamelFieldMapper camelFieldMapper = new CamelFieldMapper();
		camelFieldMapper.setFieldList(viewEn.getListFields());
		Translator translator = new Translator(this.queryDao, this.langDefine, this.viewDefine, this.viewReqConfig, this.tranParamMap);
		camelFieldMapper.setTranslator(translator);
		camelFieldMapper.setFieldMap(viewEn.getFieldMap());

		List<Map<String, Object>> enumApiData = this.queryDao.queryData(viewEn, param, null, 0, 1, filter, camelFieldMapper);
		if (enumApiData.size() > 0) {
			Object v = enumApiData.get(0).get(mVal);
			return FormatterUtils.objectToString(v);
		}
		return "";

	}

}
