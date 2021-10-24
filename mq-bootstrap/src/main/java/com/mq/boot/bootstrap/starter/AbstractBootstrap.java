package com.mq.boot.bootstrap.starter;

import com.mq.boot.bootstrap.IBootstrap;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象启动器
 */
@Slf4j
public abstract class AbstractBootstrap implements IBootstrap {

    /**启动类*/
    protected Class bootStrapClassz;
    /*启动类名称*/
    protected String bootStrapClasszName;

    public AbstractBootstrap(Class boootStrapClassz) {
        this.bootStrapClassz = boootStrapClassz;
        this.bootStrapClasszName = boootStrapClassz.getSimpleName();
    }
    /**
     * 启动
     */
    public abstract void doStart();

    /**
     * 停止
     */
    public abstract void doStop();


    @Override
    public void start() {
        doStart();
    }

    @Override
    public void stop() {
        doStop();
    }

}
