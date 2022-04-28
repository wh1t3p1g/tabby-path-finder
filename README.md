# tabby-path-finder
## #0 简介
A neo4j procedure for [tabby](https://github.com/wh1t3p1g/tabby)

tabby污点分析扩展，用于根据tabby生成的代码属性图做动态剪枝+深度搜索符合条件的利用链/漏洞链路。

## #1 用法

生成jar文件
```bash
mvn clean package -DskipTests
```

在neo4j的plugin目录添加jar文件

neo4j server需要在配置中添加上以下内容
```
dbms.security.procedures.unrestricted=apoc.*,tabby.*
dbms.security.procedures.allowlist=apoc.*,gds.*,tabby.*
```

## #2 语法

help
```
call tabby.help("all")
```

根据内置的sink污点信息进行路径检索
```
tabby.algo.allSimplePaths(
        sink, sources, 
        maxNodes, parallel, depthFirst) YIELD path
```
例子
```
// templates
match (source:Method {NAME:"readObject"})
with collect(source) as sources
match (sink:Method {IS_SINK:true, NAME:"invoke"})
call tabby.algo.allSimplePaths(sink, sources, 8, false, true) yield path
return path limit 1
```

提供sink节点的污点信息进行路径检索
```
tabby.algo.allSimplePathsWithState(
                sink, sources, 
                maxNodes, state, 
                parallel, depthFirst) YIELD path
```
例子
```
// templates
match (source:Method {NAME:"readObject"})
with collect(source) as sources
match (sink:Method {NAME:"invoke"})
call tabby.algo.allSimplePathsWithState(sink, sources, 8, "[-1,0]", false, true) yield path
return path limit 1
```

Note: 由于neo4j底层并不支持多线程，所有这两个方法在多线程的情况下有时候会不太稳定，推荐设置parallel为false，牺牲点时间

## #3 案例

TODO