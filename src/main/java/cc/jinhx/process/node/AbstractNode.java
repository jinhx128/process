package cc.jinhx.process.node;

import cc.jinhx.process.annotation.Node;
import cc.jinhx.process.chain.NodeChainContext;
import cc.jinhx.process.enums.NodeFailHandleEnums;
import cc.jinhx.process.enums.NodeLogLevelEnums;
import cc.jinhx.process.enums.NodeTimeoutEnums;
import cc.jinhx.process.exception.BusinessException;
import cc.jinhx.process.util.JsonUtils;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.StopWatch;

import java.util.List;

/**
 * 抽象节点
 *
 * @author jinhx
 * @since 2021-08-06
 */
@Data
@Slf4j
@Node
public abstract class AbstractNode<T> {

    private final String NODE_LOG = "nodeLog ";
    private final String LOG_END = " execute success";
    private final String NODE_CHAIN_NAME = " nodeChainName=";
    private final String NODE_NAME = " nodeName=";
    private final String LOG_SKIP = " skip=";
    private final String LOG_TIME = " time=";
    private final String BEFORE_EXECUTE_PARAMS = " beforeExecuteParams=";
    private final String AFTER_EXECUTE_PARAMS = " afterExecuteParams=";
    private final String TRUE = "true";
    private final String FALSE = "false";

    /**
     * 节点失败处理
     */
    private Integer failHandle = NodeFailHandleEnums.INTERRUPT.getCode();

    /**
     * 节点执行超时时间
     */
    private Long timeout = NodeTimeoutEnums.COMMONLY.getCode();

    /**
     * 获取上下文信息
     *
     * @param nodeChainContext nodeChainContext
     */
    protected <T> T getContextInfo(NodeChainContext<T> nodeChainContext){
        return nodeChainContext.getContextInfo();
    }

    /**
     * 业务失败
     *
     * @param code code
     * @param msg msg
     */
    protected void businessFail(Integer code, String msg){
        throw new BusinessException(code, msg);
    }

    /**
     * 节点执行方法
     *
     * @param nodeChainContext nodeChainContext
     */
    protected abstract void process(NodeChainContext<T> nodeChainContext);

    /**
     * 通用执行方法
     *
     * @param nodeChainContext nodeChainContext
     * @param logLevel logLevel
     */
    public void execute(NodeChainContext<T> nodeChainContext, Integer logLevel, String nodeChainName) {
        String logStr = NODE_LOG + nodeChainContext.getLogStr();
        String nodeName = this.getClass().getName();
        try {
            // 日志
            StringBuilder logInfo = new StringBuilder(logStr);

            buildLogInfo(logInfo, Lists.newArrayList(LOG_END, NODE_CHAIN_NAME, nodeChainName, NODE_NAME, nodeName), logLevel, NodeLogLevelEnums.BASE.getCode(), false);
            buildLogInfo(logInfo, Lists.newArrayList(BEFORE_EXECUTE_PARAMS, JsonUtils.objectToJson(nodeChainContext)), logLevel, NodeLogLevelEnums.BASE_AND_TIME_AND_PARAMS.getCode(), false);

            // 耗时计算
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            beforeLog();

            if (isSkip(nodeChainContext)) {
                buildLogInfo(logInfo, Lists.newArrayList(LOG_SKIP, TRUE), logLevel, NodeLogLevelEnums.BASE.getCode(), false);
            } else {
                buildLogInfo(logInfo, Lists.newArrayList(LOG_SKIP, FALSE), logLevel, NodeLogLevelEnums.BASE.getCode(), false);
                process(nodeChainContext);
            }

            afterLog();

            buildLogInfo(logInfo, Lists.newArrayList(AFTER_EXECUTE_PARAMS, JsonUtils.objectToJson(nodeChainContext)), logLevel, NodeLogLevelEnums.BASE_AND_TIME_AND_PARAMS.getCode(), false);

            stopWatch.stop();
            long time = stopWatch.getTime();

            buildLogInfo(logInfo, Lists.newArrayList(LOG_TIME, time), logLevel, NodeLogLevelEnums.BASE_AND_TIME.getCode(), true);
        } catch (BusinessException e) {
            log.error(logStr + " execute business fail nodeName={} msg={}", nodeName, ExceptionUtils.getStackTrace(e));
            throw e;
        } catch (Exception e) {
            log.error(logStr + " execute fail nodeName={} msg={}", nodeName, ExceptionUtils.getStackTrace(e));
            throw e;
        }
    }

    /**
     * 通过传进来的节点日志类型判断打印什么日志，太长可能出现YGC频繁
     *
     * @param logInfo logInfo
     * @param logInfos logInfos
     * @param print print
     */
    private void buildLogInfo(StringBuilder logInfo, List<Object> logInfos, Integer logLevel, Integer thisLogLevel, Boolean print) {
        if (!NodeLogLevelEnums.containsCode(logLevel)){
            logLevel = NodeLogLevelEnums.BASE_AND_TIME.getCode();
        }

        if (thisLogLevel <= logLevel && !NodeLogLevelEnums.NO.getCode().equals(logLevel)){
            logInfos.forEach(logInfo::append);
        }

        if (print && !NodeLogLevelEnums.NO.getCode().equals(logLevel)){
            log.info(logInfo.toString());
            // 打印完手动释放内存
            logInfo.setLength(0);
        }
    }

    /**
     * 节点执行后打印日志，执行失败则不打印
     */
    protected void afterLog() {
    }

    /**
     * 节点执行前打印日志
     */
    protected void beforeLog() {
    }

    /**
     * 是否跳过当前执行方法，默认不跳过
     *
     * @param nodeChainContext nodeChainContext
     * @return 是否跳过当前执行方法
     */
    protected boolean isSkip(NodeChainContext<T> nodeChainContext) {
        return false;
    }

}
