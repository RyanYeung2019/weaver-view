```shell

	ViewStatement statement = viewQuery
			.prepareSql("select * from view_demo.department where member_count = :memberCount");
	statement.setPageNum(0);
	statement.setPageSize(15);
	statement.putParam("memberCount", 2);
	ViewData<DepartmentEntity> viewData = statement
			.query(new BeanPropRowMapper<DepartmentEntity>(DepartmentEntity.class));
	// 获取视图名称
	String name = viewData.getName(); 
	// 获取视图描述
	String desc = viewData.getDesc(); 
	// 视图数据
	List<DepartmentEntity> data = viewData.getData(); 
	// 视图相关的引用数据，枚举数据
	Map<String, Map<String, String>> valueMapping = viewData.getValueMapping();
	// 相关消息
	String message = viewData.getMessage();
	// 执行视图查询的开始时间
	Date startTime = viewData.getStartTime();
	// 执行视图查询的结束时间
	Date endTime = viewData.getEndTime();
	// 视图中字段相关信息
	List<FieldInfo> fieldInfos = viewData.getFields();
	// 视图中有关统计数据
	Map<String, Object> aggrs = viewData.getAggrs();
	// 带出视图相关配置属性
	JSONObject props = viewData.getProps();
```

  * 使用例子:  weaver-view-demo
  

## 视图

  根据SQL语句产生对应的视图数据，视图相关的信息。

### 创建视图

  直接使用SQL查询语句,引入viewQuery，并且使用prepareSql执行SQL语句
  
```java
	@Autowired
	private ViewQuery viewQuery;

	public void baseUsage() throws Exception {
		ViewStatement statement = viewQuery
				.prepareSql("select * from view_demo.department where member_count = :memberCount");
		//设置第几页
		statement.setPageNum(0);
		//设置每页多少行
		statement.setPageSize(15);
		//传入查询参数memberCount
		statement.putParam("memberCount", 2);
		//执行查询返回视图数据
		ViewData<DepartmentEntity> viewData = statement
				.query(new BeanPropRowMapper<DepartmentEntity>(DepartmentEntity.class));
	}
```

  通过引用*.sql文件执行查询。 
  
  SQL文件： \src\main\resources\view\department.sql

```sql
select * from view_demo.department
```

  weaver-view启动时默认扫描 \src\main\resources\view\ 目录下的所有sql文件。将文件名解析为viewId。上面例子中的viewId为“department”，在java中可以通过viewId进行查询操作。

```java
	@Autowired
	private ViewQuery viewQuery;

	public void baseUsage() throws Exception {
		ViewStatement statement = viewQuery.prepareView("department");
	}
```

  如果文件位于\src\main\resources\view\org\department.sql ViewId会解析为“org.department”。

  若要更改扫描路径可以通过 application.yml更改配置值weaver-view.lang.path。
  
```yaml
weaver-view:
  lang:
    path: \src\main\resources\view
```


### 取得视图字段信息

  执行查询后可以通过 viewData.getFields() 获取视图的字段信息。
  
  * 什么情况下可以获得字段信息？
  
  当statement.setPageNum(0)或者statement.setPageNum(1)的情况下，会返回字段信息。对statement传入pageNum为0,或者1。都认为是第一页。获取第一页的情况下会自动附加上字段信息。其他情况下可以单独执行一次不带任何参数的查询请求以获得字段信息
  
  以下例子返回该视图有9个字段，字段中可以获取的信息。并且返回了一些相关的视图信息。
  
```java
		ViewStatement statement = viewQuery.prepareView("department");
		ViewData<Map<String, Object>> viewData = statement.query();
		List<FieldInfo> fields = viewData.getFields();
		assertEquals(fields.size(),9);
		ViewStatement statement = viewQuery.prepareView("department");
		ViewData<Map<String, Object>> viewData = statement.query();
		List<FieldInfo> fields = viewData.getFields();
		assertEquals(fields.size(),9);
		for(FieldInfo fieldInfo:fields) {
			//字段变量名（通常为驼峰格式）
			String field = fieldInfo.getField();
			//显示给用户的字段名称，可以通过多语言字典配置其中内容
			String name = fieldInfo.getName();
			//显示给用户的字段描述内容，可以通过多语言字典配置其中内容
			String desc = fieldInfo.getDesc();
			//字段类型（已转换为 typeScript 类型）
			String type = fieldInfo.getType();
			//字段精度
			Integer preci = fieldInfo.getPreci();
			//字段标度
			Integer scale = fieldInfo.getScale();
			//字段是否允许空值
			Boolean nullable = fieldInfo.getNullable();
			//如果该字段有引用其他视图，这里会提供获取数据的api信息
			EnumApiEn enumApi = fieldInfo.getEnumApi();
			//如果该字段有设置为枚举，会根据当前的语言设置带出枚举数据内容
			List<EnumItemEn> enumDataList = fieldInfo.getEnumDataList();
			//保存有该字段相关的其他属性
			JSONObject props = fieldInfo.getProps();
		}
		//显示给用户的视图名称，可以通过多语言字典配置其中内容
		String name = viewData.getName();
		//显示给用户的视图描述，可以通过多语言字典配置其中内容
		String desc = viewData.getDesc();
		//视图相关的其他属性
		JSONObject props = viewData.getProps();
```

### 取得视图数据

  执行查询后可以通过 viewData.getData() 获取视图数据。
  
  * 什么情况下可以获得视图数据？
  
  当statement.setPageNum和statement.setPageSize的情况下，statement得到分页信息后，执行查询动作statement.query并返回数据。默认查出数据保存为List<Map<String,Object>>格式,亦可以使用BeanPropRowMapper将数据匹配到Entity中去
  
```java
		ViewStatement statement = viewQuery.prepareView("department");
		statement.setPageNum(1);
		statement.setPageSize(10);
		//默认查出数据保存为List<Map<String,Object>>格式
		ViewData<Map<String, Object>> viewData = statement.query();
		List<Map<String, Object>> data = viewData.getData();
		assertEquals(data.size(),10);
		//亦可以使用BeanPropRowMapper将数据匹配到Entity中去
		ViewData<DepartmentEntity> viewDataBean = statement.query(new BeanPropRowMapper<DepartmentEntity>(DepartmentEntity.class));
		List<DepartmentEntity> dataBean = viewDataBean.getData();
		assertEquals(dataBean.size(),10);
```

### 取得统计数据

  执行查询后可以通过 viewData.getAggrs() 获取视图的统计数据。
  
  * 什么情况下可以获得统计数据？
  
  默认情况下不会执行统计查询，当statement.setAggrList不为null，被赋值一个size为0的ArrayList。会执行一个默认的统计查询，统计当前可查询到的总记录数（注意总记录数不同于分页后返回的记录数）。当statement.setAggrList中传字段及其统计方法后（字段名-统计方法），统计查询会同时返回相应的统计结果。
  
```java
		ViewStatement statement = viewQuery.prepareView("department");
		//分页设置
		statement.setPageNum(1);
		statement.setPageSize(10);
		//默认带出总记录数据
		statement.setAggrList(new ArrayList<String>());
		ViewData<Map<String, Object>> viewData = statement.query();
		//获取总记录数
		Long size = (Long) viewData.getAggrs().get(ViewData.AGGRS_SIZE);
		assertEquals(size,15l);
		//当前分页的记录数
		List<Map<String, Object>> data = viewData.getData();
		assertEquals(data.size(),10);
		
		//统计其他字段
		List<String> aggrList = new ArrayList<>();
		aggrList.add("memberCount-avg");
		aggrList.add("memberCount-sum");
		statement.setAggrList(aggrList);
		viewData = statement.query();
		//获取总记录数。
		size = (Long) viewData.getAggrs().get(ViewData.AGGRS_SIZE);
		assertEquals(size,15l);
		//获取统计其他字段的数据
		java.math.BigDecimal avg = (java.math.BigDecimal) viewData.getAggrs().get("memberCountAvg");
		assertEquals(avg.toString(),"4.0000");
		java.math.BigDecimal sum = (java.math.BigDecimal) viewData.getAggrs().get("memberCountSum");
		assertEquals(sum.toString(),"60");
```

### 使用查询条件



### 视图的设置
  
#### 基本设置
  
#### 字段设置
    
##### 枚举
    
##### 数据引用
    
##### 其他
   

## 树

### 树的设置


### 树的查询
 

## 多语言支持
 
### 创建字典
 
### 参数化翻译
 
#### 动态获取枚举内容
   
#### 动态引用表数据内容
   
### 翻译的使用
 
#### 在视图返回数据中应用翻译
   
#### 在文本中使用翻译
   
   
   
   
 
