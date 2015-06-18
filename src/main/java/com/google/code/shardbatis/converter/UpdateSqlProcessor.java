package com.google.code.shardbatis.converter;

import java.util.List;

import com.google.code.shardbatis.builder.SqlProcessorConfigFactory;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.update.Update;

/**
 * <p>Title: UpdateSqlProcessor</P>
 * <p>Description: 更新语句SQL处理器</p>
 * <p>Copyright: frankboy.cpu@gmail.com</p>
 * @author franklin
 * @version 1.0
 * @since Jun 18, 2015
 */
public class UpdateSqlProcessor extends AbstractSqlProcessor {

	public UpdateSqlProcessor(SqlProcessorConfigFactory sqlProcessorConfigFactory) {
		super(sqlProcessorConfigFactory);
	}

	@Override
	protected Statement doSqlProcess(Statement statement, Object params, String mapperId) {
		if (!(statement instanceof Update)) {
			throw new IllegalArgumentException("The argument statement must is instance of Update.");
		}
		
		Update update = (Update) statement;
		List<Table> tableList = update.getTables();
		
		for(Table t  : tableList){
			this.processTable(t, params, mapperId);
		}
		
		return update;
	}

}
