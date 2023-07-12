# tabby-path-finder

[English Version](https://github.com/wh1t3p1g/tabby-path-finder/blob/master/README_EN.md)

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
```

## #2 语法

#### help 
查看所有 procedure

```
call tabby.help("tabby")
```

#### findPath、findAllPaths

```cypher
tabby.algo.findPath(startNode, endNodes, maxNodeLength, isBackward, isDepthFirst) YIELD path, weight
tabby.algo.findPathWithState(startNode, endNodes, maxNodeLength, state, isDepthFirst) YIELD path, weight
tabby.algo.findAllPaths(startNodes, endNodes, maxNodeLength, isBackward, isDepthFirst) YIELD path, weight
```
findPath 系列可指定前后向分析算法`isBackward`，也可指定路径检索算法（DFS、BFS）`isDepthFirst`

另外，findPathWithState 默认为后向分析算法，`state`参数可用于指定sink函数的污点信息，类似`[0]`

#### findJavaGadget、findAllJavaGadget

```cypher
tabby.algo.findJavaGadget(source, sinks, maxNodeLength, isBackward, depthFirst) YIELD path, weight
tabby.algo.findAllJavaGadget(sources, sinks, maxNodeLength, isBackward, depthFirst) YIELD path, weight
```
findJavaGadget 系列主要用于查找 Java 原生反序列化利用链

#### beta procedures
```cypher
tabby.beta.findPath(source, direct, sink, maxNodeLength, isDepthFirst) YIELD path, weight
tabby.beta.findPathWithState(source, direct, sink, sinkState, maxNodeLength, isDepthFirst) YIELD path, weight
tabby.beta.findJavaGadget(source, direct, sink, maxNodeLength, isDepthFirst) YIELD path, weight
tabby.beta.findJavaGadgetWithState(source, direct, sink, sinkState, maxNodeLength, isDepthFirst) YIELD path, weight
```
为了能更好地利用内存 cache，不采用上述集合查询的方式，经测试比较，比采用集合的方式提效至少5倍
但当前 procedure 仍在 beta 阶段，欢迎测试使用！

上述的几个 procedure，source 和 sink 节点的位置是固定的，不需要根据检索方向来调整位置。

findPath 系列 direct 有3种：
- ">": 前向算法，从 source 开始查找至 sink
- "<": 后向算法，从 sink 开始查找至 source
- "-": 双向算法，分别从 source 和 sink 开始查找，找到聚合点后输出

findJavaGadget 系列 direct 只支持前向和后向算法

其他参数同之前的用法一致，不再赘述。

#### 通用语法

通用的语法，更多的用法参考neo4j cypher语法
```
match (source:Method {NAME:"readObject"}) // 限定source
match (sink:Method {IS_SINK:true, NAME:"invoke"}) // 限定sink
with source, collect(sink) as sinks // 聚合sink
call tabby.algo.findJavaGadget(source, sinks, 8, false, false) yield path where none(n in nodes(path) where n.CLASSNAME in ["java.io.ObjectInputStream","org.apache.commons.beanutils.BeanMap","org.apache.commons.collections4.functors.PrototypeFactory$PrototypeCloneFactory"])
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