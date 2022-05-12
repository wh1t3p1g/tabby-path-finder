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
目前，tabby-path-finder支持2种方式的路径查找
1. 正向查找，from source to sink
2. 逆向查找，from sink to source

正向查找
```
tabby.algo.findVul(source, sinks, maxDepth, depthFirst) yield path
tabby.algo.findJavaGadget(source, sinks, maxDepth, depthFirst) yield path
```

其中findJavaGadget会根据java原生反序列化的规则来查找利用链，但是会有一些bug。推荐用完该函数之后再用findVul排除一下。

逆向查找
```
tabby.algo.allSimplePaths(sink, sources, maxDepth, state, depthFirst) yield path
```

通用的语法，更多的用法参考neo4j cypher语法
```
match (source:Method {NAME:"readObject"}) // 限定source
match (sink:Method {IS_SINK:true, NAME:"invoke"}) // 限定sink
with source, collect(sink) as sinks // 聚合sink
call tabby.algo.findJavaGadget(source, sinks, 8, false) yield path where none(n in nodes(path) where n.CLASSNAME in ["java.io.ObjectInputStream","org.apache.commons.beanutils.BeanMap","org.apache.commons.collections4.functors.PrototypeFactory$PrototypeCloneFactory"])
return path limit 1
```

Note: 由于neo4j底层并不支持多线程，当前所有接口都剔除了多线程的参数配置

Note: 关于效果的说明：
    
1. 速度上：相比较直接查询路径连通性的算法，加了污点分析的算法会增加几次数据库查询，但污点分析增加了动态剪枝，减少了路径遍历的次数。目前，暂时没有进行速度上的分析，无法确定是增加耗时还是减少查询时间。
2. 检出率：由于加了污点分析，出来的结果都是tabby污点分析算法所认为可连续数据传递的链路。简单测试了一个小项目，从2k+链路能减少到10+链路。误报极大的降低，但同时，由于算法分析的不准确性，使得相应的增加了漏报率

Note: tricks:

1. 如果需要大而全的链路输出，选择apoc.algo.allSimplePaths。但相应存在大量的误报链路
2. 如果使用了污点分析扩展，建议先看污点分析出来的链路，后看对应sink的调用节点是否都准确
3. 类似tabby、codeql等静态分析工具，不是万金油，能用工具直接检测出来的漏洞是非常少的（ps:捡漏还是可以的 XD），但这些工具能给你带来审计上效率的提升，明确思路等

## #3 案例

见cyphers目录

目前，查询结果基于tabby 2.0，暂未测试tabby 1.x