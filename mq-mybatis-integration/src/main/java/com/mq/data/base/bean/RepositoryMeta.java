package com.mq.data.base.bean;

import com.mq.common.entity.BaseObject;
import lombok.Data;

@Data
/**
 * 数据仓库对应元信息
 */
public class RepositoryMeta<E, M> extends BaseObject {
    /**
     * 实体bean类class
     */
    private Class<E> etClassType;
    /**
     * mapper类class
     */
    private Class<M> mtClassType;
}
