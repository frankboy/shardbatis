package com.google.code.shardbatis.strategy.impl;

import net.sf.jsqlparser.schema.Table;

import com.google.code.shardbatis.strategy.ProcessStrategy;

/**
 * 默认的表处理策略，供测试用
 * @author sean.he
 *
 */
public class NoSqlProcessStrategy implements ProcessStrategy {

	public void processTable(Table table, Object params, String mapperId) {
		// TODO do nothing !
	}

}
