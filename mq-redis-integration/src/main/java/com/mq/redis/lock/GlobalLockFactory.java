package com.mq.redis.lock;

/**
 * 使用注意事项:
 *
 * 1 加锁操作后必须有解锁操作
 *  eg:
 *  try{
 *   g.lock();
 *  }finally{
 *    g.unlock();
 *  }
 *
 * 2 不支持锁重入
 */
public interface GlobalLockFactory {
	/**
	 * 获取一把锁,多次获取将得到相同的锁
	 * 
	 * @param lockKey
	 *            键
	 * @return 若与key相关联的 锁已存在,则直接返回此锁,否则创建一把锁，默认锁过期时间为12小时
	 * 
	 <pre>Example1:
			   GlobalLock gl = globalLockRedisFactory.getLock("lock_key1");
			   try{
			  		gl.lock();
			   }
			   catch (Exception e) {
					e.printStackTrace();
				}
				finally{
					gl.unlock();
				}

		Example2:
				GlobalLock gl = globalLockRedisFactory.getLock("lock_key1");
				try{
	                boolean acquire = lock.tryLock(500);
	                if (!acquire) {
	                   return;
				    }
				catch (Exception e) {
				e.printStackTrace();
				}
				finally{
				gl.unlock();
				}

		</pre>	 
	 */
	public GlobalLock getLock(String lockKey);

	/**
	 * 获取一把锁,第一次调用此方法将创建新锁，之后的调用将得到之前创建的锁且aliveMill参数将无效
	 * 
	 * @param lockKey
	 *            键
	 * @param aliveMill
	 *            设置锁的过期时间(单位毫秒),从第一次获取锁开始，持续的时间内将获得相同的锁，超时时间尽量设置合理的数值，有助于提升效率
	 * @return 若与key相关联的 锁已存在,则直接返回此锁,否则创建一把锁
	 */
	public GlobalLock getLock(String lockKey, long aliveMill);


	/**
	 * 获取一把锁,第一次调用此方法将创建新锁，之后的调用将得到之前创建的锁且aliveMill参数将无效
	 *
	 * @param lockKey
	 *            键
	 * @param aliveMill
	 * @param acquirePeriod 抢占锁的时间频率 ms
	 *            设置锁的过期时间(单位毫秒),从第一次获取锁开始，持续的时间内将获得相同的锁，超时时间尽量设置合理的数值，有助于提升效率
	 * @return 若与key相关联的 锁已存在,则直接返回此锁,否则创建一把锁
	 */
	public GlobalLock getLock(String lockKey, long aliveMill,long acquirePeriod);

	/**
	 * 删除锁，此删除锁只删除缓存中的锁，此时再得到的将是新的锁
	 * 
	 * @param lockKey
	 *            键
	 */
	public void removeLock(String lockKey);

}