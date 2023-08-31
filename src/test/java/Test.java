import tabby.data.Pollution;

import java.io.IOException;
import java.util.Set;

/**
 * @author wh1t3p1g
 * @since 2023/8/24
 */
public class Test {

    public static void main(String[] args) throws IOException {
        Pollution pollution = new Pollution();
        pollution.getSpecialPolluted().put(1, 111);
        pollution.getSpecialPolluted().put(1, 222);
        pollution.getSpecialPolluted().put(2, 333);
        pollution.getTypes().add(Set.of("test"));
        pollution.getPolluted().add(Set.of(123));
        pollution.getPolluted().add(Set.of(2222));
        Pollution copied = pollution.copy();
        System.out.println(copied);
        copied.getSpecialPolluted().remove(1, 111);
        System.out.println(pollution);
        System.out.println(copied);
    }
}
