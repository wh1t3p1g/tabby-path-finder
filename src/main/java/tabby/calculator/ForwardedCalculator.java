package tabby.calculator;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wh1t3p1g
 * @since 2022/5/10
 */
public class ForwardedCalculator implements Calculator{

    @Override
    public int[] v2(int[][] callSite, Set<Integer> polluted) {
        Set<Integer> nextPolluted = new HashSet<>();
        int length = callSite.length;
        for(int pos=0; pos<length; pos++){
            int[] current = callSite[pos];
            for(int c:current){
                if(polluted.contains(c)){
                    nextPolluted.add(pos - 1);
                    break;
                }
            }
        }
        return nextPolluted.stream().mapToInt(Integer::intValue).toArray();
    }


}
