package mandi.selenium.basic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class TestCon {
	
	public static final String url="jdbc:mysql://localhost:3306/fb_crawl?serverTimezone=UTC&characterEncoding=utf8&useSSL=false";
	
	public static void testDriver(String a){
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("driver not found!");
		}
		System.out.println(a);
	}
	
public static boolean findTable(String tableName) {
		
		String sql = "select * from information_schema.tables " + 
				"where TABLE_NAME = '" + tableName +
				"' and table_schema = 'fb_data'";
		
		boolean returnvalue = true;
		int a = 3;
		
		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
			ResultSet rs  = con.createStatement().executeQuery(sql);
			
			
			if (rs.next()) {
				returnvalue =  true;
					a =1;
				
			}else {
				returnvalue =  false;
				a=2;
		    }
	

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("a is ************" +a );
			return returnvalue;	
	}




	public static void createTable(String tablename) {
		String sql = "CREATE TABLE " + tablename +"(Id int unique auto_increment,postDate Timestamp,postContent varchar(255), likeNr varchar(255),"
				+ "loveNr varchar(255), hahaNr varchar(255), WowNr varchar(255), AngryNr varchar(255), SadNr varchar(255),"
				+ "currentUrl varchar(255), changePostDate int)";
				
		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
			java.sql.PreparedStatement prestate = con.prepareStatement(sql);
			
			prestate.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void addMemberOfGroup(String tablename,Timestamp postDate,String postContent,String likeNr, String loveNr, 
			String hahaNr, String WowNr,
			String AngryNr, String SadNr,String currentUrl,int changePostDate) {
		testDriver("Start adding memberOfGroup");

		String sql = "insert into fb_data."+ tablename 
				+ " (postDate,postContent,likeNr,loveNr,hahaNr,WowNr,AngryNr,SadNr,currentUrl,changePostDate)"
				+ " values(?,?,?,?,?,?,?,?,?,?)";
		

		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updatePost(Post post) {
//		UPDATE Person SET FirstName = 'Fred' WHERE LastName = 'Wilson' 
		String tablename = "tales_of_reval";
		Timestamp postTimestamp =new java.sql.Timestamp(post.getPostTime().getTime());
		String postContent = post.getPostContent();
		String currentUrl = post.getCurrentUrl();
		int likeNr = Integer.parseInt(post.getLikeNr());
		int loveNr = Integer.parseInt(post.getLoveNr()); 
		int hahaNr = Integer.parseInt(post.getHahaNr());
		int WowNr = Integer.parseInt(post.getWowNr());
		int AngryNr = Integer.parseInt(post.getAngryNr());
		int SadNr = Integer.parseInt(post.getSadNr());
		int changePostDate = post.getChangePostDate();
		
				
		String sql = "UPDATE "+ tablename +" SET postDate = ?,postContent=?,likeNr=?,loveNr=?,"
				+ "hahaNr=?,WowNr=?,AngryNr=?,SadNr=?,changePostDate=? WHERE currentUrl=?";
				
		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
			java.sql.PreparedStatement prestate = con.prepareStatement(sql);
			
			prestate.setTimestamp(1, postTimestamp);
			prestate.setString(2, postContent);
			prestate.setInt(3, likeNr);
			prestate.setInt(4, loveNr);
			prestate.setInt(5, hahaNr);
			prestate.setInt(6, WowNr);
			prestate.setInt(7, AngryNr);
			prestate.setInt(8, SadNr);
			prestate.setInt(9, changePostDate);
			prestate.setString(10, currentUrl);
			
			prestate.executeUpdate();
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static List<String> readINSheetname() {
		String sql = "select websiteName from table_name";
		List<String> sheetNameList = new ArrayList<String>();
		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
			ResultSet rs  = con.createStatement().executeQuery(sql);
			
			
			while (rs.next()) {
				sheetNameList.add(rs.getString("websiteName"));
			}
	

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sheetNameList;
		
	}
	
	
	
	
	
	
	
	
	
	private static final String EXCEL_XLS = "xls";  
	   
    public static Workbook getWorkbok(InputStream in,File file) throws IOException{  
        Workbook wb = null;  
       
            wb = new HSSFWorkbook(in);  
   
        return wb;  
    }
	
	 private static Object getValue(Cell cell) {
	    	Object obj = null;
	    	switch (cell.getCellTypeEnum()) {
		        case BOOLEAN:
		            obj = cell.getBooleanCellValue(); 
		            break;
		        case ERROR:
		            obj = cell.getErrorCellValue(); 
		            break;
		        case NUMERIC:
		            obj = cell.getNumericCellValue(); 
		            break;
		        case STRING:
		            obj = cell.getStringCellValue(); 
		            break;
		        default:
		            break;
	    	}
	    	return obj;
	    }
	 
	public static void get() {
	    try {  
	        // ͬʱ֧��Excel 2003��2007  
	        File excelFile = new File("f:\\zky\\task1_fb\\websites.xls"); // �����ļ�����  
	        FileInputStream in = new FileInputStream(excelFile); // �ļ���  
	        Workbook workbook = getWorkbok(in,excelFile);  
	
	        Sheet sheet = workbook.getSheetAt(0);   // ������һ��Sheet  
	        
	        //��ȡ������
	//      System.out.println(sheet.getLastRowNum());
	        
	        // Ϊ������һ��Ŀ¼����count  
	        int count = 0;
	        for (Row row : sheet) {
	        	try {
	        		// ������һ�е�Ŀ¼  
	                if(count < 1 ) {
	                    count++;  
	                    continue;  
	                }
	                
	                //�����ǰ��û�����ݣ�����ѭ��  
	                if(row.getCell(0).toString().equals("")){  
	                	return;
	                }
	
	                int end = row.getLastCellNum();
	                for (int i = 0; i < end; i++) {
	                	Cell cell = row.getCell(i);
	                	if(cell == null) {
	                		System.out.print("null" + "\t");
	                		continue;
	                	}
	                	
	                	Object obj = getValue(cell);
	                	String objStr = obj.toString().replaceAll(" ", "_");
	                	writeInWebsitesNameToDB(objStr);
	                	System.out.print(obj + "\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }
	}
	
	public static void readCsv() {
		 try { 
             BufferedReader reader = new BufferedReader(new FileReader("f:\\zky\\task1_fb\\test.csv"));//换成你的文件名
//             reader.readLine();//第一行信息，为标题信息，不用，如果需要，注释掉
             String line = null; 
             line = reader.readLine();
             while((line=reader.readLine())!=null){ 
                 String item[] = line.split("，");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                  
                 String last = item[item.length-1];//这就是你要的数据了
                 //int value = Integer.parseInt(last);//如果是数值，可以转化为数值
                 last= last.replaceAll(" ", "_");
             	writeInWebsitesNameToDB(last);

                 System.out.println(last); 
             } 
         } catch (Exception e) { 
             e.printStackTrace(); 
         } 
	}
	
	public static void writeInWebsitesNameToDB(String name) {
		testDriver("Start adding websites");

		String presql1 = "ALTER DATABASE fb_crawl CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
		String presql2 = "ALTER TABLE table_name CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
		String presql4 = "set character_set_client = utf8mb4";
		String presql5 = "set character_set_connection = utf8mb4";
		String presql6 = "set character_set_server = utf8mb4";
		String presql3 = "SHOW VARIABLES LIKE '%character%'";
		String sql = "insert into fb_crawl.table_name (websiteName,isPage) values('" +name+ "','0')";
		

		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
//			Statement stmt = con.createStatement
//				      (ResultSet.TYPE_SCROLL_SENSITIVE,
//				      ResultSet.CONCUR_UPDATABLE);
			java.sql.PreparedStatement prestate1 = con.prepareStatement(presql1);
			prestate1.executeUpdate();
			
			java.sql.PreparedStatement prestate2 = con.prepareStatement(presql2);
			prestate2.executeUpdate();
			
			java.sql.PreparedStatement prestate4 = con.prepareStatement(presql4);
			prestate4.executeUpdate();
			
			java.sql.PreparedStatement prestate5 = con.prepareStatement(presql5);
			prestate5.executeUpdate();
			
			java.sql.PreparedStatement prestate6 = con.prepareStatement(presql6);
			prestate6.executeUpdate();
			
			ResultSet rs  = con.createStatement().executeQuery(presql3);
			List<String> utf = new ArrayList<String>();
			
			while (rs.next()) {
				utf.add(rs.getString("value"));
			}
			
			for(int i = 0 ; i <utf.size();i++) {
				System.out.println(utf.get(i));
			}

			java.sql.PreparedStatement prestate = con.prepareStatement(sql);
//			prestate.setString(1, name);	
			prestate.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
//		String fromDateStr = "2018-01-01 00:00:00";
//	    Date postDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fromDateStr);
//	    Timestamp timestamp = new java.sql.Timestamp(postDate.getTime());
//		java.sql.Date date=java.sql.Date.valueOf(fromDateStr);



//		testDriver("Testing driver");
//		createTable("tales_of_reval");	
//		addMemberOfGroup("test11",timestamp,"a","1", "2", 
//				"3", "4",
//				"5", "6","7",0);
//		findTable("test4");

		
//		Post post = new Post(postDate, fromDateStr, fromDateStr, fromDateStr, fromDateStr, fromDateStr, fromDateStr, fromDateStr, fromDateStr, 0);
//		post.setCurrentUrl("https://mbasic.facebook.com/story.php?story_fbid=527943787677098&id=427951557676322&refid=17&_ft_=top_level_post_id.527943787677098%3Atl_objid.527943787677098%3Acontent_owner_id_new.427951557676322%3Aoriginal_content_id.2300914159948927%3Athrowback_story_fbid.527943787677098%3Apage_id.427951557676322%3Aphoto_id.2300912239949119%3Apage_insights.%7B%22427951557676322%22%3A%7B%22role%22%3A1%2C%22page_id%22%3A427951557676322%2C%22post_context%22%3A%7B%22story_fbid%22%3A527943787677098%2C%22publish_time%22%3A1536139429%2C%22story_name%22%3A%22EntStatusCreationStory%22%2C%22object_fbtype%22%3A266%7D%2C%22actor_id%22%3A427951557676322%2C%22psn%22%3A%22EntStatusCreationStory%22%2C%22sl%22%3A4%2C%22dm%22%3A%7B%22isShare%22%3A1%2C%22originalPostOwnerID%22%3A2300914159948927%7D%2C%22targets%22%3A%5B%7B%22page_id%22%3A427951557676322%2C%22actor_id%22%3A427951557676322%2C%22role%22%3A1%2C%22post_id%22%3A527943787677098%2C%22share_id%22%3A2300914159948927%7D%5D%7D%2C%22110496952324003%22%3A%7B%22page_id%22%3A110496952324003%2C%22role%22%3A1%2C%22actor_id%22%3A427951557676322%2C%22psn%22%3A%22EntStatusCreationStory%22%2C%22attached_story%22%3A%7B%22role%22%3A1%2C%22page_id%22%3A110496952324003%2C%22post_context%22%3A%7B%22story_fbid%22%3A2300914159948927%2C%22publish_time%22%3A1534248059%2C%22story_name%22%3A%22EntStatusCreationStory%22%2C%22object_fbtype%22%3A266%7D%2C%22actor_id%22%3A110496952324003%2C%22psn%22%3A%22EntStatusCreationStory%22%2C%22sl%22%3A4%2C%22dm%22%3A%7B%22isShare%22%3A0%2C%22originalPostOwnerID%22%3A0%7D%7D%2C%22sl%22%3A4%2C%22dm%22%3A%7B%22isShare%22%3A0%2C%22originalPostOwnerID%22%3A0%7D%2C%22targets%22%3A%5B%7B%22page_id%22%3A110496952324003%2C%22actor_id%22%3A427951557676322%2C%22role%22%3A1%2C%22post_id%22%3A2300914159948927%2C%22share_id%22%3A0%7D%5D%7D%7D%3Athid.427951557676322%3A306061129499414%3A2%3A0%3A1538377199%3A-3141501382329515362&__tn__=%2AW-R#footer_action_list");
//		post.setAngryNr("10");
//		post.setChangePostDate(1);
//		post.setHahaNr("10");
//		post.setLikeNr("10");
//		post.setLoveNr("10");
//		post.setPostContent("aaa");
//		post.setPostTime(postDate);
//		post.setSadNr("10");
//		post.setWowNr("10");
//		updatePost(post);		

		
//		List<String> readIn = new ArrayList<String>();
//		readIn = readINSheetname();
//		for(int i = 0; i < readIn.size();i++) {
//			System.out.println("readIn name is "+ readIn.get(i));
//		}
//		writeInWebsitesNameToDB("a");
		get();
	}

}
