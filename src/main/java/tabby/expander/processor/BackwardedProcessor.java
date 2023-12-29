package tabby.expander.processor;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import tabby.calculator.Calculator;
import tabby.data.Cache;
import tabby.data.Pollution;
import tabby.data.TabbyState;
import tabby.util.Types;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
public class BackwardedProcessor implements Processor<TabbyState> {

    private TabbyState nextState;
    private Pollution pollution;
    private boolean isCheckType = false;

    public BackwardedProcessor(boolean isCheckType) {
        this.isCheckType = isCheckType;
    }

    @Override
    public void init(Node node, TabbyState preState, Relationship lastRelationship) {
        this.nextState = TabbyState.of();

        if(lastRelationship == null){
            this.pollution = preState.get("node_"+node.getId());
        }else{
            long id = lastRelationship.getId();
            this.pollution = preState.get(String.valueOf(id));
        }
    }

    @Override
    public Relationship process(Relationship next) {
        Relationship ret = null;
        String nextId = String.valueOf(next.getId());
        if(Types.isAlias(next)){
            nextState.put(nextId, pollution);
            ret = next;
        }else{
            Pollution callSite = Cache.rel.get(next, false);

            Pollution nextPollution = Pollution.getNextPollutionReverse(pollution, callSite);
            if(nextPollution != null){
                nextState.put(nextId, nextPollution);
                ret = next;
            }
        }
        return ret;
    }

    @Override
    public boolean isNeedProcess() {
        return true;
    }

    @Override
    public TabbyState getNextState() {
        return nextState;
    }

    @Override
    public void setCalculator(Calculator calculator) {
    }

    @Override
    public boolean isLastRelationshipTypeAlias() {
        return false;
    }

    @Override
    public Processor<TabbyState> copy() {
        return new BackwardedProcessor(isCheckType);
    }

    @Override
    public Processor<TabbyState> reverse() {
        return new ForwardedProcessor(isCheckType);
    }

    @Override
    public void setDBSource(GraphDatabaseService db) {

    }

    @Override
    public void setTransaction(Transaction tx) {

    }
}
