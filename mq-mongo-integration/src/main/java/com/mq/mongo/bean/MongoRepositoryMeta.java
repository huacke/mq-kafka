package com.mq.mongo.bean;

import com.mq.common.entity.BaseObject;
import lombok.Data;

@Data
/**
 * 数据仓库对应元信息
 */
public class MongoRepositoryMeta<M> extends BaseObject {
    /**
     * mapper类class
     */
    private Class<M> mtClassType;
}
