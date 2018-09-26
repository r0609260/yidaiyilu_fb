package mandi.selenium.basic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class WriteToExcel {
	
	private List<Post> postList = new ArrayList<Post>();
	private String sheetName;
	
	FileInputStream in;  
    POIFSFileSystem fs;  
    HSSFWorkbook wb; 
    HSSFSheet sheetCurrent;
    
    private final String fileLocation = "C:\\Users\\ASUS\\desktop\\facebook_0920_2.xls";

    public void writeOut(List<Post> posts,String inputSheetName) throws IOException {
    	postList = posts;
    	in = new FileInputStream(fileLocation);  
        fs = new POIFSFileSystem(in);  
        wb = new HSSFWorkbook(fs);  
        
        //sheetName should be smaller than 32;
        if(inputSheetName.length() > 30) {
        	sheetName = inputSheetName.substring(0, 29);
        }else {
        	sheetName = inputSheetName;
        }
        
        sheetCurrent = wb.getSheet(sheetName);
        
        if (sheetCurrent != null) {
        	int rowNr = sheetCurrent.getLastRowNum();
        	writeIntoSheet(rowNr);
        }
        else {
//          @SuppressWarnings("resource")
//  		HSSFWorkbook workbook = new HSSFWorkbook();
          sheetCurrent = wb.createSheet(sheetName);
          
          
              
          HSSFRow row = sheetCurrent.createRow(0);
          HSSFCell cell = row.createCell(0);
          cell.setCellValue("postId");
          cell = row.createCell(1);
          cell.setCellValue("postDate(localTime)");
          cell = row.createCell(2);
          cell.setCellValue("postContent");
          cell = row.createCell(3);
          cell.setCellValue("likeNr");
          cell = row.createCell(4);
          cell.setCellValue("loveNr");
          cell = row.createCell(5);
          cell.setCellValue("hahaNr");
          cell = row.createCell(6);
          cell.setCellValue("WowNr");
          cell = row.createCell(7);
          cell.setCellValue("AngryNr");
          cell = row.createCell(8);
          cell.setCellValue("SadNr");
          cell = row.createCell(9);
          cell.setCellValue("CurrentUrl");
          writeIntoSheet(0);
        }
        
    
    }
    
    public void writeIntoSheet(int rowNr) {
    	CellStyle styleRed = wb.createCellStyle();
        styleRed.setFillForegroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        styleRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        CellStyle styleGreen = wb.createCellStyle();
        styleRed.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex());
        styleRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    	for (int i = 0; i < postList.size(); i++) {
            HSSFRow row1 = sheetCurrent.createRow(rowNr + i + 1);
            Post post = postList.get(i);
            
            //Style 1
            
    	      
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");		
            String postDateStr = sdf.format(post.getPostTime());
            
            row1.createCell(0).setCellValue(rowNr + i+1);
            row1.createCell(1).setCellValue(postDateStr);
            if(post.getChangePostDate() == 1) {
            	row1.getCell(1).setCellStyle(styleRed);
            }else if (post.getChangePostDate() == 2) {
            	row1.getCell(1).setCellStyle(styleGreen);
            }
            row1.createCell(2).setCellValue(post.getPostContent());
            row1.createCell(3).setCellValue(post.getLikeNr());
            row1.createCell(4).setCellValue(post.getLoveNr());
            row1.createCell(5).setCellValue(post.getHahaNr());
            row1.createCell(6).setCellValue(post.getWowNr());
            row1.createCell(7).setCellValue(post.getAngryNr());
            row1.createCell(8).setCellValue(post.getSadNr());
            row1.createCell(9).setCellValue(post.getCurrentUrl());

        }

        //将文件保存到指定的位置
        try {
            FileOutputStream fos = new FileOutputStream(fileLocation);
            wb.write(fos);
            System.out.println("写入成功");
            fos.close();
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


        
}
