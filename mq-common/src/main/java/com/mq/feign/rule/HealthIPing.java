package com.mq.feign.rule;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.NetUtil;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;
import java.nio.ByteBuffer;

/**
 * @version v1.0
 * @ClassName HealthPing
 * @Description 服务器健康检查
 */
public class HealthIPing implements IPing {
    @Override
    public boolean isAlive(Server server) {
        boolean isHealth = true;
        try {
            NetUtil.netCat(server.getHost(), server.getPort(), false, ByteBuffer.wrap(new byte[0]));
        } catch (IORuntimeException e) {
            isHealth = false;
        }
        return isHealth;
    }
}
