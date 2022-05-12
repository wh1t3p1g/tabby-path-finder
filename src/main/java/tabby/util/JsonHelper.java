package tabby.util;

import com.google.gson.Gson;

/**
 * @author wh1t3p1g
 * @since 2022/1/6
 */
public class JsonHelper {

    public static Gson gson = new Gson();

    public static int[] parsePollutedPosition(String position){
        try{
            return gson.fromJson(position, int[].class);
        }catch (Exception e){
            return new int[0];
        }
    }

    public static int[][] parse(String polluted){
        try{
            return gson.fromJson(polluted, int[][].class);
        }catch (Exception e){
            // 强制转化 v1版本的callSite
            int[] position = gson.fromJson(polluted, int[].class);
            int[][] ret = new int[position.length][];
            for(int i=0;i<position.length;i++){
                ret[i] = new int[]{position[i]};
            }
            return ret;
        }
    }
}
