package org.weaver.view.table.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ForeignKeyEn implements Serializable {

	private static final long serialVersionUID = -4532041795300261592L;

	private String fkKey;
	private List<ForeignRefFieldEn> foreignRefFieldEns = new ArrayList<>();

	public ForeignKeyEn(String fkKey) {
		super();
		this.fkKey = fkKey;
	}
	public String getFkKey() {
		return fkKey;
	}
	public void setFkKey(String fkKey) {
		this.fkKey = fkKey;
	}

	public List<ForeignRefFieldEn> getForeignRefFieldEns() {
		return foreignRefFieldEns;
	}
	public void setForeignRefFieldEns(List<ForeignRefFieldEn> foreignRefFieldEns) {
		this.foreignRefFieldEns = foreignRefFieldEns;
	}

	public String getRefTable() {
		if(foreignRefFieldEns.size()>0){
			return foreignRefFieldEns.get(0).getRefTable();
		}
		return "";
	}

	public List<String> getFkFieldStr() {
		List<String> rtv = new ArrayList<>();
		for(ForeignRefFieldEn foreignRefFieldEn:foreignRefFieldEns){
			rtv.add(foreignRefFieldEn.getField());
		}
		return rtv;
	}

	public List<String> getRefFieldStr() {
		List<String> rtv = new ArrayList<>();
		for(ForeignRefFieldEn foreignRefFieldEn:foreignRefFieldEns){
			rtv.add(foreignRefFieldEn.getRefField());
		}
		return rtv;
	}


	@Override
	public String toString() {
		return "ForeignKeyEn [fkKey=" + fkKey + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fkKey == null) ? 0 : fkKey.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		ForeignKeyEn other = (ForeignKeyEn) obj;
		if (fkKey == null) {
			if (other.fkKey != null)
				return false;
		} else if (!fkKey.equals(other.fkKey))
			return false;
		return true;
	}



}
