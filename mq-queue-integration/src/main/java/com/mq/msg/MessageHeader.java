package com.mq.msg;

import com.mq.common.entity.BaseObject;
import lombok.Data;
import lombok.ToString;
import java.util.Date;

/**
 * @version v1.0
 * @ClassName MessageHeader
 * @Description 消息头
 */
@Data
@ToString
public class MessageHeader extends BaseObject
{
    private static final long serialVersionUID = 2618949094283096769L;
    //消息ID
    private String msgId;
    //消息来源Id
    private String sourceMsgId;
    //来源系统
    private String sourceSystem;
    //来源Ip
    private String srcHost;
    //创建时间
    private Date createtime;
}
