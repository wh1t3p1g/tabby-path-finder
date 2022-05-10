package tabby.calculator;

import tabby.util.JsonHelper;

import java.util.Set;

/**
 * @author wh1t3p1g
 * @since 2022/5/10
 */
public interface Calculator {

    int[] v1(int[] callSite, Set<Integer> polluted);

    int[] v2(int[][] callSite, Set<Integer> polluted);

    default int[] calculate(String callSite, Set<Integer> polluted){
        try{
            int[][] callPos = JsonHelper.gson.fromJson(callSite, int[][].class);
            return v2(callPos, polluted);
        }catch (Exception e){
            int[] callPos = JsonHelper.gson.fromJson(callSite, int[].class);
            return v1(callPos, polluted);
        }
    }

}
