package com.mq.thread.task;

/**
 * 注意：
 *为了保证在多线程嵌套调用父子线程数据能正确共享
 * 算价服务里面必须使用如下方式创建任务实例
 */
public class RunTask extends AbstractRunnable {
    Runnable runnable;
    public RunTask(Runnable runnable){
        super();
        this.runnable = runnable;
    }
    @Override
    public void doHandler() {
        runnable.run();
    }
}
