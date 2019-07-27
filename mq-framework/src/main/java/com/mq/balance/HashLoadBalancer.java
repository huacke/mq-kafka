package com.mq.balance;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @version v1.0
 * @Description 基于hash负载均衡器
 */
public class HashLoadBalancer<K,T> extends AbstractLoadBalancer<K,T>{

    public HashLoadBalancer() {
        super();
    }
    @Override
    public synchronized T get() {
        throw  new UnsupportedOperationException("not supported operation!");
    }
    public synchronized T get(K key)
    {
       List<DelegateTarget<K, T>> targets = getTargets();
        if(ObjectUtils.isEmpty(targets)) {
            return null;
        }
        return targets.get(index(key)).getTarget();
    }
    private int index(K key){
       return Math.abs(HashCodeBuilder.reflectionHashCode(key))% targets.size();
    }

}
