package org.weaver.config.entity;

import java.util.LinkedHashMap;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class EnumDataEn {
	LinkedHashMap<String, String> data;

	public LinkedHashMap<String, String> getData() {
		return data;
	}

	public void setData(LinkedHashMap<String, String> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "EnumDataEn [data=" + data + "]";
	}

}
