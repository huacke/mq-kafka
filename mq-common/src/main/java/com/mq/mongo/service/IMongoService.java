package com.mq.mongo.service;

import com.mq.common.entity.EntiytList;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @version v1.0
 * @ClassName MongoService
 * @Description 基础mongo服务类
 */
public interface IMongoService<T,ID extends Serializable> {

    /**
     * 插入
     * @param t
     */
    public T insert(T t);
    /**
     * 批量插入
     * @param datas
     */
    public Collection<T> insertAll(Collection<? extends T> datas);
    /**
     * 保存
     * @param t
     */
    public  T save(T t);

    /**
     * 根据对象的属性删除
     * @param t
     */
    public void deleteByCondition(T t);

    /**
     * 根据对象的主键删除
     * @param id
     */
    public boolean deleteById(ID id);


    /**
     * 根据id进行更新
     * @param id
     * @param t
     */
    public boolean updateById(ID id, T t);


    /**
     * 根据id进行更新
     * @param id
     * @param update
     */
    public boolean updateById(ID id,Update update);



    /**
     * 通过条件查询更新数据
     *
     * @param query
     * @param update
     * @return
     */
    public long update(Query query, Update update) ;



    /**
     * 通过条件查询,查询分页结果
     *
     * @param page
     * @param query
     * @return
     */
    public EntiytList findByPage(com.mq.common.entity.page.Page<T> page, Query query);


    /**
     * 根据对象的属性查询
     * @param t
     * @return
     */
    public List<T> findByCondition(T t);


    /**
     * 通过条件查询实体(集合)
     *
     * @param query
     */
    public List<T> find(Query query) ;

    /**
     * 通过一定的条件查询一个实体
     *
     * @param query
     * @return
     */
    public T findOne(Query query) ;

    /**
     * 通过ID获取记录
     *
     * @param id
     * @return
     */
    public T findById(ID id) ;

    /**
     * 通过ID获取记录,并且指定了集合名(表的意思)
     *
     * @param id
     * @param collectionName
     *            集合名
     * @return
     */
    public T findById(ID id, String collectionName) ;

    /**
     * 求数据总和
     * @param query
     * @return
     */
    public long count(Query query);

    /**
     * 获取MongoDB模板操作
     * @return
     */
    public MongoTemplate getMongoTemplate();

}
