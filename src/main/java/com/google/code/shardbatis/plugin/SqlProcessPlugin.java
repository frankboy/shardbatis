package com.google.code.shardbatis.plugin;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import com.google.code.shardbatis.builder.SqlProcessorFactory;
import com.google.code.shardbatis.builder.SqlProcessorConfigFactory;
import com.google.code.shardbatis.util.ReflectionUtils;

/**
 * <p>Title: SqlProcessPlugin</P>
 * <p>Description: SQL处理拦截器插件</p>
 * <p>Copyright: frankboy.cpu@gmail.com</p>
 * @author franklin
 * @version 1.0
 * @since Jun 18, 2015
 */
@Intercepts( { @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class })})
public class SqlProcessPlugin implements Interceptor {
	
	private static final Log log = LogFactory.getLog(SqlProcessPlugin.class);

	public  static final String SHARDING_CONFIG = "shardingConfig";
	
	private SqlProcessorConfigFactory  sqlProcessorConfigFactory  = null ;

	public Object intercept(Invocation invocation) throws Throwable {

		StatementHandler statementHandler = (StatementHandler) invocation
				.getTarget();
		
		MappedStatement mappedStatement = null;
		if (statementHandler instanceof RoutingStatementHandler) {
			StatementHandler delegate = (StatementHandler) ReflectionUtils.getFieldValue(statementHandler, "delegate");
			mappedStatement = (MappedStatement) ReflectionUtils.getFieldValue(delegate, "mappedStatement");
		} else {
			mappedStatement = (MappedStatement) ReflectionUtils.getFieldValue(statementHandler, "mappedStatement");
		}

		String mapperId = mappedStatement.getId();		

		if (sqlProcessorConfigFactory.isShouldParse(mapperId)) {
			String sql = statementHandler.getBoundSql().getSql();
			if (log.isDebugEnabled()) {
				log.debug("Original Sql [" + mapperId + "]:" + sql);
			}
			Object params = statementHandler.getBoundSql().getParameterObject();
			
			SqlProcessorFactory cf =  this.sqlProcessorConfigFactory.getSqlProcessorFactory();
			sql = cf.doSqlProcess(sql, params, mapperId);
			if (log.isDebugEnabled()) {
				log.debug("Converted Sql [" + mapperId + "]:" + sql);
			}
			ReflectionUtils.setFieldValue(statementHandler.getBoundSql(), "sql", sql);
		}
		return invocation.proceed();
	}

	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	/**
	 * <p>方法名称：setProperties</p>
	 * <p>方法描述：设置参数时解析配置文件</p>
	 * @param properties
	 * @author franklin
	 * @since Jun 17, 2015
	 */
	public void setProperties(Properties properties) {
		String configFile = properties.getProperty(SHARDING_CONFIG, null);
		if (configFile == null || configFile.trim().length() == 0) {
			throw new IllegalArgumentException("property 'shardingConfig' is requested.");
		}
		sqlProcessorConfigFactory = SqlProcessorConfigFactory.getInstance(configFile);
	}
}
