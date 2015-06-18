package com.google.code.shardbatis.strategy;

import net.sf.jsqlparser.schema.Table;

/**
 * <p>Title: ProcessStrategy</P>
 * <p>Description: 表处理策略接口</p>
 * <p>Copyright: frankboy.cpu@gmail.com</p>
 * @author franklin
 * @version 1.0
 * @since Jun 18, 2015
 */
public interface ProcessStrategy {
	/**
	 * 得到实际表名
	 * @param table  表对象，sqlparser解析后的表的对象表示
	 * @param params mybatis执行某个statement时使用的参数
	 * @param mapperId mybatis配置的statement id
	 * @return void 
	 */
	public void processTable(Table table,Object params,String mapperId);
}
