package com.mq.mongo.dao;

import com.mq.common.entity.EntiytList;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.io.Serializable;
import java.util.List;

/**
 * @version v1.0
 * @ClassName MongoDao
 * @Description 基础MongoDa
 */
public class BaseMongoDao<T,ID extends Serializable> extends MongoDaoSupport<T,ID> {

    /**
     * 通过条件查询,查询分页结果
     *
     * @param page
     * @param query
     * @return
     */
    public EntiytList findByPage(com.mq.common.entity.page.Page<T> page, Query query) {
        EntiytList entiytList = new EntiytList();
        //如果没有条件 则所有全部
        query = query == null ? new Query(Criteria.where("_id").exists(true)) : query;
        long count = this.count(query);
        PageRequest pageRequest = PageRequest.of(page.getPageNum(), page.getPageSize());
        query.with(pageRequest);
        entiytList.setPageSize(page.getPageSize());
        // 总数
        entiytList.setTotal((int) count);
        int currentPage = page.getPageNum();
        int pageSize = page.getPageSize();
        query.skip((currentPage - 1) * pageSize).limit(pageSize);
        List<T> rows = this.find(query);
        entiytList.setRows(rows);
        return entiytList;
    }

}
