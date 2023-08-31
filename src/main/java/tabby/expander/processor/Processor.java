package tabby.expander.processor;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import tabby.calculator.Calculator;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
public interface Processor<STATE> {

    void init(Node node, STATE preState, Relationship lastRelationship);

    Relationship process(Relationship next);

    /**
     * 判断当前节点是否有必要继续往下扩展
     * 类似PathEvaluator的功能
     * @return
     */
    boolean isNeedProcess();

    STATE getNextState();

    void setCalculator(Calculator calculator);

    boolean isLastRelationshipTypeAlias();

    Processor<STATE> copy();

    Processor<STATE> reverse();
}
