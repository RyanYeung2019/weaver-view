package org.weaver.view.query;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.weaver.view.table.entity.TableEn;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

public class CacheUtils {
	static final Map<String, TableEn> cacheTableMap = new ConcurrentHashMap<>();
}
