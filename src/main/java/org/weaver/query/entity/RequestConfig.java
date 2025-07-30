package org.weaver.query.entity;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.weaver.config.LangDefine;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class RequestConfig {

	private String language = LangDefine.DEFAULT_LANG;
	
	private boolean translate = true;

	private Map<String, Object> params = new HashMap<>();
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		if(language!=null) {
			this.language = language;
		}
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public boolean isTranslate() {
		return translate;
	}

	public void setTranslate(Boolean translate) {
		if(translate!=null) {
			this.translate = translate;
		}
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public SimpleDateFormat getDatetimeFormat() {
		return datetimeFormat;
	}

	public void setDatetimeFormat(SimpleDateFormat datetimeFormat) {
		this.datetimeFormat = datetimeFormat;
	}

	public SimpleDateFormat getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(SimpleDateFormat timeFormat) {
		this.timeFormat = timeFormat;
	}


	
	

}
