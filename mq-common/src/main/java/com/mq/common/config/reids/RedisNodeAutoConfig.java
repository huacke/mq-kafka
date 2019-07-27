package com.mq.common.config.reids;

import com.mq.common.utils.StringUtils;
import org.springframework.data.redis.connection.RedisNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @version v1.0
 * @ClassName RedisNodeAutoConfig
 */
public class RedisNodeAutoConfig extends ArrayList<RedisNode> {

    public void setSentinels(List<String> sentinels) {
        for (String s : sentinels) {
            if (StringUtils.isEmpty(s) || s.contains("$")) {
                continue;
            }
            String[] ss = s.split(":");
            RedisNode redisNode = new RedisNode(ss[0].trim(), Integer.parseInt(ss[1].trim()));
            add(redisNode);
        }
    }
}
