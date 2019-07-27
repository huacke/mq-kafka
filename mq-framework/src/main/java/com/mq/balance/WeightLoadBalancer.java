package com.mq.balance;

import java.math.BigInteger;

/**
 * @version v1.0
 * @Description 加权轮询负载均衡器
 */
public class WeightLoadBalancer<K,T> extends AbstractLoadBalancer<K,T>{

    private int currentIndex;
    private int totalTarget;
    private int currentWeight;
    private int maxWeight;
    private int gcdWeight;

    public WeightLoadBalancer() {
        super();
    }

    @Override
    public synchronized void addTarget(T target) {
        throw  new UnsupportedOperationException("not supported operation!");
    }

    public synchronized void addTarget(T target, int weight){
        targets.add(new DelegateTarget<>(target, weight));
        reloadWeightParams();
    }

    @Override
    public synchronized void removeTarget(T target){
        super.removeTarget(target);
        reloadWeightParams();
    }
    @Override
    public synchronized T get() {
        if(targets.isEmpty()){
            return null;
        }
        if(targets.size()==1){
            return targets.get(0).target;
        }
        while (true) {
            currentIndex = (currentIndex + 1) % totalTarget;
            if (currentIndex == 0) {
                currentWeight = currentWeight - gcdWeight;
                if (currentWeight <= 0) {
                    currentWeight = maxWeight;
                    if(currentWeight == 0) {
                        return null;
                    }
                }
            }
            if(targets.get(currentIndex).getWeight() >= currentWeight) {
                return targets.get(currentIndex).target;
            }
        }
    }

    protected void reloadWeightParams(){
        totalTarget = targets.size();
        currentIndex = totalTarget - 1;
        maxWeight = maxWeight();
        gcdWeight = targetGcd();
    }

    /**
     * 返回所有服务器的权重的最大公约数
     *
     * @return
     */
    private int targetGcd() {
        int comDivisor = 0;
        for (int i = 0; i < totalTarget - 1; i++) {
            if (comDivisor == 0) {
                comDivisor = gcd(targets.get(i).getWeight(), targets.get(i + 1).getWeight());
            } else {
                comDivisor = gcd(comDivisor, targets.get(i + 1).getWeight());
            }
        }
        return comDivisor;
    }

    /**
     * 获得服务器中的最大权重
     *
     * @return
     */
    private int maxWeight() {
        if(targets.isEmpty()){
            return 0;
        }
        int max = targets.get(0).getWeight();
        int tmp;
        for (int i = 1; i < totalTarget; i++) {
            tmp = targets.get(i).getWeight();
            if (max < tmp) {
                max = tmp;
            }
        }
        return max;
    }

    /**
     * 求两个数的最大公约数 4和6最大公约数是2
     *
     * @param num1
     * @param num2
     * @return
     */
    private int gcd(int num1, int num2) {
        BigInteger i1 = new BigInteger(String.valueOf(num1));
        BigInteger i2 = new BigInteger(String.valueOf(num2));
        return i1.gcd(i2).intValue();
    }


}
