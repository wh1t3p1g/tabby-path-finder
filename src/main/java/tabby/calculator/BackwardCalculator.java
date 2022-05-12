package tabby.calculator;

import tabby.util.PositionHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wh1t3p1g
 * @since 2022/5/10
 */
public class BackwardCalculator implements Calculator{

//    /**
//     * 返回下一个节点污点状态需求
//     * 如果返回的是[-2] 说明下一个节点可以强制保留路径
//     * @param callSite
//     * @param polluted
//     * @return
//     */
//    @Override
//    public int[] v1(int[] callSite, Set<Integer> polluted) {
//        Set<Integer> newPolluted = new HashSet<>();
//
//        for(int p:polluted){
//            int pos = p + 1;
//            if(pos < callSite.length && pos >= 0){
//                int call = callSite[pos];
//                if(call == PositionHelper.NOT_POLLUTED_POSITION) return null;
//                newPolluted.add(call);
//            }else if(p == PositionHelper.SOURCE){
//                // do nothing
//                newPolluted.add(PositionHelper.SOURCE);
//            } else{
//                return null;
//            }
//        }
//
//        return newPolluted.stream().mapToInt(Integer::intValue).toArray();
//    }

    /**
     * 返回下一个节点污点状态需求
     * 如果返回的是[-2] 说明下一个节点可以强制保留路径
     * @param callSite
     * @param polluted
     * @return
     */
    @Override
    public int[] v2(int[][] callSite, Set<Integer> polluted) {
        Set<Integer> newPolluted = new HashSet<>();

        for(int p : polluted){
            int pos = p + 1;
            if(pos < callSite.length && pos >= 0){
                int[] call = callSite[pos];
                if(PositionHelper.isNotPollutedPosition(call)) return null;
                newPolluted.addAll(Arrays.stream(call).boxed().collect(Collectors.toSet()));
            }else if(p == PositionHelper.SOURCE){
                newPolluted.add(PositionHelper.SOURCE);
            } else{
                return null;
            }
        }

        newPolluted.remove(PositionHelper.NOT_POLLUTED_POSITION);
        return newPolluted.stream().mapToInt(Integer::intValue).toArray();
    }
}
