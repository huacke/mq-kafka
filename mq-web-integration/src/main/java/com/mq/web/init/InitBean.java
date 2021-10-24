package com.mq.web.init;


import com.mq.utils.LocalInfo;
import com.mq.utils.ThreadUtil;

/**
 * 启动提前初始化耗时组件
 */
public class InitBean {

    public InitBean() {
        init();
    }
    private void init() {
        new LocalInfo();
        String ip = LocalInfo.ip;
        String mac = LocalInfo.mac;
        Integer processId = ThreadUtil.processId;
    }
}
