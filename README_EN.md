# tabby-path-finder

[中文版本](https://github.com/wh1t3p1g/tabby-path-finder/blob/master/README.md)

## #0 Introduction
A neo4j plugin for tabby

## #1 Configuration

Configure jdk17 on your machine
```bash
mvn clean package -DskipTests
```

Add the compiled jar file to Neo4j's plugin directory

## #2 Query syntax

#### help 
View all procedures

```
call tabby.help("tabby")
```

#### beta procedures
```cypher
tabby.beta.findPath(source, direct, sink, maxNodeLength, isDepthFirst) YIELD path, weight
tabby.beta.findPathWithState(source, direct, sink, sinkState, maxNodeLength, isDepthFirst) YIELD path, weight
tabby.beta.findJavaGadget(source, direct, sink, maxNodeLength, isDepthFirst) YIELD path, weight
```
- ">": Forward algorithm, starting from source to sink
- "<": Backward algorithm, starting from sink to source

#### Generic syntax

```
match (source:Method {NAME:"readObject",IS_SERIALIZABLE:true})
match (sink:Method {IS_SINK:true})
call tabby.beta.findJavaGadget(source, "<", sink, 6, true) yield path where none(n in nodes(path) where n.CLASSNAME in ["java.io.ObjectInputStream","Add the class you want to remove"])
return path limit 1
```

See neo4j cypher syntax for more information on usage

