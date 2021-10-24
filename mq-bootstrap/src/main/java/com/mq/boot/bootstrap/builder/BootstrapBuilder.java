package com.mq.boot.bootstrap.builder;

import com.mq.boot.bootstrap.IBootstrap;
import com.mq.boot.bootstrap.context.BootstrapContext;
import com.mq.boot.hook.BootStopedHook;
import com.mq.boot.hook.BootStrapUpHook;
import com.mq.boot.hook.HookOrderedComparator;
import com.mq.boot.bootstrap.starter.CommonSpingBootstrap;
import com.mq.boot.def.BootstrapDef;
import com.mq.boot.hook.impl.BootOnStoppingDelayHook;
import com.mq.boot.hook.BootPreStopedHook;
import com.mq.boot.hook.impl.BootPreStopedSrvHook;
import org.springframework.util.Assert;
import java.util.*;

/**
 * 启动构建器
 */
@SuppressWarnings("ALL")
public class BootstrapBuilder {

    /**启动方式*/
    private int bootStrapType = 0;
    /*延迟下线时间 */
    private Long  stoppingDelayMs;
    /*启动类*/
    private Class bootStrapClassz;
    /*启动参数*/
    private String[] runArgs;
    /*启动配置表*/
    private Map<String, Object> bootConfig = new HashMap<>();
    /*启动完成后的处理钩子*/
    private List<BootStrapUpHook> startupHooks=new ArrayList<>();
    /*下线之前的预处理钩子*/
    private List<BootPreStopedHook> preStopHooks=new ArrayList<>();
    /*服务下线之后的回调处理器*/
    private List<BootStopedHook> stopedHooks=new ArrayList<BootStopedHook>();

    /**
     * 设置启动类
     * @param bootStrapClassz
     * @return
     */
    public BootstrapBuilder bootStrapClassz(Class bootStrapClassz) {
        this.bootStrapClassz = bootStrapClassz;
        return this;
    }

    /**
     * 设置延迟下线时间/毫秒
     * @param stoppingDelayMs
     * @return
     */
    public BootstrapBuilder stoppingDelayMs(Long  stoppingDelayMs) {
        this.stoppingDelayMs = stoppingDelayMs;
        return this;
    }

    /**
     * 设置启动参数
     * @param runArgs
     * @return
     */
    public BootstrapBuilder runArgs(String[] runArgs) {
        this.runArgs = runArgs;
        return this;
    }

    /**
     * 设置启动配置
     * @param bootConfig
     * @return
     */
    public BootstrapBuilder bootConfig(Map<String, Object> bootConfig) {
        if (null == bootConfig) return this;
        this.bootConfig.putAll(bootConfig);
        return this;
    }

    /**
     * 设置启动完成后的回调处理器
     * @param startupCallBack
     * @return
     */
    public BootstrapBuilder startupHook(BootStrapUpHook startupCallBack) {
        startupHooks.add(startupCallBack);
        return this;
    }

    /**
     * 设置下线前的预处理钩子
     * @param preStopHook
     * @return
     */
    public BootstrapBuilder preStopHook(BootPreStopedHook preStopHook) {
        this.preStopHooks.add(preStopHook);
        return this;
    }
    /**
     * 设置下线之后的钩子
     * @param stopedCallBack
     * @return
     */
    public BootstrapBuilder stopedHook(BootStopedHook bootStopedHook) {
        this.stopedHooks.add(bootStopedHook);
        return this;
    }

    /**
     * 构建启动器
     * @return
     */
    public IBootstrap build() {

        //如果没有配置启动类，则获取从当前运行栈获取main方法的启动类
        if (null == bootStrapClassz) { bootStrapClassz = getBootStrapMainClassz(); }

        Assert.notNull(bootStrapClassz, "bootStrapClassz is not null");

        String bootStrapClasszName = bootStrapClassz.getSimpleName();

        if (null == stoppingDelayMs) { stoppingDelayMs = BootstrapDef.DEFAULT_DELAY_STOPED_MS; }

        List<BootPreStopedHook> bootPreStopedSrvHooks =new ArrayList<>();
        bootPreStopedSrvHooks.add(new  BootPreStopedSrvHook());
        bootPreStopedSrvHooks.addAll(preStopHooks);
        bootPreStopedSrvHooks.add(new BootOnStoppingDelayHook(stoppingDelayMs));
        preStopHooks=bootPreStopedSrvHooks;


        //构建启动器上下文信息
        BootstrapContext bootstrapContext = new BootstrapContext();
        bootstrapContext.setBootStrapClassz(bootStrapClassz);
        bootstrapContext.setConfigMap(bootConfig);
        bootstrapContext.setRunArgs(runArgs);
        bootstrapContext.setStartupHooks(startupHooks);
        bootstrapContext.setPreStopHooks(preStopHooks);
        bootstrapContext.setStopedHooks(stopedHooks);

        HookOrderedComparator hookOrderedComparator = new HookOrderedComparator();

        //钩子正向排序
        Collections.sort(preStopHooks,hookOrderedComparator);
        Collections.sort(startupHooks,hookOrderedComparator);
        Collections.sort(stopedHooks,hookOrderedComparator);

        CommonSpingBootstrap bootstrap = null;
        //根据启动类型获取对应类型的启动器
        if (bootStrapType == BootstrapDef.BOOT_STRAP_TYPE_COMMON) {
            bootstrap = new CommonSpingBootstrap(bootstrapContext);
        } else {
            throw new RuntimeException("not support bootstrap type!");
        }
        return bootstrap;
    }

    /**
     * 获取启动类
     * @return
     */
    private Class getBootStrapMainClassz() {
        StackTraceElement[] stackTraces = new RuntimeException().getStackTrace();
        Optional<StackTraceElement> stackTraceElementOptional = Arrays.stream(stackTraces)
                .filter(it -> "main".equals(it.getMethodName()))
                .findFirst();
        if (!stackTraceElementOptional.isPresent()) {
            return null;
        }
        try {
            return Class.forName(stackTraceElementOptional.get().getClassName());
        } catch (ClassNotFoundException e) {
        }
        return null;
    }
}
