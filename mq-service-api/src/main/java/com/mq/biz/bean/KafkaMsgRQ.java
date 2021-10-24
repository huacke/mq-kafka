package com.mq.biz.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mq.common.entity.BaseObject;
import lombok.Data;
import java.util.Date;

/**
 * @version v1.0
 * @ClassName KafkaMsgRQ<T>
 * T:数据的class类型
 * @Description  简单的消息请求结构
 */
@Data
public class KafkaMsgRQ<T> extends BaseObject {
    //主题
    String topic;
    //消息类型
    String msgType;
    //消息子类型
    String msgSubType;
    //系统ID
    String systemID;
    //业务消息主键(eg:订单Id ,商品Id)
    Object msgKey;
    //消息数据
    @JsonInclude(JsonInclude.Include.NON_NULL)
    T msgData;
    //来源Ip
    String srcHost;
    //创建时间
    private Date createtime;


}
