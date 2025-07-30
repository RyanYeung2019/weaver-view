package org.weaver.table.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class IndexEn implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 355083871748230485L;

	private String idxKey;
	private List<IndexFieldEn> fieldIds = new ArrayList<>();
	private Boolean desc;
	private Boolean unique;

	private String nonUnique;
	private String ascOrDesc;


	public IndexEn(String idxKey) {
		super();
		this.idxKey = idxKey;
	}
	public String getIdxKey() {
		return idxKey;
	}
	public void setIdxKey(String idxKey) {
		this.idxKey = idxKey;
	}
	public List<IndexFieldEn> getFieldIds() {
		return fieldIds;
	}
	public void setFieldIds(List<IndexFieldEn> fieldIds) {
		this.fieldIds = fieldIds;
	}
	public Boolean getDesc() {
		return desc;
	}
	public void setDesc(Boolean desc) {
		this.desc = desc;
	}
	public Boolean getUnique() {
		return unique;
	}
	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

	public String getNonUnique() {
		return nonUnique;
	}
	public void setNonUnique(String nonUnique) {
		this.nonUnique = nonUnique;
	}
	public String getAscOrDesc() {
		return ascOrDesc;
	}
	public void setAscOrDesc(String ascOrDesc) {
		this.ascOrDesc = ascOrDesc;
	}
	public List<String> getIdxFieldStr(){
		List<String> rtv = new ArrayList<>();
		for(IndexFieldEn fieldId:fieldIds){
			rtv.add(fieldId.getField());
		}
		return rtv;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idxKey == null) ? 0 : idxKey.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		IndexEn other = (IndexEn) obj;
		if (idxKey == null) {
			if (other.idxKey != null)
				return false;
		} else if (!idxKey.equals(other.idxKey))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "IndexEn [idxKey=" + idxKey + ", fieldIds=, desc=" + desc + ", unique="
				+ unique + "]";
	}


}
