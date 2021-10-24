package com.mq.consumer.kafka.biz.bean;

import com.mq.common.entity.BaseObject;
import com.mq.msg.kafka.TopicDef;
import lombok.Data;
/**
 * @version v1.0
 * @ClassName TopicGroup
 * @Description 主题和消费组映射
 */
@Data
public class TopicGroup extends BaseObject {

    public TopicGroup(TopicDef.Topic topic, TopicDef.Group group, int consumerSize) {
        this.topic = topic;
        this.group = group;
        this.consumerSize = consumerSize>0?consumerSize:this.consumerSize;
    }
    //主题
    private TopicDef.Topic topic;
    //消费组 如果是一个topic对应多个分组属于订阅模式
    private TopicDef.Group group;
    /*
     * 消费者数量(每个分组下面有多少个消费者)
     * 因为kafka一个分区只能同时有一个消费者消费，建议设置成分区数量
     * 公式：consumerSize = topic分区数量
     * */
    private  int consumerSize=1;
}
