package com.mq.data.base.impl;

import java.io.Serializable;
import java.util.*;
import com.mq.common.entity.EntiytList;
import com.mq.common.entity.page.EntryPage;
import com.mq.common.entity.page.Page;
import com.mq.common.exception.BussinessException;
import com.mq.common.exception.code.ErrorCode;
import com.mq.common.pagehelper.PageHelper;
import com.mq.common.utils.FiledUtils;
import com.mq.common.utils.ReflectionUtils;
import com.mq.data.base.BaseMapper;
import com.mq.data.base.IBaseRepository;
import com.mq.data.base.utils.TableHelper;
import com.mq.utils.ExceptionFormatUtil;
import com.mq.utils.GsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基础数据仓库
 *
 * @param <E> 实体bean class
 * @param <M> mapper类class
 */
@SuppressWarnings("ALL")
@Slf4j
public class BaseRepository<E, M extends BaseMapper> implements IBaseRepository<E> {


    private String repositoryName=this.getClass().getSimpleName();

    @Autowired
    protected   M baseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public E createItem(E record) throws BussinessException {
        try {
            baseMapper.insertSelective(record);
            return record;
        } catch (Exception e) {
            log.error(repositoryName+":[createItem],record={},error={}", record, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_CREATE_ERROR, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public E createItemAllColumn(E record) throws BussinessException {
        try {
            baseMapper.insert(record);
            return record;
        } catch (Exception e) {
            log.error(repositoryName+":[createItemAllColumn],record={},error={}", record, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_CREATE_ERROR, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertBatch(List<E> records) throws BussinessException {
        try {
            for (E record : records) {
                baseMapper.insertSelective(record);
            }
        } catch (Exception e) {
            log.error(repositoryName+":[insertBatch],record={},error={}", records, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_CREATE_ERROR, e);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBatchAllColumn(Collection<? extends E> records) {
        int rs = 0;
        try {
            rs = baseMapper.insertBatchAllColumn(records);
        } catch (Exception e) {
            log.error(repositoryName+":[insertBatchAllColumn],record={},error={}", records, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_CREATE_ERROR, e);
        }
        return rs;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public E insertOrUpdate(E record) throws BussinessException {
        try {
            Object tablePrimaryKey = TableHelper.getTablePrimaryKey(record);
            //主键值为空：新增
            if (null == tablePrimaryKey) {
                return createItem(record);
            } else {
                //更新
                int rs = updateById(record);
                if (rs <= 0) {
                    return createItem(record);
                }
                return record;
            }
        } catch (Exception e) {
            log.error(repositoryName+":[insertOrUpdate],record={},error={}", record, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_CREATE_ERROR, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public E insertOrUpdateAllColumn(E record) throws BussinessException {
        try {
            Object tablePrimaryKey = TableHelper.getTablePrimaryKey(record);
            //主键值为空：新增
            if (null == tablePrimaryKey) {
                return createItemAllColumn(record);
            } else {
                //更新
                int r = updateAllColumnById(record);
                if (r <= 0) {
                    //更新失败则新建
                    return createItemAllColumn(record);
                }
                return record;
            }
        } catch (Exception e) {
            log.error(repositoryName+":[insertOrUpdateAllColumn],record={},error={}", record, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_CREATE_ERROR, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Serializable id) throws BussinessException {

        try {
            return baseMapper.deleteByPrimaryKey(id);
        } catch (Exception e) {
            log.error(repositoryName+":[deleteById],record={},error={}", id, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_DELETE_ERROR, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Collection<? extends Serializable> ids) throws BussinessException {
        try {
            return baseMapper.deleteByIds(ids);
        } catch (Exception e) {
            log.error(repositoryName+":[deleteByIds],record={},error={}", ids, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_DELETE_ERROR, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(E record) throws BussinessException {
        try {
            return baseMapper.updateByPrimaryKeySelective(record);
        } catch (Exception e) {
            log.error(repositoryName+":[updateById],record={},error={}", record, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_UPDATE_ERROR, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAllColumnById(E record) throws BussinessException {
        try {
            return baseMapper.updateByPrimaryKey(record);
        } catch (Exception e) {
            log.error(repositoryName+":[updateAllColumnById],record={},error={}", record, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_UPDATE_ERROR, e);
        }
    }

    @Override
    public boolean exists(Map<String, Object> params) throws BussinessException {
        try {
            return countByMap(params) > 0;
        } catch (Exception e) {
            log.error(repositoryName+":[exists],record={},error={}", params, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_QUERY_ERROR, e);
        }
    }

    @Override
    public E retrieveItemById(Serializable id) throws BussinessException {
        try {
            return (E) baseMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            log.error(repositoryName+":[retrieveItemById],record={},error={}", id, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_QUERY_ERROR, e);
        }
    }

    @Override
    public List<E> retrieveListByIds(Collection<? extends Serializable> ids) throws BussinessException {
        try {
            return baseMapper.selectBatchIds(ids);
        } catch (Exception e) {
            log.error(repositoryName+":[retrieveListByIds],record={},error={}", ids, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_QUERY_ERROR, e);
        }
    }

    @Override
    public List<E> retrieveListByItem(E record) throws BussinessException {
        try {
            return retrieveListByMap(FiledUtils.transfer2Map(record));
        } catch (Exception e) {
            log.error(repositoryName+":[retrieveListByItem],record={},error={}", record, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_QUERY_ERROR, e);
        }
    }

    @Override
    public List<E> selectAll() throws BussinessException {
        try {
            return baseMapper.selectListByMap(null);
        } catch (Exception e) {
            log.error(repositoryName+":[selectAll],error={}", ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_QUERY_ERROR, e);
        }
    }

    @Override
    public List<E> retrieveListByMap(Map map) throws BussinessException {
        try {
            return baseMapper.selectListByMap(map);
        } catch (Exception e) {
            log.error(repositoryName+":[retrieveListByMap],record={},error={}", map, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_QUERY_ERROR, e);
        }
    }

    @Override
    public EntiytList<E> retrieveListByPage(EntryPage page) throws BussinessException {
        try {
            Map<String, Object> params = page.getMap();
            Class<E> clazzType = ReflectionUtils.getSuperClassGenricType(this.getClass());
            E paramsBean = GsonHelper.fromJson(GsonHelper.toJson(params), clazzType);
            return retrieveListPageByItem(paramsBean, page.getPageNum(), page.getPageSize());
        } catch (Exception e) {
            log.error(repositoryName+":[retrieveListByPage],record={},error={}", page, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_QUERY_ERROR, e);
        }
    }

    @Override
    public EntiytList<E> retrieveListPageByItem(E record, Integer pageNum, Integer pageSize) throws BussinessException {
        try {
            int pNum = pageNum == null ? 1 : pageNum.intValue();
            int pSize = pageSize == null ? 20 : pageSize.intValue();
            PageHelper.startPage(pNum, pSize);
            Map<String, Object> params = FiledUtils.transfer2Map(record);
            Page<E> page = (Page<E>) baseMapper.selectListByMap(params);
            EntiytList etEntryList = new EntiytList(page);
            page.getPage(etEntryList);
            return etEntryList;
        } catch (Exception e) {
            log.error(repositoryName+":[retrieveListPageByItem],record={},error={}", record, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_QUERY_ERROR, e);
        }
    }

    @Override
    public int countAll() throws BussinessException {
        try {
            return countByMap(null);
        } catch (Exception e) {
            log.error(repositoryName+":[countAll],error={}", ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_QUERY_ERROR, e);
        }

    }

    @Override
    public int countByMap(Map<String, Object> params) throws BussinessException {
        try {
            return baseMapper.countByMap(params);
        } catch (Exception e) {
            log.error(repositoryName+":[countByMap],record={},error={}", params, ExceptionFormatUtil.getTrace(e, 8));
            throw new BussinessException(ErrorCode.DB_QUERY_ERROR, e);
        }
    }

}
