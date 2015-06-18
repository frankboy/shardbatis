package com.google.code.shardbatis.strategy.impl;

import net.sf.jsqlparser.schema.Table;

import com.google.code.shardbatis.strategy.ProcessStrategy;

/**
 * @author sean.he
 *
 */
public class TestShardStrategyImpl implements ProcessStrategy {

	public void processTable(Table table, Object params, String mapperId) {
		 table.setName(table.getName()+"_xx");
	}

}
