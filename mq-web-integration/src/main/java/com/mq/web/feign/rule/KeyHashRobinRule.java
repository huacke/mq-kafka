package com.mq.web.feign.rule;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.WeightedResponseTimeRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.builder.HashCodeBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Description 基于业务主键hash的方式把消息发送到同一台服务上
 **/
@Slf4j
public class KeyHashRobinRule extends AbstractLoadBalancerRule {

    protected WeightedResponseTimeRule defaultRule = new WeightedResponseTimeRule();

    private static byte[] lock =new byte[0];
    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }

    @Override
    public Server choose(Object key) {

        List<Server> aliveservers = this.getLoadBalancer().getReachableServers();
        if (aliveservers.isEmpty()) {
            return null;
        }
        if (aliveservers.size() == 1) {
            Server server = aliveservers.get(0);
            return server;
        }
        return hashKeyChoose(aliveservers, key);
    }

    /**
     * hash 映射服务器
     * @param servers
     * @param key
     * @return
     */
    protected Server hashKeyChoose(List<Server> servers, Object key) {
        Server server = chooseServer(servers, key);
        if (null == server) {
            setDefaultRuleLoadBalancer();
            server = defaultRule.choose(key);
        }
        return server;
    }

    /**
     * 根据hash算法选择状态健康的服务器返回
     * @param servers
     * @param key
     * @return
     */
    private Server chooseServer(List<Server> servers, Object key) {
        Server server = null;
        try{
            if (!servers.isEmpty()) {
                if (null == key) {
                    key = System.currentTimeMillis();
                }
                List<Server> srvs = new ArrayList<>(servers);
                int hashCode = Math.abs(HashCodeBuilder.reflectionHashCode(key));
                int index = hashCode % servers.size();
                server = servers.get(index);
                if (server != null) {
                    if (server.isAlive() && server.isReadyToServe()) {
                        return server;
                    } else {
                        Iterator<Server> srvIterator = srvs.iterator();
                        while (srvIterator.hasNext()) {
                           Server srv = srvIterator.next();
                            if (server.isAlive() && server.isReadyToServe()) {
                                server =srv;
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("KeyHashRobinRule chooseServer  error cause by :",e);
        }
        return server;
    }


    /**
     * 设置默认的规则的均衡器
     */
    private  void setDefaultRuleLoadBalancer() {
        synchronized (lock) {
            if (null == defaultRule.getLoadBalancer()) {
                defaultRule.setLoadBalancer(this.getLoadBalancer());
            }
        }
    }






}
