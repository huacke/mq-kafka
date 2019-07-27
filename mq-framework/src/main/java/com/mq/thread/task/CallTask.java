package com.mq.thread.task;


/**
 * 注意：
 *为了保证在多线程嵌套调用父子线程数据能正确共享
 * 必须使用如下方式创建任务实例
 */
public   class CallTask<T> extends AbstractCallable<T> {
    @Override
    public T doHandler() throws Exception {
        return null;
    }
}