package mandi.selenium.basic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class WriteToDB {
	private String tablename;
	private String url;
	private String schema_name;

	
	private String presql1;
	private String presql4;
	private String presql5;
	private String presql6;

	
	public void writeDB(String inputTableName,List<Post> posts,String url,String schema_name ) {
		this.url = url;
		this.schema_name = schema_name;
		
		presql1 = "ALTER DATABASE "+ schema_name +" CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
	    presql4 = "set character_set_client = utf8mb4";
		presql5 = "set character_set_connection = utf8mb4";
	    presql6 = "set character_set_server = utf8mb4";
		
		testDriver();
		if(inputTableName.length() > 30) {
			tablename = inputTableName.substring(0, 29);
			tablename = tablename.replaceAll(" ", "_");
        }else {
        	tablename = inputTableName;
        	tablename = tablename.replaceAll(" ", "_");
        }
		
		boolean findtable = findTable(tablename);
		if(findtable == true) {
			writeInPostList(posts);
		}else {
			createTable(tablename);
			writeInPostList(posts);
		}
	}
	
	public void writeInPostList(List<Post> posts) {
		for(int i = 0; i < posts.size(); i++) {
			Post post = posts.get(i);
			if(findPost(post) == true) {
				updatePost(post);
			}
			else {
				writeInPost(post);
			}
		}
	}
	
	public void updatePost(Post post) {
		Timestamp postTimestamp =new java.sql.Timestamp(post.getPostTime().getTime());

		String sql = "UPDATE "+ tablename +" SET postDate = ?,postContent=?,likeNr=?,loveNr=?,"
				+ "hahaNr=?,WowNr=?,AngryNr=?,SadNr=?,changePostDate=? WHERE currentUrl=?";
				
		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
			prepareStatement(con);

			java.sql.PreparedStatement prestate = con.prepareStatement(sql);
			
			prestate.setTimestamp(1, postTimestamp);
			prestate.setString(2, post.getPostContent());
			prestate.setString(3, post.getLikeNr());
			prestate.setString(4, post.getLoveNr());
			prestate.setString(5, post.getHahaNr());
			prestate.setString(6, post.getWowNr());
			prestate.setString(7, post.getAngryNr());
			prestate.setString(8, post.getSadNr());
			prestate.setInt(9, post.getChangePostDate());
			prestate.setString(10, post.getCurrentUrl());
			
			prestate.executeUpdate();
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void writeInPost(Post post) {
			
		Timestamp timestamp = new java.sql.Timestamp(post.getPostTime().getTime()); 
		addPost(tablename, timestamp,post.getPostContent(),post.getLikeNr(),post.getLoveNr(),
				post.getHahaNr(),post.getWowNr(),post.getAngryNr(),post.getSadNr(),post.getCurrentUrl(),post.getChangePostDate());
		
	}	
	
	
	public void testDriver(){
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("driver not found!");
		}
	}
	
	public boolean findPost(Post post) {
		String sql = "select * from " + tablename + " where currentUrl = '" + post.getCurrentUrl() +"'";
		boolean returnvalue = true;		
		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
			prepareStatement(con);

			ResultSet rs  = con.createStatement().executeQuery(sql);
			
			if (rs.next()) {
				returnvalue =  true;
			}else {
				returnvalue =  false;
		    }

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return returnvalue;	
	}
	
	public boolean findTable(String tableName) {
		
		String sql = "select * from information_schema.tables " + 
				"where TABLE_NAME = '" + tableName +
				"' and table_schema = '"+ schema_name +"'";
		
		boolean returnvalue = true;		
		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
			prepareStatement(con);

			ResultSet rs  = con.createStatement().executeQuery(sql);
			
			if (rs.next()) {
				returnvalue =  true;
			}else {
				returnvalue =  false;
		    }

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return returnvalue;	
				
	}
	
	public void createTable(String tablename) {
		String sql = "CREATE TABLE " + tablename +"(Id int unique auto_increment,postDate Timestamp,postContent longtext, likeNr varchar(255),"
				+ "loveNr varchar(255), hahaNr varchar(255), WowNr varchar(255), AngryNr varchar(255), SadNr varchar(255),"
				+ "currentUrl longtext, changePostDate int) CHARSET utf8mb4 COLLATE utf8mb4_general_ci;";
				
		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
			prepareStatement(con);

			java.sql.PreparedStatement prestate = con.prepareStatement(sql);
			
			prestate.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addPost(String tablename,Timestamp postDate,String postContent,String likeNr, String loveNr, 
			String hahaNr, String WowNr,
			String AngryNr, String SadNr,String currentUrl,int changePostDate) {

		String sql = "insert into "+ schema_name +"."+ tablename 
				+ " (postDate,postContent,likeNr,loveNr,hahaNr,WowNr,AngryNr,SadNr,currentUrl,changePostDate)"
				+ " values(?,?,?,?,?,?,?,?,?,?)";

		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
			prepareStatement(con);
			java.sql.PreparedStatement prestate = con.prepareStatement(sql);
			prestate.setTimestamp(1, postDate);
			prestate.setString(2, postContent);
			prestate.setString(3, likeNr);
			prestate.setString(4, loveNr);
			prestate.setString(5, hahaNr);
			prestate.setString(6, WowNr);
			prestate.setString(7, AngryNr);
			prestate.setString(8, SadNr);
			prestate.setString(9, currentUrl);
			prestate.setInt(10, changePostDate);
				
			prestate.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void prepareStatement(Connection con) throws SQLException {
		java.sql.PreparedStatement prestate1 = con.prepareStatement(presql1);
		prestate1.executeUpdate();
		
		java.sql.PreparedStatement prestate4 = con.prepareStatement(presql4);
		prestate4.executeUpdate();
		
		java.sql.PreparedStatement prestate5 = con.prepareStatement(presql5);
		prestate5.executeUpdate();
		
		java.sql.PreparedStatement prestate6 = con.prepareStatement(presql6);
		prestate6.executeUpdate();
	}
	
}
