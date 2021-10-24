package com.mq.common.utils;

import com.mq.common.exception.BussinessException;
import com.mq.common.exception.code.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * entry vo 转换工具
 */
@Slf4j
public class EntityUtils {
    /**
     * VO转entry
     *
     * @param dtBean
     * @param entryType
     * @param <E>
     * @return
     * @throws Exception
     */
    public static <E> E toEntryBean(Object dtBean, Class<E> entryType) throws BussinessException {
        return toEntryBean(dtBean,entryType,null);
    }


    /**
     * VO转entry
     *
     * @param dtBean
     * @param entryType
     * @param <E>
     * @ignoreProperties 忽略拷贝的字段
     * @return
     * @throws Exception
     */
    public static <E> E toEntryBean(Object dtBean, Class<E> entryType,String... ignoreProperties) throws BussinessException {
        if (null == dtBean || null == entryType) return null;
        E etInstance = null;
        try {
            etInstance = entryType.newInstance();
            BeanUtils.copyProperties(dtBean, etInstance,(String[]) null);
        } catch (Exception e) {
            log.error("toEntryBean error cause by :", e);
            throw new BussinessException(ErrorCode.DATA_OPER_ERROR, e);
        }
        return etInstance;
    }



    /**
     * entry转vo
     *
     * @param etBean
     * @param dtType
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T toDtBean(Object etBean, Class<T> dtType) throws BussinessException {
        if (null == etBean || null == dtType) return null;
        T dtInstance = null;
        try {
            dtInstance = dtType.newInstance();
            BeanUtils.copyProperties(etBean, dtInstance);
        } catch (Exception e) {
            log.error("toDtBean error cause by :", e);
            throw new BussinessException(ErrorCode.DATA_OPER_ERROR, e);
        }
        return dtInstance;
    }

    /**
     * vo list 转 entry list
     *
     * @param dtBeans
     * @param entryType
     * @param <E>
     * @return
     * @throws BussinessException
     */
    public static <E> List<E> toEntryListBean(List dtBeans, Class<E> entryType) throws BussinessException {
        List<E> etBeans = null;
        try {
            if (!CollectionUtils.isEmpty(dtBeans)) {
                etBeans = new ArrayList<E>();
                for (Object dtBean : dtBeans) {
                    E etInstance = entryType.newInstance();
                    BeanUtils.copyProperties(dtBean, etInstance);
                    etBeans.add(etInstance);
                }
            }
        } catch (Exception e) {
            log.error("toEntryListBean error cause by :", e);
            throw new BussinessException(ErrorCode.DATA_OPER_ERROR, e);
        }
        return etBeans;
    }


    /**
     * vo list 转 entry list
     *
     * @param dtBeans
     * @param entryType
     * @param <E>
     * @param ignoreProperties 忽略拷贝的字段
     * @return
     * @throws BussinessException
     */
    public static <E> List<E> toEntryListBean(List dtBeans, Class<E> entryType,String ... ignoreProperties) throws BussinessException {
        List<E> etBeans = null;
        try {
            if (!CollectionUtils.isEmpty(dtBeans)) {
                etBeans = new ArrayList<E>();
                for (Object dtBean : dtBeans) {
                    E etInstance = entryType.newInstance();
                    BeanUtils.copyProperties(dtBean, etInstance,ignoreProperties);
                    etBeans.add(etInstance);
                }
            }
        } catch (Exception e) {
            log.error("toEntryListBean error cause by :", e);
            throw new BussinessException(ErrorCode.DATA_OPER_ERROR, e);
        }
        return etBeans;
    }






    /**
     * entry list 转 vo list
     *
     * @param etBeans
     * @param dtType
     * @param <T>
     * @return
     * @throws BussinessException
     */
    public static <T> List<T> toDtListBean(List etBeans, Class<T> dtType) throws BussinessException {
        return toDtListBean(etBeans,dtType,null);
    }



    /**
     * entry list 转 vo list
     *
     * @param etBeans
     * @param dtType
     * @param <T>
     * @param   ignoreProperties 忽略拷贝的字段
     * @return
     * @throws BussinessException
     */
    public static <T> List<T> toDtListBean(List etBeans, Class<T> dtType,String ... ignoreProperties) throws BussinessException {
        List<T> dtBeans = null;
        try {
            if (!CollectionUtils.isEmpty(etBeans)) {
                dtBeans = new ArrayList<T>();
                for (Object etBean : etBeans) {
                    T dtInstance = dtType.newInstance();
                    BeanUtils.copyProperties(etBean, dtInstance,ignoreProperties);
                    dtBeans.add(dtInstance);
                }
            }
        } catch (Exception e) {
            log.error("toDtListBean error cause by :", e);
            throw new BussinessException(ErrorCode.DATA_OPER_ERROR, e);
        }
        return dtBeans;
    }


}
