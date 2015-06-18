
package com.google.code.shardbatis.processor;

import com.google.code.shardbatis.builder.SqlProcessorConfigFactory;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;


/**
 * <p>Title: InsertSqlProcessor</P>
 * <p>Description: 插入语句SQL处理器</p>
 * <p>Copyright: frankboy.cpu@gmail.com</p>
 * @author franklin
 * @version 1.0
 * @since Jun 18, 2015
 */
public class InsertSqlProcessor extends AbstractSqlProcessor {

	public InsertSqlProcessor(SqlProcessorConfigFactory sqlProcessorConfigFactory) {
		super(sqlProcessorConfigFactory);
	}

	@Override
	protected Statement doSqlProcess(Statement statement, Object params, String mapperId) {
		
		if (!(statement instanceof Insert)) {
			throw new IllegalArgumentException("The argument statement must is instance of Insert.");
		}
		
		Insert insert = (Insert) statement;   
		this.processTable(insert.getTable(), params, mapperId);
		
		return insert;
	}

}
