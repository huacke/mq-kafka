package com.mq.mongo.service.impl;

import com.mq.mongo.dao.BaseMongoDao;
import com.mq.mongo.service.IMongoService;
import com.mq.common.entity.EntiytList;
import com.mq.common.entity.page.Page;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @version v1.0
 * @ClassName MongoService
 * @Description 基础mongo服务类
 */
public abstract class BaseMongoService<T, ID extends Serializable> implements IMongoService<T, ID>, InitializingBean{

    private BaseMongoDao mongoDao;

   protected abstract BaseMongoDao populateDao();

    @Override
    public T insert(T t) {
        return (T)mongoDao.insert(t);
    }

    @Override
    public  Collection<T> insertAll(Collection<? extends T> datas) {
        return (Collection<T>)mongoDao.insert(datas);
    }

    @Override
    public  T save(T t) {
        return (T)mongoDao.save(t);
    }

    @Override
    public void deleteByCondition(T t) {
        mongoDao.deleteByCondition(t);
    }

    @Override
    public boolean deleteById(ID id) {
       return mongoDao.deleteById(id);
    }

    @Override
    public boolean updateById(ID id, T t) { return mongoDao.updateById(id, t); }

    @Override
    public boolean updateById(ID id, Update update) {
       return mongoDao.updateById(id, update);
    }
    @Override
    public long update(Query query, Update update) {
        return mongoDao.update(query, update);
    }

    @Override
    public EntiytList findByPage(Page<T> page, Query query) {
        return mongoDao.findByPage(page,query);
    }

    @Override
    public List<T> findByCondition(T t) {
        return mongoDao.findByCondition(t);
    }

    @Override
    public List<T> find(Query query) {
        return mongoDao.find(query);
    }
    @Override
    public T findOne(Query query) { return (T) mongoDao.findOne(query); }

    @Override
    public T findById(ID id) {
        return (T) mongoDao.findById(id);
    }

    @Override
    public T findById(ID id, String collectionName) {
        return (T) mongoDao.findById(id, collectionName);
    }

    @Override
    public long count(Query query) {
        return mongoDao.count(query);
    }

    @Override
    public MongoTemplate getMongoTemplate() {
        return mongoDao.getMongoTemplate();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        BaseMongoDao dao = populateDao();
        Assert.notNull(dao,"dao instance not null");
        this.mongoDao =dao;
    }
}
