package com.mojian.config.mybatisplus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class JDBCUtils {
	//这三个常用的量，放在上面，想改的话直接就改了。
	private static final String connectionURL="jdbc:mysql://localhost:3306/hahahhha?useUnicode=true&characterEncoding=UTF8&useSSL=false&autoReconnect=true&failOverReadOnly=false&serverTimezone=GMT";
	private static final String username="root";
	private static final String password="password";
	private static ArrayList<Connection> conlist=new ArrayList<Connection>();//运用连接池
	static {//静态程序块：当一个类需要在被载入时就执行一段程序，这样可以使用静态程序块。这里我们一引用这个类就创建几个Connection放在连接池中
		for(int i=0;i<5;i++) {
			Connection con=createConnection();
			conlist.add(con);
		}
	}
	public static Connection getConnection()
	{
		if(conlist.isEmpty()==false)//判断连接池中是否还有连接
		{
			Connection con=conlist.get(0);
			conlist.remove(con);//还有连接的话由于此连接要被占用了，因此我们把它移除
			return con;
		}
		else {//如果所有连接都被占用了的话，那么再继续创建连接
			return createConnection();
		}
	}
	
	public static Connection createConnection(){
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			return DriverManager.getConnection(connectionURL,username,password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	//关闭资源的通用函数
	public static void close(Connection con,Statement stmt,ResultSet rs) {
		closeResultSet(rs);
		closeStatement(stmt);
		closeConnection(con);
	}
	//再写一个参数不同的函数，用于关闭有两个Statement时且无rs时，比如转账情况
	public static void close(Connection con,Statement stmt1,Statement stmt2) {
		closeStatement(stmt1);
		closeStatement(stmt2);
		closeConnection(con);
	}
	private static void closeResultSet(ResultSet rs) {
		try {
			if(rs!=null) rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void closeStatement(Statement stmt) {
		try {
			if(stmt!=null) stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void closeConnection(Connection con) {//关闭的时候不是真正的关闭，而是让他返回到连接池中。
		conlist.add(con);//这个连接已经被一个用户运用完毕了，因此再返回到连接池中以备下次使用
	}
}

