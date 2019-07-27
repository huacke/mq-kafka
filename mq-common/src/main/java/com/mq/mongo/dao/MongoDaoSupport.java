package com.mq.mongo.dao;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mq.mongo.annotation.QueryFieldType;
import com.mq.mongo.annotation.QueryType;
import com.mq.common.utils.ReflectionUtils;
import com.mq.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
/**
 * MongoDB通用Dao抽象实现
 * @param <T>
 */
public abstract class MongoDaoSupport<T,ID extends Serializable> implements IMongoDao<T,ID> {

    @Autowired(required = true)
    private MongoTemplate template;

    @Override
    public MongoTemplate getMongoTemplate() {
        return template;
    }

    @Override
    public T insert(T t) {
        return template.insert(t);
    }

    @Override
    public  Collection<T> insertAll(Collection<? extends T> datas) {
        return template.insertAll(datas);
    }

    @Override
    public T save(T t) {
       return template.save(t);
    }
    /**
     * 根据对象的属性删除
     *
     * @param t
     */
    @Override
    public void deleteByCondition(T t) {
        Query query = buildBaseQuery(t);
        getMongoTemplate().remove(query, getEntityClass());
    }

    @Override
    public boolean deleteById(ID id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        DeleteResult delted = getMongoTemplate().remove(query, getEntityClass());
        return delted.getDeletedCount()>0l;
    }
    /**
     * 根据id进行更新
     *
     * @param id
     * @param t
     */
    @Override
    public boolean updateById(ID id, T t) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        Update update = buildBaseUpdate(t);
        return update(query, update)>0l;
    }

    @Override
    public boolean updateById(ID id, Update update) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        return update(query, update)>0l;
    }

    /**
     * 通过条件查询更新数据
     *
     * @param query
     * @param update
     * @return ModifiedCount
     */
    @Override
    public long update(Query query, Update update) {
        UpdateResult updateResul = getMongoTemplate().updateMulti(query, update, this.getEntityClass());
        return updateResul.getModifiedCount();
    }

    /**
     * 根据对象的属性查询
     *
     * @param t
     * @return
     */
    @Override
    public List<T> findByCondition(T t) {
        Query query = buildBaseQuery(t);
        return getMongoTemplate().find(query, getEntityClass());
    }

    /**
     * 通过条件查询实体(集合)
     *
     * @param query
     * @return
     */
    @Override
    public List<T> find(Query query) {
        return getMongoTemplate().find(query, this.getEntityClass());
    }

    /**
     * 通过一定的条件查询一个实体
     *
     * @param query
     * @return
     */
    @Override
    public T findOne(Query query) {
        return getMongoTemplate().findOne(query, this.getEntityClass());
    }

    /**
     * 通过ID获取记录
     *
     * @param id
     * @return
     */
    @Override
    public T findById(ID id) {
        return getMongoTemplate().findById(id, this.getEntityClass());
    }

    /**
     * 通过ID获取记录,并且指定了集合名(表的意思)
     *
     * @param id
     * @param collectionName
     * @return
     */
    @Override
    public T findById(ID id, String collectionName) {
        return getMongoTemplate().findById(id, this.getEntityClass(), collectionName);
    }

    /**
     * 求数据总和
     *
     * @param query
     * @return
     */
    public long count(Query query) {
        return getMongoTemplate().count(query, this.getEntityClass());
    }

    /**
     * 根据vo构建查询条件Query
     *
     * @param t
     * @return
     */
    private Query buildBaseQuery(T t) {
        Query query = new Query();

        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(t);
                if (value != null&&StringUtil.isNotEmpty(value.toString())) {
                    QueryFieldType queryFieldType = field.getAnnotation(QueryFieldType.class);
                    if (null==queryFieldType) {
                        query.addCriteria(QueryType.EQUALS.buildCriteria(queryFieldType, field, value));
                    }else{
                        query.addCriteria(queryFieldType.type().buildCriteria(queryFieldType, field, value));
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return query;
    }

    /**
     * 根据vo构建更新条件Query
     *
     * @param t
     * @return
     */
    private Update buildBaseUpdate(T t) {

        Update update = new Update();
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(t);
                if (value != null) {
                    update.set(field.getName(), value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return update;
    }
    /**
     * 获取需要操作的实体类class
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Class<T> getEntityClass() {
        return ReflectionUtils.getSuperClassGenricType(getClass());
    }
}
