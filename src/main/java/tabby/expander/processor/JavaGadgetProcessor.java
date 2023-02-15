package tabby.expander.processor;

import lombok.Getter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import tabby.data.EdgeCache;
import tabby.data.State;
import tabby.util.Transformer;
import tabby.util.Types;

import java.util.Map;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
@Getter
public class JavaGadgetProcessor extends BaseProcessor{

    private boolean isSerializable = false;
    private boolean isAbstract = false;
    private boolean isStatic = false;
    private boolean isFromAbstractClass = false;
    private String classname = null;

    @Override
    public void init(Node node, State preState, Relationship lastRelationship){
        super.init(node, preState, lastRelationship);

        Map<String, Object> properties = node.getProperties("IS_SERIALIZABLE", "IS_ABSTRACT", "IS_STATIC", "IS_FROM_ABSTRACT_CLASS","CLASSNAME");
        this.isSerializable = (boolean) properties.getOrDefault("IS_SERIALIZABLE", false);
        this.isAbstract = (boolean) properties.getOrDefault("IS_ABSTRACT", false);
        this.isStatic = (boolean) properties.getOrDefault("IS_STATIC", false);
        this.isFromAbstractClass = (boolean) properties.getOrDefault("IS_FROM_ABSTRACT_CLASS", false);
        this.classname = (String) properties.getOrDefault("CLASSNAME", "java.lang.Object");
    }

    @Override
    public Relationship process(Relationship next){
        Relationship ret = null;
        long nextEdgeId = next.getId();
        String key = nextEdgeId + "";
        if(Types.isAlias(next)){
            nextState.put(key, Transformer.setToIntArray(polluted));
            nextState.addAliasEdge(nextEdgeId);
            if(lastRelationship != null && preState.isStaticCall(lastRelationship.getId())){
                nextState.addStaticCallEdge(nextEdgeId);
            }
            ret = next;
        }else{
            int[][] callSite = EdgeCache.rel.get(next);
            int[] nextPos = calculator.calculate(callSite, polluted);

            if(nextPos != null && nextPos.length > 0){
                nextState.put(key, nextPos);
                if(isStatic){
                    nextState.addStaticCallEdge(nextEdgeId);
                }
                ret = next;
            }
        }
        return ret;
    }

    @Override
    public boolean isNeedProcess() {
        if(isSerializable || isStatic || isAbstract || isFromAbstractClass || "java.lang.Object".equals(classname)) return true;

        return lastRelationship != null && preState.isStaticCall(lastRelationship.getId()); // 如果上层是静态调用，则默认允许下一层可以不符合上面的条件
    }

    @Override
    public Processor copy() {
        return new JavaGadgetProcessor();
    }
}
