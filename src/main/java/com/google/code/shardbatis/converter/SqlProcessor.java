package com.google.code.shardbatis.converter;

import net.sf.jsqlparser.statement.Statement;

/**
 * <p>Title: SqlProcessor</P>
 * <p>Description: SQL处理器接口</p>
 * <p>Copyright: frankby.cpu@gmail.com</p>
 * @author franklin
 * @version 1.0
 * @since Jun 18, 2015
 */
public interface SqlProcessor {
	/**
	 * <p>方法名称：doProcess</p>
	 * <p>方法描述：处理SQL语句</p>
	 * @param Statement    原始SQL语句
	 * @param params       SQL参数
	 * @param mapperId     SQL_ID: 在mybatis中表示的mapped sql id   
	 * @return String      处理后的SQL语句
	 * @author franklin
	 * @since  Jun 18, 2015
	 * <p> history Jun 18, 2015 franklin  创建   <p>
	 */
	public String doProcess(Statement statement,Object params,String mapperId);
}
