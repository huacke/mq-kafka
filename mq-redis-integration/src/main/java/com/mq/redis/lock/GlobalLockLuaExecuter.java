package com.mq.redis.lock;

import com.mq.common.exception.BussinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.Arrays;
import java.util.List;


/**
 * redis分布式锁lua脚本执行器
 */
@Slf4j
public class GlobalLockLuaExecuter{

    /*申请锁脚本*/
    public  static  final String ACQUIRE_LOCK_SCRIPT = "if redis.call('setnx',KEYS[1],ARGV[1]) == 1 then" +
            " redis.call('expire',KEYS[1],ARGV[2]) return 1 else return 0 end";

    /*释放锁脚本*/
    public  static  final String UN_LOCK_SCRIPT = "if redis.call('get',KEYS[1]) == ARGV[1] then" +
            " redis.call('del',KEYS[1]) return 1 else return 0 end";

    /**
     * 申请锁
     * @param redisTemplate
     * @param lockKey  锁键
     * @param lockValue 锁值
     * @param expireTime 过期时间
     * @return
     */
    public static  Boolean acquireLock(RedisTemplate redisTemplate, String lockKey,Long lockValue,long expireTime){
        Boolean result=false;
        try{
            result = evalScript(redisTemplate, ACQUIRE_LOCK_SCRIPT, Boolean.class, Arrays.asList(new String[]{lockKey}), new Object[]{lockValue, expireTime});
        }catch (Exception e){
            log.error("GlobalLockLua 加锁出错： cause by:",e);
        }
        return result;
    }

    /**
     * 申请锁
     * @param redisTemplate
     * @param lockKey 锁键
     * @param lockValue 锁值
     * @return
     */
    public static  Boolean unlock(RedisTemplate redisTemplate, String lockKey,Long lockValue){
        Boolean result=false;
        try {
            result = evalScript(redisTemplate, UN_LOCK_SCRIPT, Boolean.class, Arrays.asList(new String[]{lockKey}), new Object[]{lockValue});
        }catch (Exception e){
            log.error("GlobalLockLua 解锁出错： cause by:",e);
        }
        return result;
    }

    /**
     * 执行脚本
     * @param redisTemplate
     * @param script
     * @param returnType 返回值类型
     * @param keys  键列表
     * @param params 值列表
     */
    private static  <T>T evalScript(RedisTemplate redisTemplate,String script, Class<T> returnType, List<String> keys, Object... params) {
        try{
            DefaultRedisScript<T> redisScript = new DefaultRedisScript<T>();
            redisScript.setResultType(returnType);
            redisScript.setScriptText(script);
            return   (T)redisTemplate.execute(redisScript,keys,params);
        }catch (Exception e){
            log.error("GlobalLockLua 执行Lua脚本错误 ： cause by:",e);
            throw  new BussinessException(e);
        }
    }
}
