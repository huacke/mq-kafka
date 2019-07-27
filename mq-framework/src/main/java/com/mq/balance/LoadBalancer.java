package com.mq.balance;

import lombok.Data;

import java.util.List;
/**
 * @Description 负载均衡器
 **/
public interface LoadBalancer<K,T> {

    public  void addTarget(T target);

    public  void removeTarget(T target);

    public  T get();

    public List<DelegateTarget<K,T>> getTargets();

    @Data
    public static class DelegateTarget<K, T> {
        protected K key;
        protected T target;
        protected int weight=0;

        public DelegateTarget(T target, int weight) {
            this.target = target;
            this.weight = weight;
        }

        public DelegateTarget(K key, T target) {
            this.target = target;
            this.key = key;
        }

        public DelegateTarget(T target) {
            this.target = target;
        }
    }
}
