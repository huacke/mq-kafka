package com.mq.common.pagehelper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mq.common.entity.page.Page;
import com.walton.shop.pagehelper.parser.SqlParser;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Mybatis - 通用分页拦截器
 *
 * @version 3.2.2
 * @url http://git.oschina.net/free/Mybatis_PageHelper
 */
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PageHelper implements Interceptor {
    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    public static final ReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();
    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class,
            DEFAULT_OBJECT_FACTORY,
            DEFAULT_OBJECT_WRAPPER_FACTORY,
            DEFAULT_REFLECTOR_FACTORY);

    public static final SqlParser sqlParser = new SqlParser();

    private static class NullObject {
    }

    /**
     * 反射对象，增加对低版本Mybatis的支持
     *
     * @param object
     * @return
     */
    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY,DEFAULT_REFLECTOR_FACTORY);
    }

    /**
     * 解析 - 去掉order by 语句
     */
//    private static class UnParser extends NodeToString {
//        private static final SQLParser PARSER = new SQLParser();
//
//        public String removeOrderBy(String sql) throws StandardException {
//            StatementNode stmt = PARSER.parseStatement(sql);
//            return toString(stmt);
//        }
//
//        @Override
//        protected String orderByList(OrderByList node) throws StandardException {
//            return "";
//        }
//    }

    //SQL反解析
//    private static final UnParser UNPARSER = new UnParser();
    //SQL缓存
    private static final Cache<Integer, String> COUNT_CACHE = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(30, TimeUnit.MINUTES).build();

    private static final String BOUND_SQL = "sqlSource.boundSql.sql";

    private static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<Page>();

    private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<ResultMapping>(0);

    //数据库方言
    private static String dialect = "";
    //RowBounds参数offset作为PageNum使用 - 默认不使用
    private static boolean offsetAsPageNum = false;
    //RowBounds是否进行count查询 - 默认不查询
    private static boolean rowBoundsWithCount = false;

    /**
     * 开始分页
     *
     * @param pageNum
     * @param pageSize
     */
    public static void startPage(int pageNum, int pageSize) {
        startPage(pageNum, pageSize, true);
    }

    /**
     * 开始分页
     *
     * @param pageNum
     * @param pageSize
     */
    public static void startPage(int pageNum, int pageSize, boolean count) {
        LOCAL_PAGE.set(new Page(pageNum, pageSize, count));
    }

    /**
     * 获取分页参数
     *
     * @param rowBounds
     * @return
     */
    private Page getPage(RowBounds rowBounds) {
        Page page = LOCAL_PAGE.get();
        //移除本地变量
        LOCAL_PAGE.remove();

        if (page == null) {
            if (offsetAsPageNum) {
                page = new Page(rowBounds.getOffset(), rowBounds.getLimit(), rowBoundsWithCount);
            } else {
                page = new Page(rowBounds, rowBoundsWithCount);
            }
        }
        return page;
    }

    /**
     * Mybatis拦截器方法
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Object[] args = invocation.getArgs();
        RowBounds rowBounds = (RowBounds) args[2];
        if (LOCAL_PAGE.get() == null && rowBounds == RowBounds.DEFAULT) {
            return invocation.proceed();
        } else {
            //忽略RowBounds-否则会进行Mybatis自带的内存分页
            args[2] = RowBounds.DEFAULT;
            MappedStatement ms = (MappedStatement) args[0];
            Object parameterObject = args[1];
            BoundSql boundSql = ms.getBoundSql(parameterObject);

            //分页信息
            Page page = getPage(rowBounds);
            //创建一个新的MappedStatement
            MappedStatement qs = newMappedStatement(ms, new BoundSqlSqlSource(boundSql));
            //将参数中的MappedStatement替换为新的qs，防止并发异常
            args[0] = qs;
            MetaObject msObject = forObject(qs);
            String sql = (String) msObject.getValue(BOUND_SQL);
            //简单的通过total的值来判断是否进行count查询
            if (page.isCount()) {
                //求count - 重写sql
                msObject.setValue(BOUND_SQL, getCountSql(sql));
                //查询总数
                Object result = invocation.proceed();
                //设置总数
                page.setTotal((Integer) ((List) result).get(0));
                if (page.getTotal() == 0) {
                    return page;
                }
            }
            //分页sql - 重写sql
            msObject.setValue(BOUND_SQL, getPageSql(sql, page));
            //恢复类型
            msObject.setValue("resultMaps", ms.getResultMaps());
            //执行分页查询
            Object result = invocation.proceed();
            //得到处理结果
            page.addAll((List) result);
            //返回结果
            return page;
        }
    }

    /**
     * 获取总数sql - 如果要支持其他数据库，修改这里就可以
     *
     * @param sql
     * @return
     */
    private String getCountSql(final String sql) {
//        try {
//            return COUNT_CACHE.get(sql.hashCode(), new Callable<String>() {
//                public String call() throws Exception {
//                	String strSQL=sql;
//                	//added by shuiyan on 20160412
//                	//order by 子句要精确判断，否则会无辜杀伤带有order的字段，使用空格替换Tab空格
//                	strSQL = strSQL.replace("	", " ");
//                	strSQL = strSQL.replace("\r\n", " ");
//                	strSQL = strSQL.replace("\n", " ");
//                    if (strSQL.toUpperCase().contains(" ORDER ")) {
//                        return "select count(0) from (" + UNPARSER.removeOrderBy(sql) + ") tmp_count";
//                    } else {
//                        return "select count(0) from (" + sql + ") tmp_count";
//                    }
//                }
//            });
//        } catch (Exception e) {
//            return "select count(0) from (" + sql + ") tmp_count";
//        }

        //added by ShuiYan on 20160414 for order by issue
        return sqlParser.getSmartCountSql(sql);

    }

    /**
     * 获取分页sql - 如果要支持其他数据库，修改这里就可以
     *
     * @param sql
     * @param page
     * @return
     */
    private String getPageSql(String sql, Page page) {
        StringBuilder pageSql = new StringBuilder(200);
        if ("mysql".equals(dialect)) {
            pageSql.append(sql);
            pageSql.append(" limit " + page.getStartRow() + "," + page.getPageSize());
        } else if ("hsqldb".equals(dialect)) {
            pageSql.append(sql);
            pageSql.append(" LIMIT " + page.getPageSize() + " OFFSET " + page.getStartRow());
        } else if ("postgresql".equals(dialect)) {
            pageSql.append(sql);
            pageSql.append(" limit " + page.getPageSize() + " offset " + page.getStartRow());
        } else if ("oracle".equals(dialect)) {
            pageSql.append("select * from ( select temp.*, rownum row_id from ( ");
            pageSql.append(sql);
            pageSql.append(" ) temp where rownum <= ").append(page.getEndRow());
            pageSql.append(") where row_id > ").append(page.getStartRow());
        }
        return pageSql.toString();
    }

    private  class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;
        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }
        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    /**
     * 由于MappedStatement是一个全局共享的对象，因而需要复制一个对象来进行操作，防止并发访问导致错误
     *
     * @param ms
     * @param newSqlSource
     * @return
     */
    private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + "_PageHelper", newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        //由于resultMaps第一次需要返回int类型的结果，所以这里需要生成一个resultMap - 防止并发错误
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), int.class, EMPTY_RESULTMAPPING).build();
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    /**
     * 只拦截Executor
     *
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    /**
     * 设置属性值
     *
     * @param p
     */
    @Override
    public void setProperties(Properties p) {
        dialect = p.getProperty("dialect");
        if (dialect == null || "".equals(dialect)) {
            throw new RuntimeException("Mybatis分页插件PageHelper无法获取dialect参数!");
        }
        //offset作为PageNum使用
        String offset = p.getProperty("offsetAsPageNum");
        if (offset != null && "TRUE".equalsIgnoreCase(offset)) {
            offsetAsPageNum = true;
        }
        //RowBounds方式是否做count查询
        String withcount = p.getProperty("rowBoundsWithCount");
        if (withcount != null && "TRUE".equalsIgnoreCase(withcount)) {
            rowBoundsWithCount = true;
        }
    }
}