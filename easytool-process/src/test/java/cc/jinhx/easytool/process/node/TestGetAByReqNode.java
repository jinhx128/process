package cc.jinhx.easytool.process.node;

import cc.jinhx.easytool.process.context.TestContext;
import cc.jinhx.easytool.process.topology.TopologyContext;
import cc.jinhx.easytool.process.service.TestService;
import cc.jinhx.easytool.process.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TestGetAByReqNode
 *
 * @author jinhx
 * @since 2022-03-29
 */
public class TestGetAByReqNode extends AbstractNode<TestContext> {

    @Autowired
    private TestService testService;

    @Override
    protected boolean isSkip(TopologyContext<TestContext> topologyContext) {
        return false;
    }

    @Override
    protected void process(TopologyContext<TestContext> topologyContext) {
        TestContext contextInfo = topologyContext.getContextInfo();
        if ("req".equals(contextInfo.getReq())){
            contextInfo.setA(testService.getA());
        }
    }

    @Override
    public void onUnknowFail(TopologyContext<TestContext> topologyContext, Exception e) {

    }

    @Override
    public void onBusinessFail(TopologyContext<TestContext> topologyContext, BusinessException e) {

    }

    @Override
    public void onTimeoutFail(TopologyContext<TestContext> topologyContext) {

    }

}
