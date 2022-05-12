package tabby.data;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @author wh1t3p1g
 * @since 2022/4/26
 */
@Getter
@Setter
public class State {

    private Map<String, int[]> positions;
    private List<Long> alias;
    private List<Long> staticCalls;

    public State() {
        this.positions = Collections.synchronizedMap(new HashMap<>());
        this.alias = new ArrayList<>();
        this.staticCalls = new ArrayList<>();
    }

    public int[] getPositions(String id){
        return positions.get(id);
    }

    public void put(String id, int[] position){
        positions.put(id, position);
    }

    public boolean isEmpty(){
        return positions == null || positions.isEmpty();
    }

    public boolean isAlias(long id){
        return alias.contains(id);
    }

    public boolean isStaticCall(long id){
        return staticCalls.contains(id);
    }

    public void addAliasEdge(long id){
        alias.add(id);
    }

    public void addStaticCallEdge(long id){
        staticCalls.add(id);
    }

    public static State newInstance(){
        return new State();
    }

    public static State newInstance(int[] positions){
        State state = new State();
        state.put("initial", positions);
        return state;
    }
}
