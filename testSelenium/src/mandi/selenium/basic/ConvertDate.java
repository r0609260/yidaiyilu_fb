package mandi.selenium.basic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ConvertDate {
	public Date convertDate(String perviousDate) {
//			String datePostStr = null;
			// TODO Auto-generated method stub
			//电脑上现在的时间
			Date date = new Date();  
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
//	        String dateNowStr = sdf.format(date);  
	        
	        //post时区现在的时间
	        Calendar calendar = Calendar.getInstance();
//	        calendar.add(Calendar.HOUR_OF_DAY, -3);
//	        date = calendar.getTime();
//	        calendar.setTime(date);
//			String datePostNowStr = sdf.format(date);
			
			
			String postTime = perviousDate;
			String [] splitString = postTime.split(" ");
			
			if(postTime.contains("mins") || postTime.contains("min")) {
				int minNr = Integer.parseInt(splitString[0]);
				calendar.add(Calendar.MINUTE,-minNr);
				date = calendar.getTime();
		        calendar.setTime(date);
//				datePostStr = sdf.format(date);
				
			
			}else if(postTime.contains("hrs") || postTime.contains("hr")) {
				int hrsNr = Integer.parseInt(splitString[0]);
				calendar.add(Calendar.HOUR_OF_DAY,-hrsNr);
				date = calendar.getTime();
		        calendar.setTime(date);
//				datePostStr = sdf.format(date);
			}else if(postTime.contains("Yesterday")) {
				
				String exactTime= splitString[2];
				String[] splitExactTime = exactTime.split(":");
				int hour = Integer.parseInt(splitExactTime[0]);
				int minute = Integer.parseInt(splitExactTime[1]);
				
				calendar.add(Calendar.DAY_OF_MONTH,-1);
				calendar.set(Calendar.HOUR_OF_DAY,hour);
				calendar.set(Calendar.MINUTE,minute);
				date = calendar.getTime();
		        calendar.setTime(date);
		        
			}else if(splitString.length == 4) {
				String exactTime= splitString[3];
				String[] splitExactTime = exactTime.split(":");
				int hour = Integer.parseInt(splitExactTime[0]);
				int minute = Integer.parseInt(splitExactTime[1]);
				
				int day = Integer.parseInt(splitString[0]);
				int month = getMonth(splitString[1]);
				
				calendar.set(Calendar.YEAR,2018);
				calendar.set(Calendar.MONTH,month);
				calendar.set(Calendar.DAY_OF_MONTH,day);
				calendar.set(Calendar.HOUR_OF_DAY,hour);
				calendar.set(Calendar.MINUTE,minute);
				date = calendar.getTime();
		        calendar.setTime(date);
			}
			else if(splitString.length == 5) {
				String exactTime= splitString[4];
				String[] splitExactTime = exactTime.split(":");
				int hour = Integer.parseInt(splitExactTime[0]);
				int minute = Integer.parseInt(splitExactTime[1]);
				
				int day = Integer.parseInt(splitString[0]);
				int month = getMonth(splitString[1]);
				int year = Integer.parseInt(splitString[2]);
				
				calendar.set(Calendar.YEAR,year);
				calendar.set(Calendar.MONTH,month);
				calendar.set(Calendar.DAY_OF_MONTH,day);
				calendar.set(Calendar.HOUR_OF_DAY,hour);
				calendar.set(Calendar.MINUTE,minute);
				date = calendar.getTime();
		        calendar.setTime(date);
			}
			
//			calendar.add(Calendar.HOUR_OF_DAY,3);
//			date = calendar.getTime();
//	        calendar.setTime(date);
//			datePostStr = sdf.format(date);
			
			return date;
	}
	
	
	public int getMonth(String monthInput) {
		if(monthInput.equals("January")) {
			return 0;
		}else if(monthInput.equals("February")){
			return 1;
		}else if(monthInput.equals("March")){
			return 2;
		}else if(monthInput.equals("April")){
			return 3;
		}else if(monthInput.equals("May")){
			return 4;
		}else if(monthInput.equals("June")){
			return 5;
		}else if(monthInput.equals("July")){
			return 6;
		}else if(monthInput.equals("August")){
			return 7;
		}else if(monthInput.equals("September")){
			return 8;
		}else if(monthInput.equals("October")){
			return 9;
		}else if(monthInput.equals("November")){
			return 10;
		}else if(monthInput.equals("December")){
			return 11;
		}
		return 12;
	}
}
