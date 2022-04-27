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
}
