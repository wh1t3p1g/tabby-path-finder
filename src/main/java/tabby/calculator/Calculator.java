package tabby.calculator;

import java.util.Set;

/**
 * @author wh1t3p1g
 * @since 2022/5/10
 */
public interface Calculator {

    int[] v2(int[][] callSite, Set<Integer> polluted);

    default int[] calculate(int[][] callSite, Set<Integer> polluted){
        try{
            return v2(callSite, polluted);
        }catch (Exception e){
        }
        return new int[0];
    }

}
