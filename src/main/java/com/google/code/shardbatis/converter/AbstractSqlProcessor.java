package com.google.code.shardbatis.converter;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import com.google.code.shardbatis.builder.SqlProcessorConfigFactory;
import com.google.code.shardbatis.strategy.ProcessStrategy;

/**
 * <p>Title: AbstractSqlProcessor</P>
 * <p>Description: SQL处理器抽象实现</p>
 * <p>Copyright: frankboy.cpu@gmail.com</p>
 * @author franklin
 * @version 1.0
 * @since Jun 18, 2015
 */
public abstract class AbstractSqlProcessor implements SqlProcessor {
	
	/**
	 * SQL处理配置工厂
	 */
	private SqlProcessorConfigFactory sqlProcessorConfigFactory = null;
	
	/**
	 * 默认构造器
	 * @param sqlProcessorConfigFactory  SQL处理器配置工厂
	 */
	public AbstractSqlProcessor(SqlProcessorConfigFactory sqlProcessorConfigFactory){
		this.setSqlProcessorConfigFactory(sqlProcessorConfigFactory);
	}

	public SqlProcessorConfigFactory getSqlProcessorConfigFactory() {
		return sqlProcessorConfigFactory;
	}

	public void setSqlProcessorConfigFactory(SqlProcessorConfigFactory sqlProcessorConfigFactory) {
		this.sqlProcessorConfigFactory = sqlProcessorConfigFactory;
	}
	
	/**
	 * <p>方法名称：doProcess</p>
	 * <p>方法描述：SQL语句处理实现</p>
	 * @param statement    原生SQL
	 * @param params       SQL参数
	 * @param mapperId     SQLID
	 * @return String
	 * @author franklin
	 * @since Jun 18, 2015
	 */
	@Override
	public String doProcess(Statement statement, Object params, String mapperId) {
		return doDeParse(doSqlProcess(statement, params, mapperId));
	}
	
	/**
	 * 将Statement反解析为sql
	 * @param statement
	 * @return
	 */
	protected String doDeParse(Statement statement) {
		StatementDeParser deParser = new StatementDeParser(new StringBuilder());
		statement.accept(deParser);
		return deParser.getBuffer().toString();
	}

	/**
	 * 从SqlProcessorConfigFactory中查找SqlProcessStrategy并对表名进行修改<br>
	 * 如果没有相应的ShardStrategy则对表名不做修改
	 * @param table      表对象
	 * @param params     参数对象
	 * @param mapperId   SQL_ID
	 * @return
	 */
	protected void processTable(Table table, Object params, String mapperId) {
		ProcessStrategy strategy = this.getSqlProcessorConfigFactory().getStrategy(table.getName());
		if(strategy != null){
			strategy.processTable(table, params, mapperId);
		}
	}
	
	/**
	 * 修改statement代表的sql语句
	 * @param statement
	 * @param params
	 * @param mapperId
	 * @return
	 */
	protected abstract Statement doSqlProcess(Statement statement, Object params,String mapperId);
}
