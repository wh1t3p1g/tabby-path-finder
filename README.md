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

#### released procedures

```cypher
tabby.algo.findPath(source, direct, sink, maxNodeLength, isDepthFirst) YIELD path, weight
tabby.algo.findPathWithState(source, direct, sink, sinkState, maxNodeLength, isDepthFirst) YIELD path, weight
tabby.algo.findJavaGadget(source, direct, sink, maxNodeLength, isDepthFirst) YIELD path, weight
tabby.algo.findJavaGadgetWithState(source, direct, sink, sinkState, maxNodeLength, isDepthFirst) YIELD path, weight
```

findPath 系列用于应用 tabby 生成的带污点的代码属性图，在遍历过程中不断剪枝，最终输出 n 条符合污点传播的路径。

findJavaGadget 系列用于查找 Java 原生反序列化利用链，在污点剪枝的基础上，同时判断当前函数所属的 class 是否实现了 Serializable 接口。

另外，findPath 系列 direct 有3种：
- ">": 前向算法，从 source 开始查找至 sink
- "<": 后向算法，从 sink 开始查找至 source
- "-": 双向算法，分别从 source 和 sink 开始查找，找到聚合点后输出

findJavaGadget 系列 direct 只支持前向和后向算法

#### 通用语法

通用的语法，更多的用法参考neo4j cypher语法
```
match (source:Method {NAME:"readObject"}) // 限定source
match (sink:Method {IS_SINK:true, NAME:"invoke"}) // 限定sink
call tabby.algo.findJavaGadget(source, ">", sink, 8, false) yield path 
where none(n in nodes(path) where 
    n.CLASSNAME in [
        "java.io.ObjectInputStream",
        "org.apache.commons.beanutils.BeanMap",
        "org.apache.commons.collections4.functors.PrototypeFactory$PrototypeCloneFactory"])
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

~~见cyphers目录~~

目前，查询结果基于tabby 2.0，暂未测试tabby 1.x