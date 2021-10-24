package com.mq.msg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mq.common.sys.SystemID;
import com.mq.msg.enums.*;
import com.mq.utils.DistributedIDGenerator;
import com.mq.utils.LocalInfo;
import lombok.Data;
import java.util.Date;

/**
 * @version v1.0
 * @ClassName Message<T>
 * B:body的class类型
 * T:数据的class类型
 * @Description  消息对象
 */
@Data
public abstract class Message<T> extends BaseMessage<T> {

    //消息ID生成器
    private final static DistributedIDGenerator idGenerator = new DistributedIDGenerator();

    //基于消息对象生成摘要，用来区分消息的唯一性
    private String digest;
    //基于消息对象生成摘要，用来在业务上区分消息的唯一性
    private String businessDigest;


    public Message() {}

    /**
     * @Description 创建消息
     * @param msgType 消息类型
     * @param systemID  系统ID
     * @param msgKey 消息键（eg:订单id，商品id等等），没有设置默认为消息ID
     * @param msgData 数据
     **/
    public Message(MsgType msgType, SystemID systemID, Object msgKey, T msgData) {
        createMessage(msgType,null,systemID,msgKey,msgData);
    }

    /**
     * @Description 创建消息
     * @param msgType 消息类型
     * @param msgType 消息类型
     * @param msgSubType 消息子类型
     * @param msgKey 消息键（eg:订单id，商品id等等），没有设置默认为消息ID
     * @param msgData 数据
     **/
    public Message(MsgType msgType, MsgSubType msgSubType, SystemID systemID, Object msgKey, T msgData) {
        createMessage(msgType,msgSubType,systemID,msgKey,msgData);
    }



    /**
     * @Description 创建消息
     * @param msgType 消息类型
     * @param msgType 消息类型
     * @param msgSubType 消息子类型
     * @param msgKey 消息键（eg:订单id，商品id等等），没有设置默认为消息ID
     * @param msgData 数据
     **/
    private void createMessage(MsgType msgType, MsgSubType msgSubType, SystemID systemID,Object msgKey, T msgData) {
        checkMsg(msgType,systemID);
        initMsg(systemID);
        setMessageType(msgType,msgSubType);
        getMessageBody().setMsgKey(null==msgKey?this.getHeader().getMsgId():msgKey);
        getMessageBody().setMsgData(msgData);
    }




    /**
     * @Description 初始化消息
     * @param  systemID 系统ID
     **/
    public void initMsg(SystemID systemID) {
        getHeader().setMsgId(idGenerator.generatorId(DistributedIDGenerator.IDTYPE.mq_kafka));
        getHeader().setSourceMsgId(getHeader().getMsgId());
        getHeader().setSourceSystem(systemID.name());
        getHeader().setSrcHost(LocalInfo.ip);
        getHeader().setCreatetime(new Date());
        setStatus(MsgStatus.READY.getCode());
    }
    /**
     * @Description 检查消息参数
     * @param msgType 消息类型
     * @param systemID  系统ID
     **/
    private void checkMsg(MsgType msgType, SystemID systemID){
        if(null ==msgType || null ==systemID){
            throw new IllegalArgumentException("消息参数错误！");
        }
    }

    /**
     * @Description 设置消息类型
     * @param msgType 消息主类型
     * @param msgSubType 消息子类型
     */
    protected void setMessageType(MsgType msgType, MsgSubType msgSubType) {
        if (null == msgSubType) {
            msgSubType = UsualMsgSubType.DEFAULT;
        }
        setMsgType(msgType.getCode());
        setMsgSubType(msgSubType.getCode());
    }
    /**
     * @Description 信息摘要
     **/
   protected abstract String  digest();

    /**
     * 获取消息体对象
     * @return
     */
     @JsonIgnore
     public MessageBody<T> getBody(){
        return getMessageBody();
    }

    /**
     * @Description 获取消息数据对象
     **/
    @JsonIgnore
    public T getMsgData() {
        return getMessageBody().getMsgData();
    }
}
