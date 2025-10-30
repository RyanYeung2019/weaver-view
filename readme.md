# weaver-view

  快速将SQL查询语句转换为API，为前端提丰富信息。
  
  提供数据库级别的翻页，排序，搜索；简单配置即可实现对其他数据的引用，枚举，及多语言的支持。
  
  支持树遍历模式，将SQL查询语句转为树结构返回。

## 开始

# 参照项目 weaver-view-demo

  * 引入项目
  
```xml
  <dependency>
    <groupId>io.github.ryanyeung2019</groupId>
    <artifactId>weaver-view</artifactId>
    <version>2.1.22</version>
  </dependency>
```

  * 配置视图查询
  \src\main\resources\view\demo\query.sql
  
```sql
select * from view_demo.department where domain_key = :domainKey
```

  * 执行查询
  
  支持多语言，分页，排序，统计，搜索等
  
```shell  
curl "http://localhost:8081/view/demo/query?domainKey=domain1&page=1&size=5&sort=domainKey-d,depKey-d&aggrs="
```

  返回内容：
  
```json
{
    "data": [
        {
            "domainKey": "domain1",
            "depKey": "dep15",
            "depName": "后端开发小组",
            "memberCount": 3,
            "stopped": false,
            "createTime": "2025-04-15T08:53:04.000+00:00",
            "createUser": "admin",
            "updateTime": "2025-04-15T08:53:04.000+00:00",
            "updateUser": "admin"
        },        
        :
        {
            "domainKey": "domain1",
            "depKey": "dep11",
            "depName": "会议安排小组",
            "memberCount": 2,
            "stopped": false,
            "createTime": "2025-04-15T08:53:04.000+00:00",
            "createUser": "admin",
            "updateTime": "2025-04-15T08:53:04.000+00:00",
            "updateUser": "admin"
        }
    ],
    "aggrs": { "size": 15 },        <--返回统计数据，size是当次查询的总记录数（不是分页后的记录数）
    "name": "部门",                  <--视图名称，可以从配置翻译字典中获取，也可以从配置视图中获取
    "desc": "保存公司部门相关资料",      <--视图描述，支持多语言翻译
    "valueMapping": {               <--若返回数据中有枚举类型，valueMapping中会带出相应的枚举值，让前端匹配替换显示内容；此处也可以引用其他视图匹配显示。
        "stopped": {                <--有枚举数据的字段，也可以是其他视图的数据
            "null": " ",  
            "false": "有效"          <--枚举值可以通过配置翻译字典获取，支持多语言显示
        } 
    },  
    "startTime": "2025-04-08T09:24:12.994+00:00", <-- api开始执行时间
    "endTime": "2025-04-08T09:24:13.155+00:00",   <-- api结束执行时间
    "fields": [                   <-- 视图字段信息
        {
            "field": "domainKey", <--字段代码
            "name": "域名",        <--字段名支持多语言显示可以通过 配置翻译字典 获取
            "desc": "划分区域",     <--字段描述支持多语言显示可以通过 配置翻译字典 获取
            "type": "string",     <--从数据库中获取字段类型并转换为typescript类型
            "preci": 100,         <--数据库返回的精度值，字段长度
            "scale": 0,           <--数据库定义该字段的范围值，小数位长度
            "nullable": false     <--数据库对该字段非空值定义
        },
        :
        {
            "field": "stopped",
            "name": "停止",
            "desc": "stopped",
            "type": "boolean",
            "preci": 1,
            "scale": 0,
            "nullable": true,
            "enumDataList": [     <-- 字段有枚举设置的情况下，在这里会带出所有枚举选项。支持多语言设置。供前端制作选择项。
                { "value": "null", "text": " " },
                { "value": "false", "text": "有效" },
                { "value": "true", "text": "失效" }
            ]
        },
        :
    ]
}
```
