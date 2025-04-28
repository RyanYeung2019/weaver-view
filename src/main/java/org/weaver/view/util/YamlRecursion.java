package org.weaver.view.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class YamlRecursion {

	private Map<String, Object> ymlMap;
	
	public YamlRecursion(InputStream resource) throws Exception {
		super();
		Yaml yaml = new Yaml();
		String ymlText = Utils.readTextFile(resource);		
		ymlMap = procee(yaml.load(ymlText), null);
	}
	
	public Map<String, Object> getYmlMap() {
		return ymlMap;
	}

	protected Map<String, Object> procee(Map<String, Object> ymlMap, String path) {
		Map<String, Object> result = new HashMap<>();
		for (String key : ymlMap.keySet()) {
			String resultPath = (path == null ? "" : (path + ".")) + key;
			Object value = ymlMap.get(key);
			if (value instanceof LinkedHashMap) {
				@SuppressWarnings("unchecked")
				LinkedHashMap<String, Object> mapData = (LinkedHashMap<String, Object>) value;
				result.putAll(putData(mapData,resultPath));
			} else {
				result.put(resultPath, value);
			}
		}
		return result;
	}
	
	public Map<String,Object> putData(LinkedHashMap<String, Object> mapData, String resultPath){
		Map<String, Object> result = new HashMap<>();
		result.putAll(procee(mapData, resultPath));
		return result;
	}
	
}
