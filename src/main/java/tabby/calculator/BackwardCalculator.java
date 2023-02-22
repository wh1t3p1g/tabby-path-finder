package tabby.calculator;

import tabby.util.PositionHelper;
import tabby.util.Transformer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wh1t3p1g
 * @since 2022/5/10
 */
public class BackwardCalculator implements Calculator {

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
                // 如果仅剩下-2，则后续的节点只需判断是否是source节点即可
                newPolluted.add(PositionHelper.SOURCE);
            } else{
                return null;
            }
        }

        newPolluted.remove(PositionHelper.NOT_POLLUTED_POSITION);
        return newPolluted.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * polluted
     * [[1],[2,3]]
     * 代表 第2个参数 + 第3或4个参数 是可控的，子集合里面为或关系，不同子集为并关系
     * @param callSite
     * @param polluted
     * @return
     */
    @Override
    public int[][] v3(int[][] callSite, int[][] polluted) {
        Set<int[]> ret = new HashSet<>();
        int len = polluted.length;
        for(int i=0;i<len;i++){
            Set<Integer> newPolluted = new HashSet<>();
            int[] pos = polluted[i];
            for(int p:pos){
                int index = p + 1;
                if(index < callSite.length && index >= 0){
                    // 多个值取或
                    int[] call = callSite[index];
                    if(PositionHelper.isNotPollutedPosition(call)) continue;
                    newPolluted.addAll(Transformer.intArrayToSet(call));
                }else if(p == PositionHelper.SOURCE){
                    newPolluted.add(PositionHelper.SOURCE);
                }
            }
            if(newPolluted.isEmpty()) return null;
            newPolluted.remove(PositionHelper.NOT_POLLUTED_POSITION);
            ret.add(Transformer.setToIntArray(newPolluted));
        }

        return ret.toArray(new int[0][]);
    }
}
