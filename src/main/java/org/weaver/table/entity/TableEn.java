package org.weaver.table.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class TableEn implements Serializable {

	private static final long serialVersionUID = 3265099463258551490L;

	private String tableId;
    private String tableName;
    private String tableDesc;
    private Integer sortField;
    private List<FieldEn> fieldEns;
    private Map<String,FieldEn> fieldEnMap = new HashMap<>();
    private List<PrimaryKeyEn> primaryKeyEns;
    private List<ForeignKeyEn> foreignKeyEns;
    private List<IndexEn> indexEns;
    private String remark;
	private String sourceType;// 数据源类型
	private String dataSource;// 使用的数据源

    public TableEn(String tableId) {
		super();
		this.tableId = tableId;
	}

	public String getTableId() {
		return tableId;
	}
	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableDesc() {
		return tableDesc;
	}

	public void setTableDesc(String tableDesc) {
		this.tableDesc = tableDesc;
	}

	public Integer getSortField() {
		return sortField;
	}

	public void setSortField(Integer sortField) {
		this.sortField = sortField;
	}

	public List<FieldEn> getFieldEns() {
		return fieldEns;
	}

	public void setFieldEns(List<FieldEn> fieldEns) {
		this.fieldEns = fieldEns;
	}


	public Map<String, FieldEn> getFieldEnMap() {
		return fieldEnMap;
	}

	public void setFieldEnMap(Map<String, FieldEn> fieldEnMap) {
		this.fieldEnMap = fieldEnMap;
	}

	public List<PrimaryKeyEn> getPrimaryKeyEns() {
		return primaryKeyEns;
	}

	public void setPrimaryKeyEns(List<PrimaryKeyEn> primaryKeyEns) {
		this.primaryKeyEns = primaryKeyEns;
	}

	public List<ForeignKeyEn> getForeignKeyEns() {
		return foreignKeyEns;
	}

	public void setForeignKeyEns(List<ForeignKeyEn> foreignKeyEns) {
		this.foreignKeyEns = foreignKeyEns;
	}

	public List<IndexEn> getIndexEns() {
		return indexEns;
	}

	public void setIndexEns(List<IndexEn> indexEns) {
		this.indexEns = indexEns;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public String toString() {
		return "TableEn [tableId=" + tableId + ", tableName=" + tableName + ", tableDesc=" + tableDesc + ", sortField="
				+ sortField + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		TableEn other = (TableEn) obj;
		if (tableId == null) {
			if (other.tableId != null)
				return false;
		} else if (!tableId.equals(other.tableId))
			return false;
		return true;
	}




}
