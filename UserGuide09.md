## shardbatis0.9使用指南 ##
### 运行环境 ###
  * jdk5.0+:对于ibatis的扩展基于jdk5.0的api，并且使用jdk5.0进行编译
  * Spring2.5+:使用spring+ibatis的话(这个好像是废话O(∩`_`∩)O哈哈)
### 1.使用ibatis原生api <font color='red'>since 0.9.1</font> ###
  * 添加sharding配置
```
<!-- 在sql-map-config.xml中添加如下配置 -->
<shardingConfig>
	<!-- 对于app_test使用默认的切分策略,默认的切分策略只是简单按数值取模生成新的表名,如何实现切分策略后面再介绍 -->
	<sharding tableName="app_test" strategyClass="com.ibatis.sqlmap.engine.sharding.impl.DefaultShardingStrategy"/>
	<!-- 没有配置切分策略等于使用默认的切分策略 -->
	<sharding tableName="app_2test" />
</shardingConfig>
<!-- 在sqlmap.xml中statement节点都可以添加shardingParams属性
shardingParams对应的值使用json数组格式，
paramExpr 对应parameterClass对象中的某个属性，这个属性的值会传递给sharding策略用于计算新的表名，
例如paramExpr：testId 等同于appTest.getTestId;paramExpr：model.id 等同于appTest.getModel().getId()
tableName 用于表示当前这个json对象配使用于sql中的哪个表
strategyClass strateg接口实现类的全名,这个值如果没有将使用sql-map-config.xml里shardingConfig下配置对应的值。
-->
<select id="select_count_native" parameterClass="AppTest" resultClass="java.lang.Integer" 
shardingParams='[{"paramExpr":"testId","tableName":"app_test"}]'>
select count(*) from app_test where cnt=#cnt#
</select>
```
  * 下面开始编码
```
AppTest param=new AppTest();
param.setTestId(2);
param.setCnt("testShardingWithConfig");
Integer count=(Integer)sqlMapper.queryForObject("AppTest.select_count_native",param);//和使用原生的ibatis API没有区别
```
> 最终执行的SQL可能是如下样式
```
select count(*) from app_test_1 where cnt=?
```
### 2.使用ibatis sharding API ###
  * 添加sharding配置。在sql-map-config.xml中添加如下配置
```
<shardingConfig>
	<!-- 对于app_test使用默认的切分策略,默认的切分策略只是简单按数值取模生成新的表名,如何实现切分策略后面再介绍 -->
	<sharding tableName="app_test" strategyClass="com.ibatis.sqlmap.engine.sharding.impl.DefaultShardingStrategy"/>
	<!-- 没有配置切分策略等于使用默认的切分策略 -->
	<sharding tableName="app_2test" />
</shardingConfig>
```
> 完成配置请参考http://shardbatis.googlecode.com/svn/trunk/src/test/resources/sql-map-config.xml
  * 使用sharding API
```
public class SqlMapClientTest {
	SqlMapClient sqlMapper;

	@Before
	public void init() {
		Reader reader;
		try {
			reader = Resources.getResourceAsReader("sql-map-config.xml");
			sqlMapper = SqlMapClientBuilder.buildSqlMapClient(reader);
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testSharding() throws Exception {
		Map param = new HashMap();
		param.put("cnt", "ttt");
		ShardingFactorGroup g = new ShardingFactorGroup();//ShardingFactorGroup为切分策略提供必要参数
		g.setTableName("App_Test");//设置为哪张表配置切分策略
		g.setParam(new Integer(123));//设置为切分策略提供参数
		//这里还可以通过g.setShardingStrategy(...)来设置切分策略
		//通过API配置的切分策略可以覆盖xml里配置的App_Test表的切分策略
		Integer count = (Integer) sqlMapper.queryForObjectWithSharding(
				"AppTest.select_paging_count_by_map", param, g);
		Assert.assertEquals(count.toString(), "0");
	}

	@Test
	public void testUpdate() throws SQLException {
		ShardingFactorGroup g = new ShardingFactorGroup();
		g.setTableName("App_Test");
		g.setParam(new Integer(123));

		String cnt = "testUpdate" + System.currentTimeMillis();
		AppTest at1 = new AppTest();
		at1.setCnt(cnt);
		Integer id = (Integer) sqlMapper.insertWithSharding(
				"AppTest.insert_h2", at1, g);

		AppTest parameterObject = new AppTest();
		parameterObject.setCnt(cnt);
		AppTest ret = (AppTest) sqlMapper.queryForObjectWithSharding(
				"AppTest.select_by_condition", parameterObject, g);
		Assert.assertEquals(ret.getId().toString(), id.toString());

		ret.setCnt("NEW_CONTENT");
		Integer count = sqlMapper.updateWithSharding("AppTest.update", ret, g);
		Assert.assertEquals(count.toString(), "1");

		count = (Integer) sqlMapper.queryForObjectWithSharding(
				"AppTest.select_paging_count", ret, g);
		Assert.assertEquals(count.toString(), "1");
	}

	@Test
	public void testDelete() throws SQLException {
		ShardingFactorGroup g = new ShardingFactorGroup();
		g.setTableName("App_Test");
		g.setParam(new Integer(123));

		String cnt = "testDelete" + System.currentTimeMillis();
		AppTest at1 = new AppTest();
		at1.setCnt(cnt);
		Integer id = (Integer) sqlMapper.insertWithSharding(
				"AppTest.insert_h2", at1, g);

		AppTest parameterObject = new AppTest();
		parameterObject.setCnt(cnt);
		AppTest ret = (AppTest) sqlMapper.queryForObjectWithSharding(
				"AppTest.select_by_condition", parameterObject, g);
		Assert.assertEquals(ret.getId().toString(), id.toString());

		Integer row = sqlMapper.deleteWithSharding("AppTest.delete", ret, g);
		Assert.assertEquals(row.toString(), "1");
	}
}
```
> 具体代码可以查看org.shardbatis.test.ibatis.SqlMapClientTest<br />
> <font color='red'>注意使用api方式将优先于配置，通过api传递的sharding参数可以覆盖原xml中的配置</font>
### 3.使用spring+ibatis sharding API ###
  * ibatis配置
> 请参考http://shardbatis.googlecode.com/svn/trunk/src/test/resources/sql-map-config-spring.xml
  * spring关键配置
```
<bean id="sqlMapClient"
	class="org.springframework.orm.ibatis.SqlMapClientFactoryBean" lazy-init="false">
	<property name="configLocation"
		value="classpath:/sql-map-config-spring.xml" /><!-- 使用带有sharding配置的配置ibatis配置文件 -->
	<property name="dataSource" ref="dataSource" />
</bean>
<!-- 配置shardbatis为spring重新实现的sqlmapclienttemplate -->
<bean id="sqlMapClientWithShardingTemplate"
	class="org.shardbatis.spring.orm.SqlMapClientWithShardingTemplate" lazy-init="false">
	<property name="sqlMapClient" ref="sqlMapClient" />
</bean>
<!-- 将新的sqlMapClientWithShardingTemplate注入到应用的dao中 -->
<bean id="testDao" class="org.shardbatis.test.spring.TestDao">
	<property name="sqlMapClientTemplate" ref="sqlMapClientWithShardingTemplate" />
</bean>
```
> 完整spring配置请参考http://shardbatis.googlecode.com/svn/trunk/src/test/resources/applicationContext-test.xml
  * DAO实现
```
/**
 * 应用的DAO要继承SqlMapClientWithShardingDaoSupport
 */
public class TestDao extends SqlMapClientWithShardingDaoSupport {
	
	public <T> T get(Class<T> entityClass, Object param,
			String statementName,ShardingFactorGroup... groups) {
		//通过getSqlMapClientWithShardingTemplate方法得到SqlMapClientWithShardingTemplate 的引用
		SqlMapClientWithShardingTemplate template=this.getSqlMapClientWithShardingTemplate();
		return (T)template.queryForObjectWithSharding(statementName, param, groups);
	}
	
	public void remove(Object parameterObject,String statementName,ShardingFactorGroup... groups) {
		SqlMapClientWithShardingTemplate template=getSqlMapClientWithShardingTemplate();
		template.deleteWithSharding(statementName, parameterObject, groups);
	}
	
	public int insert(Object parameterObject,String statementName,ShardingFactorGroup... groups) {
		SqlMapClientWithShardingTemplate template=getSqlMapClientWithShardingTemplate();
		return (Integer) template.insertWithSharding(statementName, parameterObject, groups);
	}
}
```

### 实现自己的sharding策略 ###
  * 实现一个简单的接口即可
```
public interface ShardingStrategy {
	/**
	 * 计算得到新的表名
	 * @param baseTableName 逻辑表名
	 * @param params 为sharding逻辑提供必要参数
	 * @return 
	 */
	public String getTargetTableName(String baseTableName,Object params);
}
```
> 可以参考http://shardbatis.googlecode.com/svn/trunk/src/main/java/com/ibatis/sqlmap/engine/sharding/impl/DefaultShardingStrategy.java

### <font color='red'>使用注意事项</font> ###
  * 0.9版本中insert update delete 语句中的\*子查询语句中的表\*不支持sharding(不好意思太拗口了-`_`-!)
  * select语句中如果进行多表关联，请务必为每个表名加上别名
```
例如原始sql语句：SELECT a.* FROM ANTIQUES a,ANTIQUEOWNERS b, mytable c where a.id=b.id and b.id=c.id
经过转换后的结果可能为：SELECT a.* FROM ANTIQUES_0 AS a, ANTIQUEOWNERS_1 AS b, mytable_1 AS c WHERE a.id = b.id AND b.id = c.id
```
  * 目前已经支持了大部分的sql语句的解析，已经测试通过的语句可以查看测试代码：http://shardbatis.googlecode.com/svn/trunk/src/test/java/org/shardbatis/test/ibatis/ConverterTest.java
> 可能有些sql语句没有出现在测试用例里，但是相信基本上常用的查询sql shardbatis解析都没有问题，因为shardbatis对sql的解析是基于<a href='http://jsqlparser.sourceforge.net/'>jsqlparser</a>

## 性能测试结果 ##
50个线程并发，每个线程循环执行100次数据库(h2数据库)查询。<br />
下面的测试结果建议关注两种场景的比较值，而不是关注某个具体值（你懂的）<br />
jdk:1.5.0\_22 无jvm参数<br />
单位：毫秒<br />

| **场景** | **20线程** | **30线程** | **50线程** | **50线程use jdk6** |
|:-----------|:-------------|:-------------|:-------------|:---------------------|
| sharding   | 690.721      | 1051.983778  | 1708.146467  | 1584.499067          |
| no sharding | 683.7596667  | 1024.096444  |	1704.1314    | 1601.5926            |

![http://shardbatis.googlecode.com/svn/tags/0.9.3beta/src/test/resources/perf.jpg](http://shardbatis.googlecode.com/svn/tags/0.9.3beta/src/test/resources/perf.jpg)

### 下载、安装 ###
  * 在maven中使用（推荐）
```
<!-- 新增远程仓库设置 -->
<repository>
	<id>shardbaits</id>
	<name>shardbaits repository</name>
	<url>http://shardbatis.googlecode.com/svn/trunk/repository</url>
	<snapshots>
		<enabled>false</enabled>
	</snapshots>
</repository>

<!-- 声明依赖 -->
<dependency>
	<groupId>org.shardbatis</groupId>
	<artifactId>shardbatis</artifactId>
	<version>0.9.2B</version>
</dependency>
```
  * 手工添加到项目classpath中
请到download页面下载