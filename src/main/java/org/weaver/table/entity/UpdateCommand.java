package org.weaver.table.entity;

import java.util.List;

/**
*
* @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
* 
*/

	public class UpdateCommand<T> {
	
	private String command;
	private String tableName;
	private T data;
	private List<T> datas;
	private Long assertMaxRecordAffected;
	private String[] whereFields;
	private int[] result;
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	public List<T> getDatas() {
		return datas;
	}
	public void setDatas(List<T> datas) {
		this.datas = datas;
	}
	public Long getAssertMaxRecordAffected() {
		return assertMaxRecordAffected;
	}
	public void setAssertMaxRecordAffected(Long assertMaxRecordAffected) {
		this.assertMaxRecordAffected = assertMaxRecordAffected;
	}
	public String[] getWhereFields() {
		return whereFields;
	}
	public void setWhereFields(String[] whereFields) {
		this.whereFields = whereFields;
	}
	public int[] getResult() {
		return result;
	}
	public void setResult(int[] result) {
		this.result = result;
	}
	
	
}
