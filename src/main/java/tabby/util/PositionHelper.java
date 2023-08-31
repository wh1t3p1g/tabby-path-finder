package tabby.util;

import java.util.Set;

/**
 * @author wh1t3P1g
 * @since 2022/2/8
 */
public class PositionHelper {

    public final static int THIS = -1;
    public final static int SOURCE = -2;
    public final static int NOT_POLLUTED_POSITION = -3;
    public final static int DAO = -4;
    public final static int RPC = -5;
    public final static int AUTH = -6;

    public static boolean isNotPollutedPosition(Object pos){
        if(pos instanceof int[]){
            int[] val = (int[]) pos;
            return val.length == 1 && NOT_POLLUTED_POSITION == val[0];
        }

        return NOT_POLLUTED_POSITION == (int) pos;
    }

    public static boolean isThisPolluted(int[][] polluted){
        for(int[] pos:polluted){
            Set<Integer> set = Transformer.intArrayToSet(pos);
            if(set.contains(PositionHelper.THIS)) return true;
        }
        return false;
    }
}
