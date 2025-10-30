package org.weaver.table.entity;

public class DatabaseType {
	
	private String type;
	
	private int majorVersion;
	
	private int minorVersion;
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public int getMajorVersion() {
		return majorVersion;
	}
	
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}
	
	public int getMinorVersion() {
		return minorVersion;
	}
	
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}
	
}
