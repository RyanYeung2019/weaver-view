package org.weaver.view.query.entity;

import java.util.List;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class TreeData<T> {
	
	private T node;
	
	private List<TreeData<T>> children;
	
	public T getNode() {
		return node;
	}
	public void setNode(T node) {
		this.node = node;
	}
	public List<TreeData<T>> getChildren() {
		return children;
	}
	public void setChildren(List<TreeData<T>> children) {
		this.children = children;
	}
	
}
