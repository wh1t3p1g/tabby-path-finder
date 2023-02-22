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

    private Map<String, int[][]> positions;
    private List<Long> alias;
    private List<Long> staticCalls;
    private List<Long> nextAlias; // 是否允许下一个节点进行alias操作

    public State() {
        this.positions = Collections.synchronizedMap(new HashMap<>());
        this.alias = new ArrayList<>();
        this.staticCalls = new ArrayList<>();
        this.nextAlias = new ArrayList<>();
    }

    public int[][] getInitialPositions(long nodeId){
        return getPositions("node_"+nodeId);
    }

    public void addInitialPositions(long nodeId, int[] positions){
        int len = positions.length;
        int[][] pos = new int[len][];
        for(int i=0;i<len;i++){
            pos[i] = new int[]{positions[i]};
        }
        this.positions.put("node_"+nodeId, pos);
    }

    public int[][] getPositions(String id){
        return positions.get(id);
    }

    public void put(String id, int[][] position){
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

    public State copy(){
        State state = new State();
        state.setAlias(new ArrayList<>(alias));
        state.setNextAlias(new ArrayList<>(nextAlias));
        state.setStaticCalls(new ArrayList<>(staticCalls));
        state.setPositions(new HashMap<>(positions));
        return state;
    }
}
