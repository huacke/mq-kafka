package com.mq.worker;

/**
 * @author huacke
 * @version v1.0
 * @ClassName AbstractWorker
 * @Description 抽象工作处理器
 */
public abstract class AbstractWorker<R,T> implements Worker<R,T> {
	@Override
	public   R onWork(T data) {
		return null;
	}
}
