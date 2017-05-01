package com.test.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库操作辅助工具类
 * 功能：获取数据库连接对象，关闭连接，执行更新语句，执执查询语句
 * 对于Oracle11g，驱动的一般位置在：D:\Oracle\app\oracle\product\11.2.0\server\jdbc\lib\ojdbc6_g.jar
 * 如果使用连接池，需做如下配置：
 * 1、找到tomcat的context.xml文件，在<Context>下级加上：
  		<!-- XML文件中不允许出现“&”符号，所以必须使用HTML转义字符 -->
  		<Resource name="mysqljdbc" auth="Container" type="javax.sql.DataSource" maxActive="100" maxIdle="30" maxWait="10000" username="karaoke" password="karaoke123" driverClassName="com.mysql.jdbc.Driver" url="jdbc:mysql://172.16.4.253:3306/children_kalaok?useUnicode=true&amp;characterEncoding=utf-8"/>
 * 2、在工程的web.xml里面加上：
 		<resource-ref>
			<res-ref-name>mysqljdbc</res-ref-name>
			<res-type>javax.sql.DataSource</res-type>
			<res-auth>Container</res-auth>
		</resource-ref>
 * @start 2012年某月某日
 * @last 2014年7月3日
 * @version 1.0
 * @author LXA
 */
public class JdbcUtil
{
	private static final Logger log = LoggerFactory.getLogger(JdbcUtil.class);//日志对象
	private Connection con = null;//创建的连接对象
	private PreparedStatement ps = null;//创建预编译语句对象
	private ResultSet rs = null;//创建结果集对象
	
	/**
	 * 无参的构造方法，使用默认配置的一些东西，默认情况下不使用连接池
	 */
	public JdbcUtil()
	{
		String url="jdbc:mysql://172.16.4.253:3306/new-health-province?useUnicode=true&characterEncoding=utf-8";
		String user="test";
		String password="test";
		getConnection("mysql", url, user, password);
	}
	
	/**
	 * 使用JNDI获取连接，需事先在tomcat以及项目的web.xml里面配置好各种参数
	 */
	public JdbcUtil(String jndi)
	{
		getConnection(jndi);
	}
	
	/**
	 * 有参的构造方法
	 * @param url
	 * @param user 用户名
	 * @param password 
	 */
	public JdbcUtil(String url,String user,String password)
	{
		getConnection("mysql", url, user, password);
	}
	
	/**
	 * 有参的构造方法
	 * @param url
	 * @param user 用户名
	 * @param password 
	 */
	public JdbcUtil(String database,String url,String user,String password)
	{
		getConnection(database, url, user, password);
	}
	
	/**
	 * 普通的获取数据库连接
	 * @param database 数据库种类，可以是mysql、orcal
	 * @param url 数据库连接的URL
	 * 		Oracle示例：jdbc:oracle:thin:@192.168.3.228:1521:XE，
	 * 		MySQL示例：jdbc:mysql://172.16.4.253:3306/new-health-province?useUnicode=true&characterEncoding=utf-8
	 * @param user 数据库连接的用户名
	 * @param password 数据库连接的密码
	 * @return 创建的连接对象
	 */
	private Connection getConnection(String database,String url,String user,String password)
	{
		try
		{
			String className = "";
			if("mysql".equals(database))
				className="com.mysql.jdbc.Driver";
			else if("orcal".equals(database))
				className="oracle.jdbc.driver.OracleDriver";
			Class.forName(className);
			log.debug("开始尝试连接数据库！");
			con = DriverManager.getConnection(url, user, password);
			log.debug("连接成功！");
		}
		catch (Exception e)
		{
			log.error("连接数据库失败！", e);
		}
		return con;
	}
	
	/**
	 * 获取连接，使用数据库连接池
	 * @param jndi 配置在tomcat的context.xml里面的东西
	 * @return 创建的连接对象
	 */
	public Connection getConnection(String jndi)
	{
		try
		{
			log.debug("开始尝试连接数据库！");
			Context context=new InitialContext();
			DataSource dataSource=(DataSource)context.lookup("java:comp/env/"+jndi);
			con=dataSource.getConnection();
			log.debug("连接成功！");
		}
		catch (Exception e)
		{
			log.error("连接数据库失败！", e);
		}
		return con;
	}
	
	/**
	 * 关闭所有占有的资源
	 */
	public void closeAll()
	{
		try
		{
			if(rs!=null)
				rs.close();
			if(ps!=null)
				ps.close();
			if (con != null)
				con.close();
			log.debug("数据库连接已关闭！");
		}
		catch (Exception e)
		{
			log.error("尝试关闭数据库连接时出错：", e);
		}
	}
	
	/**
	 * 执行数据库的更新操作，包括增、删、改，执行后需手动关闭数据库连接
	 * @param sql 需要执行的预编译语句
	 * @param params 预编译语句的参数列表
	 * @return 受影响的行数
	 */
	public int update(String sql,String... params)
	{
		log.debug("执行SQL："+sql);
		int count = 0;//受影响的行数
		try
		{
			ps=con.prepareStatement(sql);
			for(int i=0;i<params.length;i++)
				ps.setString(i+1, params[i]);
			count=ps.executeUpdate();
		}
		catch (SQLException e)
		{
			log.debug("执行update时出错：", e);
		}
		log.debug("受影响的行数:{}", count);
		return count;
	}
	
	/**
	 * 执行数据库的更新操作，执行后会自动关闭数据库连接
	 * @param sql 需要执行的预编译语句
	 * @param params 预编译语句的参数列表
	 * @return 受影响的行数
	 */
	public int updateWithClose(String sql,String... params)
	{
		int count = update(sql, params);
		closeAll();//关闭连接
		return count;
	}
	
	/**
	 * 执行数据库的查询操作
	 * @param sql 需要执行的预编译语句
	 * @param params 预编译语句的参数列表
	 * @return 返回查询的结果集，类型为ResultSet
	 */
	public ResultSet query(String sql,String... params)
	{
		log.debug("执行查询SQL："+sql);
		try
		{
			ps=con.prepareStatement(sql);
			for(int i=0;i<params.length;i++)
				ps.setString(i+1, params[i]);
			rs=ps.executeQuery();
		}
		catch (SQLException e)
		{
			log.debug("执行query时出错：", e);
		}
		//由于查询是返回结果集，在调用此方法的时候还要用ResultSet.Next()的方法，
		//所以这里还不能关闭数据库连接
		return rs;
	}
	
	/**
	 * 执行数据库的查询操作，带分页
	 * @param sql 需要执行的预编译语句
	 * @param params 预编译语句的参数列表
	 * @return 返回查询的结果集，类型为ResultSet
	 */
	public ResultSet queryByPage(int page,int pageSize, String sql,String... params)
	{
		sql = sql + " limit "+(page-1)*pageSize+","+pageSize;
		return query(sql, params);
	}
	
	/**
	 * 查询结果集第一行的第一列的int型数据，一般都是获取count(*)的值
	 * @param sql 需要执行的预编译语句
	 * @param params 预编译语句的参数列表
	 * @return 返回的int型数据
	 */
	public int queryInt(String sql,String... params)
	{
		ResultSet resultSet = query(sql, params);
		int result = 0;
		try
		{
			if(resultSet.next())
				result = resultSet.getInt(1);//注意第一个索引是1，一般都是获取count(*)的值
		}
		catch (Exception e)
		{
			log.error("执行queryInt出错：", e);
		}
		return result;
	}
	/**
	 * 查询结果集第一行的第一列的String型数据
	 * @param sql 需要执行的预编译语句
	 * @param params 预编译语句的参数列表
	 * @return 返回的int型数据
	 */
	public String queryString(String sql,String... params)
	{
		ResultSet rs = query(sql, params);
		String result = "";
		try
		{
			if(rs.next())
				result = rs.getString(1);//注意第一个索引是1
		}
		catch (Exception e)
		{
			log.error("执行queryString出错：", e);
		}
		return result;
	}
	
	/**
	 * 查询结果集最后一行的第一列的int型数据，一般都是获取count(*)的值
	 * @param sql 需要执行的预编译语句
	 * @param params 预编译语句的参数列表
	 * @return 返回的int型数据
	 */
	public int queryIntWithClose(String sql,String... params)
	{
		int result = queryInt(sql, params);
		closeAll();
		return result;
	}
	
	public static void main(String[] args) throws Exception
	{
		//关于查询的示例代码
		JdbcUtil jdbc = new JdbcUtil();
		String code = "103104000003";
		ResultSet rs = jdbc.query("select * from video where `code`=? ", code);
		while(rs.next())
		{
			System.out.println(rs.getInt("video_id"));
			System.out.println(rs.getString("player"));
			System.out.println(rs.getDate("upload_date"));
			System.out.println(rs.getString(1));//注意索引从1开始
		}
		jdbc.closeAll();
	}
}
