package tabby.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author wh1t3p1g
 * @since 2022/4/26
 */
public class State {

    private Map<String, int[]> positions;

    public State() {
        this.positions = Collections.synchronizedMap(new HashMap<>());
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

    public static State newInstance(){
        return new State();
    }

    public static State newInstance(int[] positions){
        State state = new State();
        state.put("initial", positions);
        return state;
    }

    public int[] test(int[] current, String polluted, boolean isLastRelationshipTypeAlias){
        try{
            int[][] callPos = JsonHelper.gson.fromJson(polluted, int[][].class);
            return test(current, callPos, isLastRelationshipTypeAlias);
        }catch (Exception e){
            // 兼容tabby 1.x版本
            int[] callPos = JsonHelper.gson.fromJson(polluted, int[].class);
            return test(current, callPos, isLastRelationshipTypeAlias);
        }

    }

    public int[] test(int[] current, int[][] callPos, boolean isLastRelationshipTypeAlias){
        Set<Integer> newPolluted = new HashSet<>();

        // 对于上一条边为alias的，但是当前的call边 调用者又是不可控的，那说明无法进行alias操作，直接剔除
        if(isLastRelationshipTypeAlias && callPos.length > 0
                && callPos[0].length == 1 && callPos[0][0] == -3){
            return null;
        }

        for(int p : current){
            int pos = p + 1;
            if(pos < callPos.length){
                int[] call = callPos[pos];
                if(call.length == 1 && call[0] == -3) return null;
                newPolluted.addAll(Arrays.stream(call).boxed().collect(Collectors.toSet()));
            }else{// 超出数组长度
                return null;
            }
        }
        return newPolluted.stream().mapToInt(Integer::intValue).toArray();
    }

    public int[] test(int[] current, int[] callPos, boolean isLastRelationshipTypeAlias){
        Set<Integer> newPolluted = new HashSet<>();

        if(isLastRelationshipTypeAlias && callPos.length > 0 && callPos[0] == -3){
            return null;
        }

        for(int p : current){
            int pos = p + 1;
            if(pos < callPos.length){
                int call = callPos[pos];
                if(call == -3) return null;
                newPolluted.add(call);
            }else{ // 超出数组长度
                return null;
            }
        }
        return newPolluted.stream().mapToInt(Integer::intValue).toArray();
    }

    static class PathPredicate implements Predicate<List<Set<Integer>>> {

        @Override
        public boolean test(List<Set<Integer>> positions) {
            return false;
        }

        @Override
        public Predicate<List<Set<Integer>>> and(Predicate<? super List<Set<Integer>>> other) {
            return Predicate.super.and(other);
        }
    }
}
