package com.google.code.shardbatis.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.shardbatis.SqlProcessException;
import com.google.code.shardbatis.builder.SqlProcessorConfigFactory;
import com.google.code.shardbatis.builder.SqlProcessorFactory;

public class SqlConverterFactoryTest {
	
	private static SqlProcessorConfigFactory sqlProcessorConfigFactory ;
	private static SqlProcessorFactory  sqlProcessorFactory ;

	@BeforeClass
	public static void parseConfig() {
		
		    // 解析配置文件
			sqlProcessorConfigFactory = SqlProcessorConfigFactory.getInstance("test_config.xml");
			
			// 获取SQL处理器工厂
			sqlProcessorFactory = sqlProcessorConfigFactory.getSqlProcessorFactory() ;
	}
	
	@AfterClass
	public static void closeConfig() throws Exception {
		// 清理配置文件
		sqlProcessorConfigFactory.close();
	}

	@Test
	public void test_1() throws IOException, SqlProcessException {
		BufferedReader reader = new BufferedReader(Resources.getResourceAsReader("sql_src.txt"));
		String instring = null;

		List<String> converted = new ArrayList<String>();
		while ((instring = reader.readLine()) != null) {
			if (instring != null) {
				String sql = sqlProcessorFactory.doSqlProcess(instring, null, null);
				converted.add(sql);
			}
		}

		List<String> expectList = new ArrayList<String>();
		reader = new BufferedReader(Resources.getResourceAsReader("sql_ret.txt"));
		instring = null;
		while ((instring = reader.readLine()) != null) {
			if (instring != null) {
				expectList.add(instring);
			}
		}

		for (int i = 0; i < converted.size(); i++) {
			String sql = converted.get(i);
			String expect = expectList.get(i);
			System.out.println("sql after process is : " + sql + "\n expect sql is :" + expect);
		}
	}
}
