package mandi.selenium.basic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Period;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.WebElement;


public class Main0919 {

	private static WebDriver driver;
	private JavascriptExecutor jse;
	private List<Post> readIn = new ArrayList<Post>();
	private boolean keepLooping = true;
	private int changePostDate = 0;
	private boolean writeToExcel = false;
	
	private String username;
	private String password;
	
	private String fromDate;
	private String toDate;
	private String schema_name;
//	private final String toDate = "2018-06-19 07:11:52";
	
	private String sheetName;
	private final String crawlUrl = "https://mbasic.facebook.com/";
	private final int loopTime = 1000000;
	
	private boolean searchPage = true;
	
	public String url;
	
	private String driverLocation = "";
	private JButton btnExit;
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	
	public void setDriverLocation(String driverLocation) {
		this.driverLocation = driverLocation;
	}
	
	public void setSchemaName(String schemaName) {
		this.schema_name = schemaName;
	}
	
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	
	public void setSearchPage(boolean searchPage) {
		this.searchPage = searchPage;
	}
	
	public void invokeBrowser() {
		try {
			System.setProperty("webdriver.chrome.driver",driverLocation );
			ChromeOptions chromeOptions = new ChromeOptions();
			
			chromeOptions.addArguments("--window-position=-32000,-32000");
			chromeOptions.addArguments("window-size=1400,800");
			
			
			driver = new ChromeDriver(chromeOptions);

			driver.manage().deleteAllCookies();
//			driver.manage().window().maximize();
			driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
			
			driver.get(crawlUrl);
			login();
			
			//get all posts
			for(int i = 0; i < loopTime; i++) {
				if(keepLooping == false) {
					driver.quit();
					break;
				}else {
					System.out.println("###########################"+ i +"##############################");
					getPosts(fromDate,toDate);
					
					//write to excel
					if(writeToExcel == true) {
						WriteToExcel write = new WriteToExcel();
						write.writeOut(readIn,sheetName);
					}else {
						WriteToDB write = new WriteToDB();
						write.writeDB(sheetName,readIn,url,schema_name);
					}
					
					showMore();
					readIn.clear();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean getPosts(String fromDateStr, String toDateStr) throws InterruptedException, ParseException {
		keepLooping = true;
		boolean hasExpression;
		
		Date fromDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fromDateStr);
		Date toDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(toDateStr);

		if(fromDate.after(toDate)) {
			Date helpDate = fromDate;
			fromDate = toDate;
			toDate = helpDate;
		}
		
		List<WebElement> previousFullStory = driver.findElements(By.xpath("//*/div[2]/div[2]/a[4] | //*/div[3]/div[2]/a[4]"));

		for (int i = 0; i < previousFullStory.size(); i++) {
			//Deal with full story link, get again because the page is refreshed; the position is not persist;
			List<WebElement> fullStoryLink = driver.findElements(By.xpath("//*/div[2]/div[2]/a | //*/div[3]/div[2]/a"));			
			List<WebElement> posts = new ArrayList<WebElement>();
			for(WebElement w : fullStoryLink) {
				if (w.getText().equals("Full Story")) {
					posts.add(w);
				}
			}
			
			//Deal with expression
			List<WebElement> expressions = driver.findElements(By.xpath("//span[contains(@id,\"like_\")]/a[1]"));
			String expressionStr = null;
			if(i < expressions.size() ) {
				expressionStr = expressions.get(i).getText();
			}else {
				expressionStr = "Like";
			}
			
			if(expressionStr.equals("Like") ) {
				hasExpression = false;
			}else {
				hasExpression = true;
			}
						
			//Deal with postTime
			List<WebElement> postTimes = driver.findElements(By.xpath("//*/div[2]/div[1]/abbr | //*/div[3]/div[1]/abbr"));
			String postTime = postTimes.get(i).getText();
			System.out.println("postTime is" + postTime);
			String [] splitString = postTime.split(" ");
			
			if(postTime.contains("hr") 
					|| postTime.contains("hrs")
					|| postTime.contains("min") 
					|| postTime.contains("mins")) {
				changePostDate = 1;
			}else if(postTime.contains("Yesterday")) {
				changePostDate = 2;
			}
			
			Date postDate;
			Date fakePostDate =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("00-00-00 00:00:00");
			
			// if the time in the main page is different from the time in full story page(more accurate), then get the time in the full story page
			if((splitString.length == 2 && postTime.contains("hrs")!=true && postTime.contains("hr")!=true
					&& postTime.contains("mins")!=true && postTime.contains("min")!=true) || splitString.length == 1) {
				
				posts.get(i).click();
				Post newPost = getFullStory(fakePostDate, hasExpression);
				
				postDate = newPost.getPostTime();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");		
		        String postDateStr = sdf.format(postDate);
				System.out.println("Converted postTime is " + postDateStr);
				
			}
			
			// if the time in the main page is same as the time in full story page
			else {
				ConvertDate conDate = new ConvertDate();
				postDate = conDate.convertDate(postTime);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");		
		        String postDateStr = sdf.format(postDate);
				System.out.println("Converted postTime is" + postDateStr);
				
			}

			//if postDate after toDate, showMore; if postDate before fromDate, stop;
			if(postDate.after(toDate)) {
				continue;
			}else if(postDate.before(fromDate)){
				keepLooping = false;
			    break;
			}
			
			//get WebElements again because the page is refreshed
			fullStoryLink = driver.findElements(By.xpath("//*/div[2]/div[2]/a | //*/div[3]/div[2]/a"));
			posts = new ArrayList<WebElement>();
			for(WebElement w : fullStoryLink) {
				if (w.getText().equals("Full Story")) {
					posts.add(w);
				}
			}
					
			posts.get(i).click();
			Post newPost = getFullStory(postDate,hasExpression);
			
			readIn.add(newPost);	
			changePostDate = 0;
		}
		return keepLooping;
	}

	public Post getFullStory(Date inputPostDate, boolean hasExpression) throws InterruptedException, ParseException {

		WebElement eContent = null, eContent1 = null, eContent2 = null;

		String currentUrl = driver.getCurrentUrl();
		System.out.println("Current Url is" + currentUrl);
		eContent2 = driver.findElement(By.xpath("//div[contains(@id,'u_0_')]/div[1]/div[2]"
				+ "| //*[@id=\"root\"]/table/tbody/tr/td/div/div/div[1]/table/tbody/tr/td[1]/div/div[2]"
				+ "| //*[@id=\"root\"]/table/tbody/tr/td/div/div[2]"
				+ "| //*[@id=\"root\"]/table/tbody/tr/td/div[1]/table[3]/tbody/tr/td"));		
		String eContent2Text = eContent2.getText();		
		if(eContent2Text.equals("Like\nReact\nComment\nShare")) {
			eContent1 = driver.findElement(By.xpath("//*[@id=\"MPhotoContent\"]/div[1]/div[1]/div"
					+"| //*[@id=\"MPhotoContent\"]/div[1]/table/tbody/tr/td[1]/table/tbody/tr/td[2]/div"));
			eContent = eContent1;
		}
		else {
			eContent = eContent2;
		}
		String postContent = eContent.getText();	
		
		Date fullstoryPostDate = new Date();
		Date fakePostDate =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("00-00-00 00:00:00");
		
		if(inputPostDate.equals(fakePostDate)) {
			WebElement eTime;
			eTime = driver.findElement(By.xpath("//*[@id=\"u_0_0\"]/div[2]/div[1]/abbr"
					+"| //*[@id=\"u_0_1\"]/div[2]/div[1]/abbr"
					+"| //*[@id=\"MPhotoContent\"]/div[1]/div[2]/span/div/div/abbr"
					+"| //*[@id=\"root\"]/table/tbody/tr/td/div/div/div[1]/table/tbody/tr/td[1]/div/span/abbr"));	
			String postTime = eTime.getText();
			
			if(postTime.contains("hr") 
					|| postTime.contains("hrs")
					|| postTime.contains("min") 
					|| postTime.contains("mins")) {
				changePostDate = 1;
			}else if(postTime.contains("Yesterday")) {
				changePostDate = 2;
			}
			
			ConvertDate conDate = new ConvertDate();
			fullstoryPostDate = conDate.convertDate(postTime);

		}else {
			fullstoryPostDate = inputPostDate;
		}
		
		//Click to get expressions
		List<String> expressionNrString = new ArrayList<String>();

		if(hasExpression == true) {
			driver.findElement(By.xpath("//div[contains(@id,'sentence_')]/a")).click();
			
			List<WebElement> expressionNr = driver.findElements
					(By.xpath("//*[@id=\"root\"]/table/tbody/tr/td/div/div/a/span"));
			
			for(WebElement w : expressionNr) {
				expressionNrString.add(w.getText());
			}
			for(int i = expressionNr.size(); i < 6; i++) {
				expressionNrString.add("0");
			}

			driver.navigate().back();
		}else {
			for(int i = 0; i < 6; i++) {
				expressionNrString.add("0");
			}
		}
		
		Post newPost = new Post(fullstoryPostDate,
				postContent,
				expressionNrString.get(0),
				expressionNrString.get(1),
				expressionNrString.get(2),
				expressionNrString.get(3),
				expressionNrString.get(4),
				expressionNrString.get(5),
				currentUrl,
				changePostDate);
		
		
		driver.navigate().back();
		
		return newPost;
	}
	
	
	public void showMore() {
		WebElement showMoreElement = driver.findElement(By.xpath("//*[@id=\"structured_composer_async_container\"]/div[2]/a\r\n"));
		if (showMoreElement.getText().equals("Show more") || showMoreElement.getText().equals("See more stories")) {
			showMoreElement.click();
		}else {
			keepLooping = false;
		}

    }
	
	public List<Website> readINSheetname() {
		String sql = "select * from table_name";
		List<Website> websiteList = new ArrayList<Website>();
		try {
			Connection con = DriverManager.getConnection(url, 
					"root", "root");
			ResultSet rs  = con.createStatement().executeQuery(sql);
			
			
			while (rs.next()) {
				String sheetName = rs.getString("websiteName").replaceAll("_", " ");
				int isPage = rs.getInt("isPage");
				Website w = new Website(sheetName,isPage);
				websiteList.add(w);
			}
	

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return websiteList;
		
	}
	
	public void login() {
		try {
			//login
			driver.findElement(By.id("m_login_email")).sendKeys(username);
			driver.findElement(By.name("pass")).sendKeys(password);			
			//sleep for 2 seconds for the page to be loaded
			Thread.sleep(2000);
			driver.findElement(By.name("login")).click();
			
			//remember device
			driver.findElement(By.xpath("//*[@id=\"root\"]/table/tbody/tr/td/div/form/div/input")).click();	
			
			//search
			
			driver.findElement(By.name("query")).sendKeys(sheetName);
			Thread.sleep(1000);
			driver.findElement(By.xpath("//*[@id=\"header\"]/form/table/tbody/tr/td[3]/input")).click();
			
			if(searchPage == true) {
				driver.findElement(By.xpath("//*[@id=\"BrowseResultsContainer\"]/div[1]/div/div[2]/table/tbody/tr/td[2]/a\r\n")).click();
			}else {
				driver.findElement(By.xpath("//*[@id=\"root\"]/div[1]/div[1]/div/a[2]")).click();
				driver.findElement(By.xpath("//*[@id=\"root\"]/table/tbody/tr/td/ul/li[3]/table/tbody/tr/td/a")).click();
				driver.findElement(By.xpath("//*[@id=\"BrowseResultsContainer\"]/div[1]/div/div[1]/table/tbody/tr/td[2]/a")).click();
				driver.findElement(By.xpath("//*[@id=\"root\"]/div[1]/div[1]/div[4]/a[1]")).click();
			}
			//scroll down
//			jse = (JavascriptExecutor)driver;
//			jse.executeScript("scroll(0,1000)");
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
//	public static void main(String[] args) {
//		
//		List<Website> readIn = new ArrayList<Website>();
//		readIn = readINSheetname();
////		for(int i = 0; i< readIn.size();i++) {
////				sheetName = readIn.get(i).getWebsiteName();
////				System.out.println("The crawling website is " + sheetName);
////				
////				int isPage = readIn.get(i).getIsPage();
////				if(isPage == 0) {
////					searchPage = true;
////				}else {
////					searchPage = false;
////				}
////				try {
//					Main0919 myObj = new Main0919();
//					myObj.invokeBrowser();
////				} catch (TimeoutException e) {
////					driver.quit();
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
////				
//			
////		}
//		
//	}

}
