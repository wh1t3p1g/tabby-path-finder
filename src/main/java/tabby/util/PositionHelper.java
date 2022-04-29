package tabby.util;

/**
 * @author wh1t3P1g
 * @since 2022/2/8
 */
public class PositionHelper {

    public static int THIS = -1;
    public static int SOURCE = -2;
    public static int NOT_POLLUTED_POSITION = -3;

    public static boolean isNotPollutedPosition(int pos){
        return NOT_POLLUTED_POSITION == pos;
    }

    public static boolean isNotPollutedPosition(int[] pos){
        return pos.length == 1 && NOT_POLLUTED_POSITION == pos[0];
    }
}
