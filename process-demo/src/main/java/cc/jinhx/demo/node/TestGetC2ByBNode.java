package cc.jinhx.demo.node;

import cc.jinhx.demo.context.TestContext;
import cc.jinhx.demo.service.TestService;
import cc.jinhx.process.AbstractNode;
import cc.jinhx.process.NodeChainContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TestGetC1ByANode
 *
 * @author jinhx
 * @since 2022-03-29
 */
public class TestGetC2ByBNode extends AbstractNode<TestContext> {

    @Autowired
    private TestService testService;

    @Override
    public void process(NodeChainContext<TestContext> testNodeChainContext) {
        TestContext contextInfo = testNodeChainContext.getContextInfo();
        System.out.println(Thread.currentThread().getName() + "start3");
        try {
            Thread.sleep(180L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(Thread.currentThread().getName() + "start4");
        if ("B".equals(contextInfo.getB()) && "C1".equals(contextInfo.getC1())){
            contextInfo.setC2(testService.getC() + "2");
        } else {
            int i = 1/0;
        }
    }

}
