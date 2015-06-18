package com.google.code.shardbatis.builder;

import org.junit.Assert;
import org.junit.Test;

import com.google.code.shardbatis.strategy.ProcessStrategy;


public class ShardConfigParserTest {
	@Test
	public void testParse_1() throws Exception{
		SqlProcessorConfigFactory factory=SqlProcessorConfigFactory.getInstance("test_config.xml");
		
		ProcessStrategy strategy=factory.getStrategy("test_table1");
		Assert.assertNotNull(strategy);
		
		strategy=factory.getStrategy("test_table2");
		Assert.assertNotNull(strategy);
		
		boolean configed=factory.isConfigParseId();
		Assert.assertFalse(configed);
		
		boolean ret=factory.isIgnoreId("ignoreId1");
		Assert.assertFalse(ret);
		
		ret=factory.isParseId("parseId");
		Assert.assertFalse(ret);
		
		// 正常情况下配置工厂只能处理一次，只有这里关闭了，下次测试才能重新解析文件
		factory.close() ;
	}
	
	@Test
	public void testParse_2() throws Exception{
		SqlProcessorConfigFactory factory=SqlProcessorConfigFactory.getInstance("test_config_2.xml");
		
		ProcessStrategy strategy=factory.getStrategy("test_table1");
		Assert.assertNotNull(strategy);
		
		strategy=factory.getStrategy("test_table2");
		Assert.assertNotNull(strategy);
		
		boolean configed=factory.isConfigParseId();
		Assert.assertTrue(configed);
		
		boolean ret=factory.isIgnoreId("ignoreId1");
		Assert.assertTrue(ret);
		
		ret=factory.isIgnoreId("ignoreId2");
		Assert.assertTrue(ret);
		
		ret=factory.isIgnoreId("<testid");
		Assert.assertTrue(ret);
		
		ret=factory.isIgnoreId("xxx");
		Assert.assertFalse(ret);
		
		ret=factory.isParseId("parseId");
		Assert.assertTrue(ret);
		
		ret=factory.isParseId("parseid>2");
		Assert.assertTrue(ret);
		
		ret=factory.isParseId("xxxxxxx");
		Assert.assertFalse(ret);
		
		factory.close();
	}
	
	@Test
	public void testParseFail() throws Exception {
		SqlProcessorConfigFactory factory=SqlProcessorConfigFactory.getInstance("error_config.xml");
		Assert.fail();
		factory.close();
	}
}
