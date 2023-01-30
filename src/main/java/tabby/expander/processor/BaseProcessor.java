package tabby.expander.processor;

import lombok.Getter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import tabby.calculator.Calculator;
import tabby.data.State;
import tabby.util.PositionHelper;
import tabby.util.Transformer;
import tabby.util.Types;

import java.util.Set;

/**
 * @author wh1t3p1g
 * @since 2022/5/10
 */
@Getter
public abstract class BaseProcessor implements Processor{

    public Node node = null;
    public State preState = null;
    public State nextState = null;
    public Relationship lastRelationship = null;
    public Calculator calculator = null;
    public Set<Integer> polluted = null;
    public boolean isLastRelationshipTypeAlias = false;
    public boolean isFirstNode = false;

    @Override
    public void init(Node node, State preState, Relationship lastRelationship) {
        this.node = node;
        this.preState = preState;
        this.nextState = State.newInstance();
        this.lastRelationship = lastRelationship;

        int[] polluted = null;
        if(lastRelationship == null){
            polluted = preState.getInitialPositions(node.getId());
            isFirstNode = true;
        }else{
            long id = lastRelationship.getId();
            polluted = preState.getPositions(id + "");
            isLastRelationshipTypeAlias = Types.isAlias(lastRelationship);
            isFirstNode = false;
        }

        this.polluted = Transformer.intArrayToSet(polluted);
        this.polluted.add(PositionHelper.SOURCE); // 添加source
    }

    public void setCalculator(Calculator calculator){
        this.calculator = calculator;
    }

    @Override
    public boolean isLastRelationshipTypeAlias() {
        return isLastRelationshipTypeAlias;
    }

    @Override
    public boolean isNeedProcess() {
        return true;
    }

    @Override
    public State getNextState() {
        return nextState;
    }
}
