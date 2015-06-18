/**
 * 
 */
package com.google.code.shardbatis.builder;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.google.code.shardbatis.strategy.ProcessStrategy;

/**
 * @author sean.he
 * 
 */
public class SqlProcessorConfigFactory implements Closeable {
	
	/**
	 * 日志处理器
	 */
	private static final Log log = LogFactory.getLog(SqlProcessorConfigFactory.class);
	
	/**
	 * SQL 处理器配置工厂
	 */
	private static  SqlProcessorConfigFactory instance = null;
	
	/**
	 * XML类型定义文件
	 */
	private static final String SHARD_CONFIG_DTD = "com/google/code/shardbatis/builder/shardbatis-config.dtd";
	
	/**
	 * XML文件类型映射
	 */
	private static final Map<String, String> DOC_TYPE_MAP = new HashMap<String, String>();
	
	/**
	 * XML解析类型文档映射
	 */
	static {
		DOC_TYPE_MAP.put("http://shardbatis.googlecode.com/dtd/shardbatis-config.dtd".toLowerCase(), SHARD_CONFIG_DTD);
		DOC_TYPE_MAP.put("-//shardbatis.googlecode.com//DTD Shardbatis 2.0//EN".toLowerCase(), SHARD_CONFIG_DTD);
	}
	
	
	/**
	 * 已经解析SQL缓存
	 */
	private static final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<String, Boolean>();
	
	/**
	 * 配置文件类路径
	 */
	private String configFile  = null ;
	
	/**
	 * SQL 处理器工厂
	 */
	private SqlProcessorFactory sqlProcessorFactory  = null ;
	
	/**
	 * SQL转换策略注册
	 */
	private Map<String, ProcessStrategy> strategyRegister = new HashMap<String, ProcessStrategy>();

	/**
	 * 忽略SQL列表
	 */
	private Set<String> ignoreSet;
	
	/**
	 * 解析SQL列表
	 */
	private Set<String> parseSet;

	
	/**
	 * 默认构造器
	 * @param configFile  指定配置文件类路径
	 */
	private SqlProcessorConfigFactory(String configFile) {
		this.configFile = configFile ;
		parser(configFile) ;
		this.sqlProcessorFactory = new SqlProcessorFactory(this);
	}
	
	/**
	 * <p>方法名称：parser</p>
	 * <p>方法描述：配置文件解析处理</p>
	 * @param config     配置文件类路径
	 * @author franklin
	 * @since  Jun 17, 2015
	 * <p> history Jun 17, 2015 franklin  创建   <p>
	 */
	private void parser(String config) {
		
		InputStream input  = null ;
		
		try {
			input = Resources.getResourceAsStream(config);
			parser(input);
		} catch (IOException e) {
			log.error("Get  config file failed.", e);
			throw new IllegalArgumentException(e);
		} catch (ParserConfigurationException e) {
			log.error("Parse config file failed.", e);
			throw new IllegalStateException(e);
		} catch (SAXException e) {
			log.error("Parse config file failed.", e);
			throw new IllegalStateException(e);
		} catch (Exception e){
			log.error("Parse config file failed.", e);
			throw new IllegalStateException(e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
					throw new IllegalStateException(e);
				}
			}
		}
	}

	/**
	 * <p>方法名称：parser</p>
	 * <p>方法描述：解析配置文件</p>
	 * @param input       输入文件流
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @author franklin
	 * @since  Jun 17, 2015
	 * <p> history Jun 17, 2015 franklin  创建   <p>
	 */
	private void parser(InputStream input) throws ParserConfigurationException,	SAXException, IOException {
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(true);
		spf.setNamespaceAware(true);
		SAXParser parser = spf.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		
		// 解析XML实现
		DefaultHandler handler = new DefaultHandler() {
			
			private String parentElement;
			
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				
				if ("strategy".equals(qName)) {// 解析<strategy/>节点
					// 解析<strategy tableName="xxx"/>
					String table = attributes.getValue("tableName");
					// 解析<strategy strategyClass="xxx"/>
					String className = attributes.getValue("strategyClass");
					try {
						
						Class<?> clazz = Class.forName(className);
						ProcessStrategy strategy = (ProcessStrategy) clazz.newInstance();
						SqlProcessorConfigFactory.this.register(table, strategy);
						
					} catch (ClassNotFoundException e) {
						throw new SAXException(e);
					} catch (InstantiationException e) {
						throw new SAXException(e);
					} catch (IllegalAccessException e) {
						throw new SAXException(e);
					} catch(Exception e){
						throw new RuntimeException(e);
					}
				}
				
				if("ignoreList".equals(qName)||"parseList".equals(qName)){
					parentElement=qName;
				}

			}
			
			public void characters (char ch[], int start, int length) throws SAXException {
				if("ignoreList".equals(parentElement)){
					SqlProcessorConfigFactory.this.addIgnoreId(new String(ch, start, length).trim());
				} else if("parseList".equals(parentElement)){
					SqlProcessorConfigFactory.this.addParseId(new String(ch, start, length).trim());
				}
			}
			
			public void error(SAXParseException e) throws SAXException {
				throw e;
			}

			public InputSource resolveEntity(String publicId, String systemId) 	throws IOException, SAXException {
				
				if (publicId != null)
					publicId = publicId.toLowerCase();
				if (systemId != null)
					systemId = systemId.toLowerCase();

				InputSource source = null;
				try {
					String path = DOC_TYPE_MAP.get(publicId);
					source = getInputSource(path, source);
					if (source == null) {
						path = DOC_TYPE_MAP.get(systemId);
						source = getInputSource(path, source);
					}
				} catch (Exception e) {
					throw new SAXException(e.toString());
				}
				
				return source;
			}
		};
		
		reader.setContentHandler(handler);
		reader.setEntityResolver(handler);
		reader.setErrorHandler(handler);
		reader.parse(new InputSource(input));
	}
	
	/**
	 * <p>方法名称：getInputSource</p>
	 * <p>方法描述：获取XML配置类型定义文件</p>
	 * @param path    文件路径
	 * @param source  文件输入源
	 * @return InputSource
	 * @author franklin
	 * @since  Jun 17, 2015
	 * <p> history Jun 17, 2015 franklin  创建   <p>
	 */
	private InputSource getInputSource(String path, InputSource source) {
		if (path != null) {
			InputStream in = null;
			try {
				in = Resources.getResourceAsStream(path);
				source = new InputSource(in);
			} catch (IOException e) {
				log.warn(e.getMessage());
			}
		}
		return source;
	}

	/**
	 * <p>方法名称：getInstance</p>
	 * <p>方法描述：获取SQL处理器配置工厂</p>
	 * @param configFile  配置文件类路径
	 * @return SqlProcessorConfigFactory
	 * @author franklin
	 * @since  Jun 17, 2015
	 * <p> history Jun 17, 2015 franklin  创建   <p>
	 */
	public static synchronized SqlProcessorConfigFactory getInstance(String configFile) {
		
		if(instance == null ){
			instance =  new SqlProcessorConfigFactory(configFile) ;
		}
		
		return instance ;
	}
	

	/**
	 * <p>方法名称：isShouldParse</p>
	 * <p>方法描述：是否需要解析处理: 逻辑待优化</p>
	 * @param mapperId    SQL MAPPER ID
	 * @return boolean
	 * @author franklin
	 * @since  Jun 17, 2015
	 * <p> history Jun 17, 2015 franklin  创建   <p>
	 */
	public boolean isShouldParse(String mapperId) {
		Boolean parse = cache.get(mapperId);
		
		if (parse != null) {//已被缓存
			return parse;
		}
		/*
		 * 1.<selectKey>不做解析
		 * 2.在ignoreList里的sql不用处理
		 * 3.如果不在ignoreList里并且没有配置parseList则进行处理
		 * 4.如果不在ignoreList里并且也在parseList里则进行处理
		 * 5.如果不在ignoreList里并且也不在parseList里则不进行处理
		 */
		if (!mapperId.endsWith("!selectKey")) {

			if (!this.isIgnoreId(mapperId)) {
				if (!this.isConfigParseId()
						|| this.isParseId(mapperId)) {

					parse = true;
				}
			}
		}
		if (parse == null) {
			parse = false;
		}
		cache.put(mapperId, parse);
		return parse;
	}
	
	/**
	 * 注册分表策略
	 * 
	 * @param table
	 * @param strategy
	 */
	public void register(String table, ProcessStrategy strategy) {
		this.strategyRegister.put(table.toLowerCase(), strategy);
	}

	/**
	 * 查找对应表的分表策略
	 * 
	 * @param table
	 * @return
	 */
	public ProcessStrategy getStrategy(String table) {
		return strategyRegister.get(table.toLowerCase());
	}

	/**
	 * 增加ignore id配置
	 * 
	 * @param id
	 */
	public synchronized void addIgnoreId(String id) {
		if (ignoreSet == null) {
			ignoreSet = new HashSet<String>();
		}
		ignoreSet.add(id);
	}

	/**
	 * 增加parse id配置
	 * 
	 * @param id
	 */
	public synchronized void addParseId(String id) {
		if (parseSet == null) {
			parseSet = new HashSet<String>();
		}
		parseSet.add(id);
	}

	/**
	 * 判断是否配置过parse id<br>
	 * 如果配置过parse id,shardbatis只对parse id范围内的sql进行解析和修改
	 * 
	 * @return
	 */
	public boolean isConfigParseId() {
		return parseSet != null;
	}

	/**
	 * 判断参数ID是否在配置的parse id范围内
	 * 
	 * @param id
	 * @return
	 */
	public boolean isParseId(String id) {
		return parseSet != null && parseSet.contains(id);
	}

	/**
	 * 判断参数ID是否在配置的ignore id范围内
	 * 
	 * @param id
	 * @return
	 */
	public boolean isIgnoreId(String id) {
		return ignoreSet != null && ignoreSet.contains(id);
	}

	/**
	 * <p>方法名称：getConfigFile</p>
	 * <p>方法描述：获取配置文件类路径</p>
	 * @return String
	 * @author franklin
	 * @since  Jun 17, 2015
	 * <p> history Jun 17, 2015 franklin  创建   <p>
	 */
	public String getConfigFile() {
		return configFile;
	}
	
	
	
	/**
	 * <p>方法名称：getSqlProcessorFactory</p>
	 * <p>方法描述：获取SQL处理器工厂</p>
	 * @return SqlProcessorFactory
	 * @author franklin
	 * @since  Jun 18, 2015
	 * <p> history Jun 18, 2015 franklin  创建   <p>
	 */
	public SqlProcessorFactory getSqlProcessorFactory() {
		return sqlProcessorFactory;
	}

	/**
	 * <p>方法名称：close</p>
	 * <p>方法描述：释放必要资源</p>
	 * @throws IOException
	 * @author franklin
	 * @since Jun 17, 2015
	 */
	@Override
	public void close() throws IOException {
		this.configFile  = null ;
		instance = null ;
		cache.clear() ;
	}
	
}
