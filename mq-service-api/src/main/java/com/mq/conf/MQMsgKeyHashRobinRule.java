package com.mq.conf;

import com.netflix.loadbalancer.Server;
import com.mq.feign.FeignClientInvokerIntercepter;
import com.mq.feign.rule.KeyHashRobinRule;
import com.mq.msg.BaseMessage;
import com.mq.msg.MessageBody;
import com.mq.utils.GsonHelper;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import java.util.List;

/**
 * @Description 基于业务主键hash的方式把消息发送到同一台服务上
 **/
@Slf4j
public class MQMsgKeyHashRobinRule extends KeyHashRobinRule {

    @Override
    protected Server hashKeyChoose(List<Server> servers, Object key) {
        try {

            RequestTemplate requestTemplate = FeignClientInvokerIntercepter.requestTemplateThreadLocal.get();
            if (requestTemplate != null) {
                String body = requestTemplate.requestBody().asString();
                if (!ObjectUtils.isEmpty(body)) {
                    BaseMessage message = GsonHelper.fromJson(body, BaseMessage.class);
                    if (message != null) {
                        MessageBody messageBody = message.getMessageBody();
                        if (messageBody != null) {
                            key = messageBody.getMsgKey();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("MQMsgKeyHashRobinRule hashKeyChoose  error cause by:", e);
        }
       return  super.hashKeyChoose(servers,key);
    }




}
