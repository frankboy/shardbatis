/**
 * 
 */
package com.google.code.shardbatis.builder;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import com.google.code.shardbatis.SqlProcessException;
import com.google.code.shardbatis.processor.DeleteSqlProcessor;
import com.google.code.shardbatis.processor.InsertSqlProcessor;
import com.google.code.shardbatis.processor.SelectSqlProcessor;
import com.google.code.shardbatis.processor.SqlProcessor;
import com.google.code.shardbatis.processor.UpdateSqlProcessor;

/**
 * 管理各种CRUD语句的Converter
 * @author sean.he
 * 
 */
public class SqlProcessorFactory {
	
	private static final Log log = LogFactory.getLog(SqlProcessorFactory.class);
	
	private SqlProcessorConfigFactory sqlProcessorConfigFactory = null ;

	private Map<String, SqlProcessor> converterMap;
	
	private CCJSqlParserManager pm  = null;
	
	protected SqlProcessorFactory(SqlProcessorConfigFactory sqlProcessorConfigFactory) {
		this.sqlProcessorConfigFactory = sqlProcessorConfigFactory ;
		converterMap = new HashMap<String, SqlProcessor>();
		pm = new CCJSqlParserManager();
		register();
	}

	private void register() {
		//TODO 待调整：从配置工厂中取得
		converterMap.put(Select.class.getName(), new SelectSqlProcessor(this.sqlProcessorConfigFactory));
		converterMap.put(Insert.class.getName(), new InsertSqlProcessor(this.sqlProcessorConfigFactory));
		converterMap.put(Update.class.getName(), new UpdateSqlProcessor(this.sqlProcessorConfigFactory));
		converterMap.put(Delete.class.getName(), new DeleteSqlProcessor(this.sqlProcessorConfigFactory));
	}
	
	/**
	 * 修改sql语句处理方法
	 * @param sql
	 * @param params
	 * @param mapperId
	 * @return 修改后的sql
	 * @throws SqlProcessException 解析sql失败会抛出SqlProcessException
	 */
	public String doSqlProcess(String sql, Object params, String mapperId) throws SqlProcessException {
		Statement statement = null;
		try {
			statement = pm.parse(new StringReader(sql));
		} catch (JSQLParserException e) {
			log.error(e.getMessage(), e);
			throw new SqlProcessException(e);
		}

		// TODO 待调整： 获取转换器策略
		SqlProcessor processor = this.converterMap.get(statement.getClass().getName());

		if (processor != null) {
			return processor.doProcess(statement, params, mapperId);
		}
		return sql;
	}
}
