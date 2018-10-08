package mandi.selenium.basic;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import java.awt.CardLayout;
import javax.swing.SpringLayout;
import net.miginfocom.swing.MigLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.awt.Font;
import javax.swing.JTextArea;

public class FacebookCrawlerWindow {

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_3;
	private JTextField textField_2;
	private JTextField textField_4;
	private JTextField textField_5;
	
	
	private static WebDriver driver;
	private List<Post> readIn = new ArrayList<Post>();
	private boolean keepLooping = true;
	private int changePostDate = 0;
	private boolean writeToExcel = false;
	private static boolean searchPage = true;
	private static String sheetName;
	private String schema_name;
//	public static String url="jdbc:mysql://localhost:3306/fb_crawl?serverTimezone=UTC&characterEncoding=utf8&useSSL=false";
	public String url;
	private JLabel lblFacebookCrawler;

	public void setUrl(String url) {
		this.url = url;
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

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FacebookCrawlerWindow window = new FacebookCrawlerWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public FacebookCrawlerWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setFont(new Font("Bahnschrift", Font.PLAIN, 22));
		frame.setBounds(100, 100, 747, 554);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[87.00px,grow][250.00][49.00px][40.00px][250]", "[20px][29px][40px][][40px][][][][40px][][40px][][40px][]"));
		
		lblFacebookCrawler = new JLabel("Facebook crawler");
		lblFacebookCrawler.setFont(new Font("Book Antiqua", Font.BOLD, 22));
		frame.getContentPane().add(lblFacebookCrawler, "cell 0 0");
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setFont(new Font("Book Antiqua", Font.BOLD, 22));
		frame.getContentPane().add(lblUsername, "cell 0 2,alignx left");
		
		textField = new JTextField();
		textField.setFont(new Font("Bahnschrift", Font.PLAIN, 22));
		frame.getContentPane().add(textField, "flowx,cell 1 2,growx");
		textField.setColumns(5);
		textField.setText("18200114569");

		
		JLabel lblFrom = new JLabel("From");
		lblFrom.setFont(new Font("Book Antiqua", Font.BOLD, 22));
		frame.getContentPane().add(lblFrom, "cell 3 2,alignx left");
		
		textField_2 = new JTextField();
		textField_2.setFont(new Font("Bahnschrift", Font.PLAIN, 22));
		frame.getContentPane().add(textField_2, "cell 4 2,growx");
		textField_2.setColumns(10);
		textField_2.setText("2018-01-01 00:00:00");

		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Book Antiqua", Font.BOLD, 22));
		frame.getContentPane().add(lblPassword, "cell 0 4,alignx left");
		
		textField_1 = new JTextField();
		textField_1.setFont(new Font("Bahnschrift", Font.PLAIN, 22));
		frame.getContentPane().add(textField_1, "flowx,cell 1 4,growx");
		textField_1.setColumns(10);
		textField_1.setText("Lmd960402");

		
		JLabel lblTo = new JLabel("To");
		lblTo.setFont(new Font("Book Antiqua", Font.BOLD, 22));
		frame.getContentPane().add(lblTo, "cell 3 4,alignx left");
		
		textField_3 = new JTextField();
		textField_3.setFont(new Font("Bahnschrift", Font.PLAIN, 22));
		frame.getContentPane().add(textField_3, "cell 4 4,growx");
		textField_3.setColumns(10);
		textField_3.setText("3018-01-01 00:00:00");

		

		
		JLabel lblDriverlocation = new JLabel("Driver Location");
		lblDriverlocation.setFont(new Font("Book Antiqua", Font.BOLD, 22));
		frame.getContentPane().add(lblDriverlocation, "cell 0 8,alignx left");
		
		textField_4 = new JTextField();
		textField_4.setFont(new Font("Bahnschrift", Font.PLAIN, 22));
		frame.getContentPane().add(textField_4, "cell 1 8 4 1,growx");
		textField_4.setColumns(10);
		textField_4.setText("F:\\mySelenium\\webdriver\\chromedriver.exe");
		
		JLabel lblSchemaName = new JLabel("Schema Name");
		lblSchemaName.setFont(new Font("Book Antiqua", Font.BOLD, 22));
		frame.getContentPane().add(lblSchemaName, "cell 0 10,alignx left");
		
		textField_5 = new JTextField();
		textField_5.setFont(new Font("Bahnschrift", Font.PLAIN, 22));
		frame.getContentPane().add(textField_5, "cell 1 10 4 1,growx");
		textField_5.setColumns(10);
		textField_5.setText("fb_crawl");
				
		JButton btnSubmit = new JButton("SUBMIT & RUN");
		btnSubmit.setFont(new Font("Book Antiqua", Font.BOLD, 22));
		btnSubmit.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				
				schema_name = textField_5.getText();
				url = "jdbc:mysql://localhost:3306/" + schema_name +"?serverTimezone=UTC&characterEncoding=utf8&useSSL=false";
		
				List<Website> readIn = new ArrayList<Website>();
				readIn = readINSheetname();
				for(int i = 0; i< readIn.size();i++) {
					sheetName = readIn.get(i).getWebsiteName();
					System.out.println("The crawling website is " + sheetName);
					
					int isPage = readIn.get(i).getIsPage();
					if(isPage == 0) {
						searchPage = true;
					}else {
						searchPage = false;
					}
					
					
					try {
						Main0919 myObj = new Main0919();
						
						
						
						myObj.setUsername(textField.getText());
						myObj.setPassword(textField_1.getText());
						myObj.setFromDate(textField_2.getText());
						myObj.setToDate(textField_3.getText());
						myObj.setDriverLocation(textField_4.getText());
						myObj.setSchemaName(textField_5.getText());
						myObj.setSheetName(sheetName);
						myObj.setSearchPage(searchPage);
						myObj.setUrl(url);
						
						myObj.invokeBrowser();
					} catch (TimeoutException e1) {
						driver.quit();
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}//mouse pressed
		});
		frame.getContentPane().add(btnSubmit, "cell 4 13,alignx right");


	}

}
