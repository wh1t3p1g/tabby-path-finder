package tabby.expander.processor;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import tabby.calculator.Calculator;
import tabby.data.State;
import tabby.util.PositionHelper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wh1t3p1g
 * @since 2022/5/10
 */
public abstract class BaseProcessor implements Processor{

    public Node node = null;
    public State preState = null;
    public State nextState = null;
    public Relationship lastRelationship = null;
    public Calculator calculator = null;
    public Set<Integer> polluted = null;

    @Override
    public void init(Node node, State preState, Relationship lastRelationship, Calculator calculator) {
        this.node = node;
        this.preState = preState;
        this.nextState = State.newInstance();
        this.lastRelationship = lastRelationship;
        this.calculator = calculator;

        int[] polluted = null;
        if(lastRelationship == null){
            polluted = preState.getInitialPositions(node.getId());
        }else{
            long id = lastRelationship.getId();
            polluted = preState.getPositions(id + "");
        }

        this.polluted = Arrays.stream(polluted).boxed().collect(Collectors.toSet());
        this.polluted.add(PositionHelper.SOURCE); // 添加source
    }
}
