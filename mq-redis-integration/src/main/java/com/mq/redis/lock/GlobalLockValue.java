package com.mq.redis.lock;

import com.mq.utils.ThreadUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * redis分布式锁随机值生成器
 */
@SuppressWarnings("ALL")
@Component
public class GlobalLockValue implements InitializingBean,DisposableBean {

   private static ScheduledExecutorService scheduUpadater=null;

    private static final long MIN_INNCR_VALUE=1;

    private static final long MAX_INNCR_VALUE=24*60*60*1000;

    private static final AtomicLong incrementer =new AtomicLong(randomNum());

    /*锁随机值缓存(随机值存入redis,为保证释放的是当前获取锁线程的锁)*/
    private static final   ThreadLocal<Long> globalLockValueHolder = new ThreadLocal<>();

    /**
     * 获取新的随机值
     * @return
     */
    protected static Long newValue() {
        Long lockValue = gen();
        return lockValue;
    }

    /**
     * 设置随机值到缓存
     * @return
     */
    protected static void set(Long value) {
        globalLockValueHolder.set(value);
    }

    /**
     * 获取缓存中的随机值
     * @return
     */
    protected static Long get() {
        Long currentLockValue = globalLockValueHolder.get();
        return currentLockValue;
    }
    /**
     * 重置
     * @return
     */
    protected static void reset() {
        globalLockValueHolder.remove();
    }

    /**
     * 获取随机值
     * @return
     */
    private static Long gen(){
        long now = System.currentTimeMillis();
        Integer processId = ThreadUtil.processId;
        long incr = incrementer.incrementAndGet();
        long rs = now + processId + incr;
        return rs;
    }

    private static Long randomNum(){ return ThreadLocalRandom.current().nextLong(MIN_INNCR_VALUE,MAX_INNCR_VALUE); }

    private static void scheduleUpdating() { scheduUpadater.scheduleAtFixedRate(() -> incrementer.set(randomNum()), 2000, 2000, TimeUnit.MILLISECONDS); }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(null==scheduUpadater){
            scheduUpadater= Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "RedisLockRandomUpdater");
                thread.setDaemon(true);
                return thread;
            });
            scheduleUpdating();
        }
    }

    @Override
    public void destroy() throws Exception {
        if(scheduUpadater!=null){
            scheduUpadater.shutdownNow();
            scheduUpadater=null;
        }
    }
}
