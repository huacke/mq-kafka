package com.mq.redis.lock;

import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁工厂
 */
public class GlobalLockRedisFactory implements GlobalLockFactory {


	private final static int LOCK_EXPIRED = 15 * 60 * 1000; // 15minutes
	private final ConcurrentHashMap<String, GlobalLock> locks = new ConcurrentHashMap<String, GlobalLock>();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
	
	public RedisTemplate<String, String> redisClient;

	public RedisTemplate<String, String> getRedisClient() {
		return redisClient;
	}

	public void setRedisClient(RedisTemplate<String, String> redisClient) {
		this.redisClient = redisClient;
	}

	@Override
	public GlobalLock getLock(String lockKey) {
		return getLock(lockKey, LOCK_EXPIRED);
	}

	@Override
	public GlobalLock getLock(String lockKey, long aliveMill) {
		return getLock(lockKey,aliveMill,-1);
	}

	@Override
	public GlobalLock getLock(String lockKey, long aliveMill, long acquirePeriod) {
		GlobalLock lock = locks.computeIfAbsent(lockKey, key->{
			GlobalLock newlock = new GlobalLock(redisClient, lockKey, aliveMill,acquirePeriod);
			scheduler.submit(newCleanLockTask(lockKey, aliveMill));
			return newlock;
		});
		return lock;
	}

	@Override
	public void removeLock(String lockKey) {
		locks.remove(lockKey);
	}

	private CleanLockTask newCleanLockTask(String lockKey, long aliveMill) {
		return new CleanLockTask(lockKey, aliveMill);
	}

	private class CleanLockTask implements Runnable {
		private long delayMill = 1000;
		private String key;
		private long waitMill;
		private long currentMill;

		private CleanLockTask(String key, long waitMill) {
			this.key = key;
			currentMill = System.currentTimeMillis();
			this.waitMill = waitMill;
		}

		@Override
		public void run() {
			if (waitMill > 0) {
				if (System.currentTimeMillis() - currentMill > waitMill) {
					GlobalLock lock = locks.remove(key);
					lock.unlock();
				}
				else {// 重新安排任务
					delayMill += 100;
					scheduler.schedule(this, delayMill, TimeUnit.MILLISECONDS);
				}
			}
		}
	}

}