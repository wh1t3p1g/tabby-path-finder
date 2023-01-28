package tabby.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wh1t3p1g
 * @since 2022/6/2
 */
public class Transformer {

    public static int[] setToIntArray(Set<Integer> set){
        return set.stream().mapToInt(Integer::intValue).toArray();
    }

    public static Set<Integer> intArrayToSet(int[] array){
        return Arrays.stream(array).boxed().collect(Collectors.toSet());
    }
}
