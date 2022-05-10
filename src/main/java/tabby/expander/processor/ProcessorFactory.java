package tabby.expander.processor;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
public class ProcessorFactory {

    public static Processor newInstance(String name){
        switch (name){
            case "JavaGadget":
                return new JavaGadgetProcessor();
            default:
                return new CommonProcessor();
        }
    }
}
