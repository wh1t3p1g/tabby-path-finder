package tabby.calculator;

import java.util.Set;

/**
 * @author wh1t3p1g
 * @since 2022/5/10
 */
public interface Calculator {

    int[] v2(int[][] callSite, Set<Integer> polluted);
    int[][] v3(int[][] callSite, int[][] polluted);

    default int[] calculate(int[][] callSite, Set<Integer> polluted){
        try{
            return v2(callSite, polluted);
        }catch (Exception e){
        }
        return new int[0];
    }

    default int[][] calculate(int[][] callSite, int[][] polluted){
        try{
            return v3(callSite, polluted);
        }catch (Exception e){
        }
        return new int[][]{new int[]{-3}};
    }

}
