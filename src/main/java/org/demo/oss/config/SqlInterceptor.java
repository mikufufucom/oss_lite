package org.demo.oss.config;

import org.demo.oss.utils.StringUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * SQLInterceptor
 *
 *
 * @author moxiaoli*/
/*
这段代码是使用MyBatis的插件系统来创建一个拦截器。拦截器是用来拦截MyBatis的某些方法调用的。

@Intercepts注解用于标识这个类是一个MyBatis拦截器。

@Signature注解则定义了需要拦截的类、方法和参数。每个@Signature代表一个拦截点：

- type：需要拦截的类，例如StatementHandler.class和Executor.class。
- method：需要拦截的方法，例如prepare、getBoundSql、update和query。
- args：方法的参数类型，例如Connection.class, Integer.class表示拦截的方法的参数是一个Connection和一个Integer。

例如，@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})表示拦截StatementHandler类的prepare方法，该方法的参数是Connection和Integer。

这个拦截器可以用于多种用途，例如修改SQL语句、记录执行时间、审计等。
 */
@Intercepts({
        // 拦截StatementHandler类的prepare方法
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        // 拦截StatementHandler类的getBoundSql方法
        @Signature(type = StatementHandler.class, method = "getBoundSql", args = {}),
        // 拦截Executor类的update方法
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        // 拦截Executor类的query方法
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        // 拦截Executor类的query方法
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
@Component //SpringBoot使用该注解，注入容器
public class SqlInterceptor implements Interceptor {

    // 定义日志记录器
    private static final Logger log = LoggerFactory.getLogger(SqlInterceptor.class);

    // 定义一个线程安全的HashMap
    private static final Map<String, Object> MAP = new ConcurrentHashMap<>(2);

    // 定义一个日期格式化对象
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 重写intercept，拦截sql，拼接完整sql语句
     * @param invocation 调用
     * @return Object
     * @throws Throwable 可抛出
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 定义返回值
        Object returnValue;
        // 记录开始时间 System.currentTimeMillis()获取当前时间毫秒数
        long start = System.currentTimeMillis();
        // 执行方法 invocation.proceed()表示继续执行被拦截的方法，并返回其结果
        returnValue = invocation.proceed();
        // 记录结束时间
        long end = System.currentTimeMillis();
        // 计算执行时间
        long time = end - start;
        try {
            /* 获取方法参数
            invocation.getArgs()是一个方法调用的参数数组。
            在拦截器中，invocation对象表示当前方法调用的上下文，包括被拦截的方法、方法的参数等信息。
            getArgs()方法返回一个数组，包含了被拦截方法的所有参数。
            通过invocation.getArgs()可以获取到被拦截方法的参数列表。
            在这段代码中，invocation.getArgs()用于获取拦截器方法中的参数数组。
             */
            final Object[] args = invocation.getArgs();
            // 判断参数是否为MappedStatement实例
            if (!(args[0] instanceof MappedStatement)) {
                return returnValue;
            }
            // 获取MappedStatement实例
            MappedStatement ms = (MappedStatement) args[0];

            Object parameter = null;
            // 判断是否有参数
            if (invocation.getArgs().length > 1) {
                // 获取参数
                parameter = invocation.getArgs()[1];
            }
            // 获取方法对象
            String name = getMethodName(invocation, ms);
            // 获取到节点的id,即sql语句的id
            String sqlId = ms.getId();
            // 获取BoundSql对象
            BoundSql boundSql = ms.getBoundSql(parameter);
            // 获取配置对象
            Configuration configuration = ms.getConfiguration();
            // 获取最终的sql语句
            String sql = getSql(configuration, boundSql, sqlId, time, returnValue, name);
            // 记录sql语句
            log.info(sql);
        } catch (Exception e) {
            // 记录错误信息
            log.error("拦截sql处理出错，出错原因：" + e.getMessage());
        }
        // 返回结果
        return returnValue;
    }

    /**
     * 获取方法名
     * @param invocation invocation对象表示当前方法调用的上下文，包括被拦截的方法、方法的参数等信息。
     * @param ms         MappedStatement实例
     * @return String 方法名
     */
    @NotNull
    private static String getMethodName(Invocation invocation, MappedStatement ms) {
        Method method = invocation.getMethod();
        // 获取方法名
        String name = method.getName();
        // 获取sql命令类型
        String commandName = ms.getSqlCommandType().name();
        // 根据sql命令类型修改方法名
        if (commandName.startsWith("INSERT")) {
            name = name + " = 新增";
        } else if (commandName.startsWith("UPDATE")) {
            name = name + " = 修改";
        } else if (commandName.startsWith("DELETE")) {
            name = name + " = 删除";
        } else if (commandName.startsWith("SELECT")) {
            name = name + " = 查询";
        }
        return name;
    }

    /**
     * 封装了一下sql语句，使得结果返回完整xml路径下的sql语句节点id + sql语句
     *
     * @param configuration 配置
     * @param boundSql      boundSql
     * @param sqlId         sqlId
     * @param time          执行时间
     * @param result        结果
     * @param name          sql操作类型
     * @return String
     */
    public static String getSql(Configuration configuration, BoundSql boundSql, String sqlId, long time, Object result, String name) {
        // 显示sql语句
        showSql(configuration, boundSql);
        // 定义消息
        String message = "[SqlInterceptor] 执行 [" + name + "] 时间 [" + FORMATTER.format(System.currentTimeMillis()) + "] sql耗时 [" + (double) time / 1000 + "] s";
        // 定义StringBuilder对象
        StringBuilder str = new StringBuilder();
        // 获取参数映射列表
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // 拼接sql执行信息
        str.append("\n").append("----------------------------begin【SQL Execute Message】--------------------------------\n");
        str.append("【方法】").append(sqlId).append("\n");
        str.append("【sql】").append("\n").append(MAP.get("sql"));
        str.append("\n");
        str.append("【参数映射】").append(parameterMappings);
        str.append("\n");
        str.append("【参数值】").append(MAP.get("parameters"));
        str.append("\n");
        str.append("【参数对象】").append(StringUtils.join((List<?>) MAP.get("parameters"), ", ","{ "," }"));
        str.append("\n");
        str.append("【结果】 ");
        // 判断结果类型并拼接结果信息
        if (result != null) {
            if (result instanceof List) {
                str.append("共 ").append(((List<?>) result).size()).append(" 条记录\n");
            } else if (result instanceof Collection) {
                str.append("共 ").append(((Collection<?>) result).size()).append(" 条记录\n");
            } else {
                str.append("共 1 条记录").append("\n");
            }
            str.append("【结果详情】").append("\n").append(result).append("\n");
        } else {
            str.append("【结果】  NULL").append("\n");
        }
        // 拼接执行信息
        str.append("【执行信息】").append(message);
        str.append("\n");
        str.append("----------------------------end【SQL Execute Message】--------------------------------\n");
        // 返回sql执行信息
        return str.toString();
    }

    /**
     * 如果参数是String，则添加单引号，
     * 如果是日期，则转换为时间格式器并加单引号； 对参数是null和不是null的情况作了处理
     *
     * @param obj 参数
     * @return String
     */
    private static String getParameterValue(Object obj) {
        // 定义值
        String value;
        // 判断参数类型并处理
        if (obj instanceof String) {
            value = "'" + obj + "'";
        } else if (obj instanceof Date) {
            value = "'" + FORMATTER.format(obj) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        // 返回值
        return value;
    }

    /**
     * 进行？的替换
     *
     * @param configuration 配置
     * @param boundSql boundSql
     */
    public static void showSql(Configuration configuration, BoundSql boundSql) {
        // 获取参数
        Object parameterObject = boundSql.getParameterObject();
        // 获取参数映射列表
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // 替换sql语句中的多个空格为一个空格
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        // 定义参数列表
        List<String> list = new ArrayList<>();
        // 判断是否有参数映射和参数对象
        if (!CollectionUtils.isEmpty(parameterMappings) && parameterObject != null) {
            // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换
            // 如果根据parameterObject.getClass(）可以找到对应的类型，则替换
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
                list.add(parameterObject + "(" + parameterObject.getClass().getSimpleName() + ")");
            } else {
                // MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值,主要支持对JavaBean、Collection、Map三种类型对象的操作
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    // 获取属性名
                    String propertyName = parameterMapping.getProperty();
                    // 判断是否有该属性的getter方法
                    if (metaObject.hasGetter(propertyName)) {
                        // 获取属性值
                        Object obj = metaObject.getValue(propertyName);
                        // 判断属性值是否为null并处理
                        sql = getSqlString(sql, list, parameterMapping, obj);
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        // 该分支是动态sql
                        // 获取附加参数
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        // 如果对象不为空
                        sql = getSqlString(sql, list, parameterMapping, obj);
                    } else {
                        // 如果没有getter方法，替换sql语句中的第一个问号为"缺失"
                        sql = sql.replaceFirst("\\?", "缺失");
                        // 将"缺失"添加到列表中
                        list.add("缺失");
                    }//打印出缺失，提醒该参数缺失并防止错位
                }
            }
        }
        // 将sql语句和参数列表添加到MAP中
        MAP.put("sql", sql);
        MAP.put("parameters", list);
    }

    /**
     * 根据参数值替换sql语句中的问号，并将参数添加到列表中
     *
     * @param sql              sql语句
     * @param list             参数列表
     * @param parameterMapping 参数映射
     * @param obj              参数值
     * @return String 替换后的sql语句
     */
    @NotNull
    private static String getSqlString(String sql, List<String> list, ParameterMapping parameterMapping, Object obj) {
        if (Objects.nonNull(obj)) {
            // 替换sql语句中的第一个问号为参数值
            sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
            // 将参数添加到列表中
            list.add(parameterMapping.getProperty() + "=" + obj + "(" + obj.getClass().getSimpleName() + ")");
        } else {
            // 如果对象为空，替换sql语句中的第一个问号为null
            sql = sql.replaceFirst("\\?", "null");
            // 将参数添加到列表中
            list.add(parameterMapping.getProperty() + "=null");
        }
        return sql;
    }

    // 重写plugin方法，使用Plugin.wrap方法包装目标对象
    @Override
    public Object plugin(Object arg0) {
        return Plugin.wrap(arg0, this);
    }

    @Override
    // 设置属性，此处未使用，所以方法体为空
    public void setProperties(Properties properties) {}
}

