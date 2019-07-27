package com.mq.balance;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.util.ObjectUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @version v1.0
 * @ClassName abstractLoadBalancer
 * @Description 抽象负载均衡器
 */
public class AbstractLoadBalancer<K,T> implements LoadBalancer<K,T> {

    protected List<DelegateTarget<K,T>> targets =new ArrayList<>();

    @Override
    public synchronized void  addTarget(T target) {
        targets.add(new DelegateTarget<K,T>(target));
    }

    @Override
    public synchronized void removeTarget(T target) {
        Iterator<DelegateTarget<K,T>> it = targets.iterator();
        while(it.hasNext()){
            DelegateTarget<K,T> delegateTarget = it.next();
            if(delegateTarget.target==target){
                it.remove();
            }
        }
    }
    @Override
    public synchronized T get() {
        if(ObjectUtils.isEmpty(targets)) { return null;}
        int random= new Random().nextInt(Math.abs(HashCodeBuilder.reflectionHashCode(this)));
        return targets.get(random %(targets.size())).getTarget();
    }
    @Override
    public  List<DelegateTarget<K,T>> getTargets() {
       return targets;
    }
}
