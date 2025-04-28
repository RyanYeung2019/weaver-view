package org.weaver.config.entity;

import java.util.LinkedHashMap;

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
