## shardbatis2.x使用指南 ##
### 运行环境 ###
  * jdk6.0+:shardbatis使用JDK6.0编译。也可以使用JDK5.0编译
  * mybatis3.0+
### 1.配置 ###
  * 添加sharding配置
新建一个xml文件,例如：shard\_config.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE shardingConfig PUBLIC "-//shardbatis.googlecode.com//DTD Shardbatis 2.0//EN"
  "http://shardbatis.googlecode.com/dtd/shardbatis-config.dtd">
<shardingConfig>
	<!--
		ignoreList可选配置
		ignoreList配置的mapperId会被分表参加忽略解析,不会对sql进行修改
	-->
	<ignoreList>
		<value>com.google.code.shardbatis.test.mapper.AppTestMapper.insertNoShard</value>
	</ignoreList>
	<!-- 
		parseList可选配置
		如果配置了parseList,只有在parseList范围内并且不再ignoreList内的sql才会被解析和修改
	-->
	<parseList>
		<value>com.google.code.shardbatis.test.mapper.AppTestMapper.insert</value>
	</parseList>
	<!-- 
		配置分表策略
	-->
	<strategy tableName="APP_TEST" strategyClass="com.google.code.shardbatis.strategy.impl.AppTestShardStrategyImpl"/>      
</shardingConfig>
```
shard\_config.xml必须保存在应用的classpath中
  * 在mybatis配置文件中添加插件配置
```
<plugins>
	<plugin interceptor="com.google.code.shardbatis.plugin.ShardPlugin">
		<property name="shardingConfig" value="shard_config.xml"/>
	</plugin>
</plugins> 
```

### 2.实现自己的sharding策略 ###
  * 实现一个简单的接口即可
```
/**
 * 分表策略接口
 * @author sean.he
 *
 */
public interface ShardStrategy {
	/**
	 * 得到实际表名
	 * @param baseTableName 逻辑表名,一般是没有前缀或者是后缀的表名
	 * @param params mybatis执行某个statement时使用的参数
	 * @param mapperId mybatis配置的statement id
	 * @return
	 */
	String getTargetTableName(String baseTableName,Object params,String mapperId);
}
```
> 可以参考
```
com.google.code.shardbatis.strategy.impl.AppTestShardStrategyImpl
```

### 3.代码中使用shardbatis ###
因为shardbatis2.0使用插件方式对mybatis功能进行增强，因此使用配置了shardbatis的mybatis3和使用原生的mybatis3没有区别
```
SqlSession session = sqlSessionFactory.openSession();
try {
	AppTestMapper mapper = session.getMapper(AppTestMapper.class);
  mapper.insert(testDO);
	session.commit();
} finally {
	session.close();
}
```

### <font color='red'>使用注意事项</font> ###
  * 2.0版本中insert update delete 语句中的\*子查询语句中的表\*不支持sharding(不好意思太拗口了-`_`-!)
  * select语句中如果进行多表关联，请务必为每个表名加上别名
```
例如原始sql语句：SELECT a.* FROM ANTIQUES a,ANTIQUEOWNERS b, mytable c where a.id=b.id and b.id=c.id
经过转换后的结果可能为：SELECT a.* FROM ANTIQUES_0 AS a, ANTIQUEOWNERS_1 AS b, mytable_1 AS c WHERE a.id = b.id AND b.id = c.id
```
  * 目前已经支持了大部分的sql语句的解析，已经测试通过的语句可以查看测试用例：
```
select * from test_table1
select * from test_table1 where col_1='123'
select * from test_table1 where col_1='123' and col_2=8
select * from test_table1 where col_1=?
select col_1,max(col_2) from test_table1 where col_4='t1' group by col_1
select col_1,col_2,col_3 from test_table1 where col_4='t1' order by col_1
select col_1,col_2,col_3 from test_table1 where id in (?,?,?,?,?,?,?,?,?) limit ?,?
select a.*  from test_table1 a,test_table2 b where a.id=b.id and a.type='xxxx'
select a.col_1,a.col_2,a.col_3 from test_table1 a where a.id in (select aid from test_table2 where col_1=1 and col_2=?) order by id desc
select col_1,col_2 from test_table1 where type is not null and col_3 is null order by id
select count(*),col_1 from test_table2 group by col_1 having count(*)>1
select a.col_1,a.col_2,b.col_1 from test_table1 a,t_table b where a.id=b.id
insert into test_table1 (col_1,col_2,col_3,col_4) values (?,?,?,?)
SELECT EMPLOYEEIDNO FROM test_table1 WHERE POSITION = 'Manager' AND SALARY > 60000 OR BENEFITS > 12000
SELECT EMPLOYEEIDNO FROM test_table1 WHERE POSITION = 'Manager' AND (SALARY > 50000 OR BENEFIT > 10000)
SELECT EMPLOYEEIDNO FROM test_table1 WHERE LASTNAME LIKE 'L%'
SELECT DISTINCT SELLERID, OWNERLASTNAME, OWNERFIRSTNAME FROM test_table1, test_table2 WHERE SELLERID = OWNERID ORDER BY OWNERLASTNAME, OWNERFIRSTNAME, OWNERID
SELECT OWNERFIRSTNAME, OWNERLASTNAME FROM test_table1 WHERE EXISTS (SELECT * FROM test_table2 WHERE ITEM = ?)
SELECT BUYERID, ITEM FROM test_table1 WHERE PRICE >= ALL (SELECT PRICE FROM test_table2)
SELECT BUYERID FROM test_table1 UNION SELECT BUYERID FROM test_table2
SELECT OWNERID, 'is in both Orders & Antiques' FROM test_table1 a, test_table2 b WHERE a.OWNERID = b.BUYERID and a.type in (?,?,?)
SELECT DISTINCT SELLERID, OWNERLASTNAME, OWNERFIRSTNAME FROM test_table1, noconvert_table WHERE SELLERID = OWNERID ORDER BY OWNERLASTNAME, OWNERFIRSTNAME, OWNERID
SELECT a.* FROM test_table1 a, noconvert_table b WHERE a.SELLERID = b.OWNERID 
update test_table1 set col_1=123 ,col_2=?,col_3=? where col_4=?
update test_table1 set col_1=?,col_2=col_2+1 where id in (?,?,?,?)
delete from test_table2 where id in (?,?,?,?,?,?) and col_1 is not null
INSERT INTO test_table1 VALUES (21, 01, 'Ottoman', ?,?)
INSERT INTO test_table1 (BUYERID, SELLERID, ITEM) VALUES (01, 21, ?)
```
> 可能有些sql语句没有出现在测试用例里，但是相信基本上常用的查询sql shardbatis解析都没有问题，因为shardbatis对sql的解析是基于<a href='http://jsqlparser.sourceforge.net/'>jsqlparser</a>

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
	<version>2.0.0B</version>
</dependency>
```
  * 手工添加到项目classpath中
请到download页面下载