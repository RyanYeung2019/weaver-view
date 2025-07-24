package org.weaver.config.entity;

import java.util.LinkedHashMap;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class EnumApiEn {

	String viewId;
	LinkedHashMap<String, String> param;
	String valueField;
	String textField;

	public String getViewId() {
		return viewId;
	}

	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	public LinkedHashMap<String, String> getParam() {
		return param;
	}

	public void setParam(LinkedHashMap<String, String> param) {
		this.param = param;
	}

	public String getValueField() {
		return valueField;
	}

	public void setValueField(String valueField) {
		this.valueField = valueField;
	}

	public String getTextField() {
		return textField;
	}

	public void setTextField(String textField) {
		this.textField = textField;
	}

	@Override
	public String toString() {
		return "EnumApiEn [viewId=" + viewId + ", param=" + param + ", valueField=" + valueField + ", textField="
				+ textField + "]";
	}

}
