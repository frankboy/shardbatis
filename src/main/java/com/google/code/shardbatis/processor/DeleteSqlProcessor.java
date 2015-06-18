package com.google.code.shardbatis.processor;

import com.google.code.shardbatis.builder.SqlProcessorConfigFactory;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;

/**
 * <p>Title: DeleteSqlProcessor</P>
 * <p>Description: 删除语句SQL处理器</p>
 * <p>Copyright: frankboy.cpu@gmail.com</p>
 * @author franklin
 * @version 1.0
 * @since Jun 18, 2015
 */
public class DeleteSqlProcessor extends AbstractSqlProcessor {

	public DeleteSqlProcessor(SqlProcessorConfigFactory sqlProcessorConfigFactory) {
		super(sqlProcessorConfigFactory);
	}

	@Override
	protected Statement doSqlProcess(Statement statement, Object params, String mapperId) {
		
		if (!(statement instanceof Delete)) {
			throw new IllegalArgumentException("The argument statement must is instance of Delete.");
		}
		
		Delete delete = (Delete) statement;

		this.processTable(delete.getTable(), params, mapperId);
		
		return delete;
	}

}
