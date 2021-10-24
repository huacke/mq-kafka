package com.mq.data.base;

import org.apache.ibatis.annotations.Param;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * 基础 mapper
 * @author：  huacke
 */
public interface BaseMapper<E> {

	/**
	 *插入(全字段)
	 * @param record
	 * @return
	 */
	int insert(E record);
	/**
	 * 非空插入（非空字段）
	 * @param record
	 * @return
	 */
	int insertSelective(E record);
	/**
	 * 批量插入
	 * @param records
	 * @return
	 */
	 int insertBatchAllColumn(@Param("coll") Collection<? extends E> records);
	/**
	 * 根据主键删除
	 * @param id
	 * @return
	 */
	int deleteByPrimaryKey(Serializable id);
	/**
	 * 根据主键id列表删除
	 * @param
	 * @return
	 */
	int deleteByIds(@Param("coll")Collection<? extends Serializable> ids);
	/**
	 * 根据主键更新（全字段）
	 * @param record
	 * @return
	 */
	int updateByPrimaryKey(E record);

	/**
	 * 根据主键更新(非空字段)
	 * @param record
	 * @return
	 */
	int updateByPrimaryKeySelective(E record);
	/**
	 * 根据主键查询
	 * @param id
	 * @return
	 */
	E selectByPrimaryKey(Serializable id);

	/**
	 * 根据主键id批量查询
	 * @param ids
	 * @return
	 */
	List<E> selectBatchIds(@Param("coll") Collection<? extends Serializable> ids);
	/**
	 * 通过map参数查询
	 * @param params
	 * @return
	 */
	List<E> selectListByMap(Map<String,Object> params);
	/**
	 * 查询记录条数
	 * @param params
	 * @return 存在记录条数
	 */
	int countByMap(Map<String,Object> params);

}
