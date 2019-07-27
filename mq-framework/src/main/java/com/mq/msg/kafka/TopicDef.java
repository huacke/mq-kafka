package com.mq.msg.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @version v1.0
 * @ClassName TopicDef
 * @Description 主题相关定义
 */
public class TopicDef {

    @AllArgsConstructor
    @Getter
    public enum Topic
    {
        TEST("TEST","测试","测试主题"),//测试用的主题，正常业务请勿使用

        ORDER("ORDER","订单","订单主题"),

        PAY("PAY","支付","订单主题"),

        ORDER_SYNC("ORDER_SYNC","订单同步","订单同步主题");

        private String code;
        private String name;
        private String remark;

        public static Topic getTopicByCode(String code){
            Topic tp=null;
            Topic[] values = Topic.values();
            Optional<Topic> optional = Stream.of(values).filter(it -> it.getCode().equals(code)).findFirst();
            if(optional.isPresent()){
                tp =optional.get();
            }
            return tp;
        }

    }
    @AllArgsConstructor
    @Getter
    public enum Group
    {
        TEST(Topic.TEST,"测试消费组"),//测试用的类型，正常业务请勿使用

        ORDER_CRATE_PROCESS(Topic.ORDER,"下单处理消费组"),

        PAYED_PROCESS(Topic.PAY,"支付消息处理消费组"),

        ORDER_SYNC_ERP_PROCESS(Topic.ORDER_SYNC,"订单同步ERP处理消费组");


        private Topic topic;
        private String name;
    }

}
