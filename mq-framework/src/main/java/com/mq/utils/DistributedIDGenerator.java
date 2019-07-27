package com.mq.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version v1.0
 * @ClassName DistributedIDGenerator
 * @Description ID生成器
 */
public class DistributedIDGenerator {

    private final AtomicInteger num = new AtomicInteger(0);

    public  String generatorId(IDTYPE idtype) {

        if (num.get() > 100000) {
            num.set(0);
        }
        SimpleDateFormat sim = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        StringBuilder c = new StringBuilder();
        c.append(num.addAndGet(1));
        for (int i = c.length(); i < 5; i++) {
            c.insert(0, "0");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(idtype.name());
        sb.append("-");
        sb.append(sim.format(new Date()));
        sb.append("-");
        sb.append(LocalInfo.mac );
        sb.append("-");
        sb.append(ThreadUtil.processId);
        sb.append("-");
        sb.append(c.toString());
        return sb.toString();
    }
    @AllArgsConstructor
    @Getter
    public enum IDTYPE {
        mq_kafka("kafkaMQ消息"),
        mq_kafka_request_log("kafkaMQ消息请求日志");
        String name;
    }
}
