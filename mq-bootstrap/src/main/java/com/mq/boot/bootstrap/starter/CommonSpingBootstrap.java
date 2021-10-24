package com.mq.boot.bootstrap.starter;


import com.mq.boot.bootstrap.context.BootstrapContext;
import com.mq.boot.def.BootstrapConfigDef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;
import java.util.Map;


/**
 * 通用的springboot Web 启动器
 */
@Slf4j
public class CommonSpingBootstrap extends AbstractBootstrap {

    /*运行状态*/
    private static volatile boolean running = true;
    /*启动上下文信息*/
    private BootstrapContext bootstrapContext;
    /*spring 应用 context*/
    private ApplicationContext applicationContext;

    public CommonSpingBootstrap(BootstrapContext bootstrapContext) {
        super(bootstrapContext.getBootStrapClassz());
        this.bootstrapContext = bootstrapContext;
    }

    @Override
    public void doStart() {

        String[] runArgs = bootstrapContext.getRunArgs();
        Map<String, Object> configMap = bootstrapContext.getConfigMap();

        //把spring默认注册的下线钩子关闭，使用我们自定义的ShutdownHook
        SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(bootStrapClassz).registerShutdownHook(false);

        //如果配置为空则走默认的配置构建
        if (CollectionUtils.isEmpty(configMap)) {
            springApplicationBuilder = springApplicationBuilder
                    .web(WebApplicationType.SERVLET)
                    .bannerMode(Banner.Mode.OFF);
        } else {
            //如果存在定义的配置，则根据配置定义的参数构建
            if (configMap.containsKey(BootstrapConfigDef.BOOTSTRAP_CONF_WEBAPPLICATIONTYPE)) {
                springApplicationBuilder = springApplicationBuilder
                        .web((WebApplicationType) configMap.get(BootstrapConfigDef.BOOTSTRAP_CONF_WEBAPPLICATIONTYPE));
            }
            if (configMap.containsKey(BootstrapConfigDef.BOOTSTRAP_CONF_BANNERMODE)) {
                springApplicationBuilder = springApplicationBuilder
                        .bannerMode((Banner.Mode) configMap.get(BootstrapConfigDef.BOOTSTRAP_CONF_BANNERMODE));
            }
        }
        //启动spring 容器
        applicationContext = springApplicationBuilder.run(runArgs);

        //保持运行
        running(applicationContext, runArgs);
    }

    @Override
    public void doStop() {

        //执行预下线的钩子
        bootstrapContext.getPreStopHooks().forEach(it->{
            try{
                it.run();
            }catch (Exception e){
                log.error(String.format("%执行预下线的钩子发生错误!", bootStrapClasszName), e);
            }
        });
        synchronized (bootStrapClassz) {
            try {
                //开始停止 spring容器
                ((ConfigurableApplicationContext) applicationContext).close();
            } catch (Exception e) {
                log.error(String.format("%停止时发生错误!", bootStrapClasszName), e);
            }
            //运行状态更新为停止状态
            running = false;
            //唤醒主线程，让主线程能正常退出
            bootStrapClassz.notify();
        }
    }

    /**
     * 保持运行
     * @param applicationContext
     * @param args
     */
    private void running(ApplicationContext applicationContext, String[] args) {

        //注册停机钩子
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                //停止容器
                doStop();
            }
        });

        log.info(String.format("启动%s服务成功", bootStrapClasszName));

        //执行启动完成后的回调
        bootstrapContext.getStartupHooks().forEach(it->{
            try{
                it.run();
            }catch (Exception e){
                log.error(String.format("%执行启动完成后的钩子发生错误!", bootStrapClasszName), e);
            }
        });

        synchronized (bootStrapClassz) {
            while (running) {
                try {
                    bootStrapClassz.wait();
                } catch (Exception e) {
                    log.error(String.format("%服务发生错误!", bootStrapClasszName), e);
                }
            }
        }
        //执行启动完成后的回调
        bootstrapContext.getStopedHooks().forEach(it->{
            try{
                it.run();
            }catch (Exception e){
                log.error(String.format("%执行停止后的钩子发生错误!", bootStrapClasszName), e);
            }
        });
        log.info(String.format("%s服务已停止", bootStrapClasszName));
    }
}
