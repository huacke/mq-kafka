package com.data.dao;

import java.util.List;
import java.util.Map;

/**
 * 数据库接口
 */
public interface GeneratorDao {
    /**
     * 查询数据库列表
     * @param map
     * @return
     */
    List<Map<String, Object>> queryDatabases(Map<String, Object> map);
    /**
     * 查询数据库表结构列表
     * @param map
     * @return
     */
    List<Map<String, Object>> queryList(Map<String, Object> map);
    /**
     * 查询数据库表结构
     * @param map
     * @return
     */
    Map<String, String> queryTable(Map<String, Object> map);
    /**
     * 查询数据库表结构主键
     * @param map
     * @return
     */
    Map<String,String> queryPrimarykey(Map<String, Object> map);
    /**
     * 查询数据库表结构主键
     * @param map
     * @return
     */
    Map<String,String> queryTrigger(Map<String, Object> map);
    /**
     * 查询数据库表结构索引
     * @param map
     * @return
     */
    Map<String,String> queryIndex(Map<String, Object> map);
    /**
     * 查询数据库表结构外键
     * @param map
     * @return
     */
    Map<String,String> queryForeignKey(Map<String, Object> map);
    /**
     * 查询数据库表结构字段列表
     * @param map
     * @return
     */
    List<Map<String, String>> queryColumns(Map<String, Object> map);
}
