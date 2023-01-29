package tabby.expander.processor;

/**
 * @author wh1t3p1g
 * @since 2022/5/7
 */
public class ProcessorFactory {

    public static Processor newInstance(String name){
        if ("JavaGadget".equals(name)) {
            return new JavaGadgetProcessor();
        }else if("JavaGadgetBackward".endsWith(name)){
            return new JavaGadgetBackwardProcessor();
        }
        return new CommonProcessor();
    }
}
