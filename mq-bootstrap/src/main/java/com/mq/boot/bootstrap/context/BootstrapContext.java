package com.mq.boot.bootstrap.context;

import com.mq.boot.hook.BootStopedHook;
import com.mq.boot.hook.BootStrapUpHook;
import com.mq.boot.hook.BootPreStopedHook;
import lombok.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 启动器上下文信息
 */
@Data
public class BootstrapContext {
    /*启动类*/
    private Class bootStrapClassz;
    /*启动参数*/
    private String[] runArgs;
    /*启动配置表*/
    private Map<String, Object> configMap=new HashMap<>();
    /*启动完成后的处理钩子*/
    private List<BootStrapUpHook> startupHooks=new ArrayList<>();
    /*下线之前的预处理钩子*/
    private List<BootPreStopedHook> preStopHooks=new ArrayList<>();
    /*服务下线之后的回调处理器*/
    private List<BootStopedHook> stopedHooks=new ArrayList<BootStopedHook>();
}
