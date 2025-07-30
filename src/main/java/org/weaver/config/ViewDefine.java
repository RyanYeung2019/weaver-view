package org.weaver.config;

import java.util.regex.Pattern;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import com.alibaba.fastjson.JSONObject;

import org.weaver.config.entity.EnumApiEn;
import org.weaver.config.entity.EnumDataEn;
import org.weaver.config.entity.ViewEn;
import org.weaver.config.entity.ViewField;
import org.weaver.query.entity.EnumItemEn;
import org.weaver.service.ViewDao;
import org.weaver.service.ViewQuery;
import org.weaver.view.util.Utils;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

@Component("viewDefine")
public class ViewDefine {

	private static final String TREE_PARENT = "parent";
	private static final String TREE_ID = "id";
	private static final String TREE_SEARCH = "search";
	
	private static final Logger log = LoggerFactory.getLogger(ViewQuery.class);

	@Autowired
	private ViewDao queryDao;

	@Autowired
	private LangDefine langDefine;

	@Autowired
	private Environment environment;

	private Hashtable<String, ViewEn> viewMap = new Hashtable<>();

	public ViewEn getView(String viewKey) {
		return this.viewMap.get(viewKey);
	}

	public void putView(String viewId,ViewEn viewEn) {
		this.viewMap.put(viewId, viewEn);	
	}
	
	
	@SuppressWarnings("unchecked")
	public void loadView() throws Exception {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		String settingStr = "weaver-view.view.path";
		String defPath = "/view";
		String searchFile = "**/*.sql";
		String viewPath = environment.getProperty(settingStr);
		if (viewPath == null) {
			log.info(
					String.format("Config setting [%s] not found. Default path of view is:[%s]", settingStr, defPath));
			try {
				resolver.getResources(defPath + searchFile);
			}catch(FileNotFoundException e){
				return;
			}
			viewPath = defPath;
		}
		viewPath = Utils.toPath(viewPath);
		Resource[] resources =new Resource[] {};
		try {
			resources = resolver.getResources(viewPath + searchFile);
		}catch(Exception e) {
			log.warn(e.getMessage());
		}
		Yaml yaml = new Yaml();
		List<String> nonDefinedParam = new ArrayList<>();
		for (Resource file : resources) {
			String name = file.getFilename();
			if (!name.endsWith(".sql"))
				continue;
			String fileUrlPath = file.getURL().toString();
			String viewId = fileUrlPath
					.substring(fileUrlPath.indexOf(viewPath) + viewPath.length(), fileUrlPath.length() - 4)
					.replace("/", ".");
			String fileValue = Utils.readTextFile(file.getInputStream());
			Pattern p = Pattern.compile("(?:^|\\n|\\r)\\s*\\/\\*[\\s\\S]*?\\*\\/\\s*(?:\\r|\\n|$)");
			Matcher matcher = p.matcher(fileValue);
			String remarkStr = matcher.find() ? matcher.group().trim() : null;
			if (remarkStr == null) {
				remarkStr = "";
			}
			String sql = fileValue.replace(remarkStr, "").trim();
			if (!sql.toLowerCase().startsWith("select")) {
				throw new RuntimeException("Only allow the use of select statement!");
			}
			while(sql.endsWith(";")) {
				sql = sql.substring(0,sql.length()-1).trim();
			}
			Map<String, Object> rmkData = yaml.load(remarkStr.replace("/*", "").replace("*/", ""));
			if(rmkData==null) {
				rmkData = new HashMap<>();
			}
			String title = (String) rmkData.get("name");
			String dataSource = (String) rmkData.get("dataSource");
			dataSource = dataSource==null?"dataSource":dataSource;
			String remark = (String) rmkData.get("remark");
			
			Map<String, Map<String, String>> enumDataMapFields = null;
			Map<String, List<EnumItemEn>> enumDataListFields = null;
			Map<String, String> enumDataStringFields = null;
			LinkedHashMap<String, EnumApiEn> enumApiFields = null;
			LinkedHashMap<String, Object> fields = (LinkedHashMap<String, Object>) rmkData.get("fields");
			Map<String, String> fieldSettingMap = new HashMap<>();
			if(fields!=null) {
				for (String type : fields.keySet()) {
					if (type.equals("enum")) {
						LinkedHashMap<String, Object> enumFields = (LinkedHashMap<String, Object>) fields.get(type);
						for (String fieldName : enumFields.keySet()) {
							Object valueObj = enumFields.get(fieldName);
							if (valueObj instanceof LinkedHashMap) {
								LinkedHashMap<String, Object> subLangMap = (LinkedHashMap<String, Object>) valueObj;
								Object enumData = langDefine.loadEnumData(subLangMap);
								if (enumData instanceof EnumApiEn) {
									if (enumApiFields == null)
										enumApiFields = new LinkedHashMap<>();
									enumApiFields.put(fieldName, (EnumApiEn) enumData);
								}
								if (enumData instanceof EnumDataEn) {
									if (enumDataMapFields == null)
										enumDataMapFields = new HashMap<>();
									if (enumDataListFields == null)
										enumDataListFields = new HashMap<>();
									List<EnumItemEn> enumItemList = new ArrayList<>();
									EnumDataEn enumDataEn = (EnumDataEn) enumData;
									for (String key : enumDataEn.getData().keySet()) {
										EnumItemEn enumItemEn = new EnumItemEn(key, enumDataEn.getData().get(key));
										enumItemList.add(enumItemEn);
									}
									log.warn(viewId + ".fields.enum." + fieldName
											+ " setting not support i18n. please use lang setting.");
									enumDataListFields.put(fieldName, enumItemList);
									enumDataMapFields.put(fieldName, enumDataEn.getData());
								}
							} else {
								String val = (String) valueObj;
								Object enumData = langDefine.getSetting(null,val);
								if (enumData == null)
									throw new RuntimeException("view:" + viewId + " props:" + val + " value not found.");
								if (enumData instanceof EnumApiEn) {
									if (enumApiFields == null)
										enumApiFields = new LinkedHashMap<>();
									enumApiFields.put(fieldName, (EnumApiEn) enumData);
								} else {
									if (enumDataStringFields == null)
										enumDataStringFields = new LinkedHashMap<>();
									enumDataStringFields.put(fieldName, val);
								}
							}
						}
					} else {
						Object typeFields = fields.get(type);
						if (typeFields instanceof String) {
							fieldSettingMap.put(type, ((String) typeFields) + ",");
						}
					}
				}
			}
			LinkedHashMap<String, String> paramMap = (LinkedHashMap<String, String>) rmkData.get("param");
			Map<String, Object> critParams = new HashMap<String, Object>();
			if (paramMap != null) {
				for (String key : paramMap.keySet()) {
					Object value = ViewUtils.stubObjVal(paramMap.get(key));
					critParams.put(key, value);
				}
			}
			List<String> paramList = ViewUtils.parseSqlStatement(sql);

			for (String param : paramList) {
				if (paramMap == null || paramMap.get(param) == null) {
					Object value = ViewUtils.stubObjVal(ViewField.FIELDTYPE_STRING);
					critParams.put(param, value);
					if(paramMap==null)paramMap = new LinkedHashMap<String, String>();
					paramMap.put(param, ViewField.FIELDTYPE_STRING);
					if (!nonDefinedParam.contains(param))
						nonDefinedParam.add(param);
				}
			}
			
			ViewEn viewEn = queryDao.getViewInfo(dataSource, sql, critParams);
			viewEn.setViewId(viewId);
			viewEn.setName(title);
			viewEn.setParam(paramMap);
			viewEn.setRemark(remark);
			viewEn.setSql(sql);
			
			LinkedHashMap<String, Object> meta = (LinkedHashMap<String, Object>) rmkData.get("meta");
			if (meta != null) {
				JSONObject viewMeta = viewEn.getMeta();
				for (String metaItem : meta.keySet()) {
					viewMeta.put(metaItem, meta.get(metaItem));
				}
			}
			LinkedHashMap<String, Object> props = (LinkedHashMap<String, Object>) rmkData.get("props");
			if (props != null) {
				JSONObject viewProps = viewEn.getProps();
				for (String propItem : props.keySet()) {
					viewProps.put(propItem, props.get(propItem));
				}
			}			

			Map<String, ViewField> fieldMap = new HashMap<>();
			for (ViewField viewField : viewEn.getListFields()) {
				String fieldName = viewField.getField();
				JSONObject fieldSetting = viewField.getProps();
				for (String type : fieldSettingMap.keySet()) {
					String typeFieldList = fieldSettingMap.get(type);
					if (StringUtils.hasText(typeFieldList) && (typeFieldList + ",").contains(fieldName)) {
						fieldSetting.put(type, true);
					} else {
						fieldSetting.put(type, false);
					}
				}
				if (enumApiFields != null)
					viewField.setEnumApi(enumApiFields.get(fieldName));
				if (enumDataMapFields != null)
					viewField.setEnumDataMap(enumDataMapFields.get(fieldName));
				if (enumDataListFields != null)
					viewField.setEnumDataList(enumDataListFields.get(fieldName));
				if (enumDataStringFields != null)
					viewField.setEnumDataString(enumDataStringFields.get(fieldName));
				fieldMap.put(viewField.getField(), viewField);
			}
			
			LinkedHashMap<String, Object> tree = (LinkedHashMap<String, Object>) rmkData.get("tree");
			if(tree!=null) {
				String treeId = (String) tree.get(TREE_ID);
				String treeParent = (String) tree.get(TREE_PARENT);
				String treeSearch = (String) tree.get(TREE_SEARCH);
				
				if(treeId==null)throw new RuntimeException(String.format("View %s need tree.id to support tree view!",viewId));
				if(fieldMap.get(treeId)==null)throw new RuntimeException(String.format("View %s tree.id field %s not exists!",viewId,treeId));
				
				if(treeParent==null)throw new RuntimeException(String.format("View %s need tree.parent to support tree view!",viewId));
				if(fieldMap.get(treeParent)==null)throw new RuntimeException(String.format("View %s need tree.parent field %s not exists!",viewId,treeParent));
				
				viewEn.setTreeId(treeId);
				viewEn.setTreeParent(treeParent);
				if(StringUtils.hasText(treeSearch)) {
					List<String> treeSearchList = new ArrayList<>();
					String[] treeSearchingFields = treeSearch.split(",");
					for(String field:treeSearchingFields) {
						if(fieldMap.get(field)==null)throw new RuntimeException(String.format("View %s need tree.search field %s not exists!",viewId,field));
						treeSearchList.add(field);
					}
					viewEn.setTreeSearch(treeSearchList);
				}
				
			}
			viewEn.setFieldMap(fieldMap);
			log.debug("Loaded view:" + viewId);
			this.viewMap.put(viewId, viewEn);
		}
		
		StringBuffer nonDefinedParamStr = new StringBuffer();
		if (nonDefinedParam != null && nonDefinedParam.size() > 0) {
			nonDefinedParamStr.append("Not defined param(s) in view files:\n");
			for (String item : nonDefinedParam) {
				nonDefinedParamStr.append("  " + item + "\n");
			}
			log.warn(nonDefinedParamStr.toString());
		}
	}

}
