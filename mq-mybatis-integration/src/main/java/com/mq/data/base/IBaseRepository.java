package com.mq.data.base;

import com.mq.common.entity.EntiytList;
import com.mq.common.entity.page.EntryPage;
import com.mq.common.exception.BussinessException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 基础数据仓库接口定义
 * @param <E>
 */
public interface IBaseRepository<E> {
    /**
     * 新增（字段非空插入）
     *
     * @param record
     * @return record 返回实体对象
     * @throws Exception
     */
    E createItem(E record) throws BussinessException;

    /**
     * 新增（全字段）
     *
     * @param record
     * @return
     * @throws Exception
     */
    E createItemAllColumn(E record) throws BussinessException;

    /**
     * 批量插入
     *
     * @param records
     * @return
     * @throws Exception
     */
    boolean insertBatch(List<E> records) throws BussinessException;

    /**
     * 批量插入(全字段插入)
     * @param records
     * @return
     */
    int insertBatchAllColumn(Collection<? extends E> records);

    /**
     * 插入或更新（不存在插入，已存在就更新）
     *
     * @param record
     * @return
     */
    E insertOrUpdate(E record) throws BussinessException;

    /**
     * 插入或更新(全字段插入)（不存在插入，已存在就更新）
     *
     * @param record
     * @return
     */
    E insertOrUpdateAllColumn(E record) throws BussinessException;

    /**
     * 根据主键删除
     *
     * @param id
     * @return
     * @throws Exception
     */
    int deleteById(Serializable id) throws BussinessException;

    /**
     * 根据主键批量删除
     *
     * @param ids
     * @return 删除的记录数
     * @throws Exception
     */
    int deleteByIds(Collection<? extends Serializable> ids) throws BussinessException;

    /**
     * 根据主键更新（非空字段更新）
     *
     * @param record
     * @return 更新的记录数
     * @throws Exception
     */
    int updateById(E record) throws BussinessException;

    /**
     * 根据主键更新（全字段更新）
     *
     * @param record
     * @return 更新的记录数
     * @throws Exception
     */
    int updateAllColumnById(E record) throws BussinessException;

    /**
     * 检查是否存在
     *
     * @param params
     * @return
     * @throws Exception
     */
    boolean exists(Map<String, Object> params)throws BussinessException;

    /**
     * 根据主键查询
     *
     * @param id
     * @return
     * @throws Exception
     */
    E retrieveItemById(Serializable id) throws BussinessException;

    /**
     * 根据主键批量查询
     *
     * @param ids
     * @return
     */
    List<E> retrieveListByIds(Collection<? extends Serializable> ids)throws BussinessException;

    /**
     * 根据实体bean查询列表
     *
     * @param record
     * @return
     * @throws Exception
     */
    List<E> retrieveListByItem(E record) throws BussinessException;

    /**
     * 查询所有
     *
     * @return
     */
    List<E> selectAll()throws BussinessException;

    /**
     * 根据map查询列表
     *
     * @param map
     * @return
     * @throws Exception
     */
    List<E> retrieveListByMap(Map map) throws BussinessException;

    /**
     * 分页条件查询
     *
     * @param page
     * @return
     * @throws Exception
     */
    EntiytList<E> retrieveListByPage(EntryPage page) throws BussinessException;

    /**
     * 根据实体bean分页查询
     *
     * @param record
     * @return
     * @throws Exception
     */
    EntiytList<E> retrieveListPageByItem(E record, Integer pageNum, Integer pageSize) throws BussinessException;
    /**
     * 查询所有记录条数
     *
     * @return
     */
    int countAll() throws BussinessException;

    /**
     * 查询记录条数
     *
     * @param params
     * @return 存在记录条数
     */
    int countByMap(Map<String, Object> params) throws BussinessException;
}
