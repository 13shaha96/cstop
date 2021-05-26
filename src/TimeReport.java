import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.ArrayList;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;  
import java.util.Calendar;
import java.sql.Timestamp;

public class TimeReport{
	ArrayList<Timestamp> timeReportStemp= new ArrayList();
	Calendar  start= Calendar.getInstance();
	Calendar end = Calendar.getInstance();
	DecimalFormat df2 = new DecimalFormat("#0.00");
	ArrayList<String> timeReport = new ArrayList();
	
	
	public TimeReport(int shift) {
		String sqlTimeReport = "Select min(transactionDate) as `start`, max(transactionDate) as `end` from cstop.transactiontotal";
		if (shift !=0) {
			sqlTimeReport= "Select min(transactionDate) as `start`, max(transactionDate) as `end` from cstop.transactiontotal where shiftNum=" + shift + ";";
		}
		
		Statement stmt = null;
		ResultSet rs = null;
		try 
		{
			SqlConn sqlInfo = new SqlConn();		  	  
			Connection conn = DriverManager.getConnection(sqlInfo.url, sqlInfo.username, sqlInfo.password);
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sqlTimeReport);
			while(rs.next()){
				start.setTime(rs.getTimestamp("start"));
				end.setTime(rs.getTimestamp("end"));
			}			
			rs.close();
			Calendar temp= start;
			while(end.after(temp)) {
				timeReportStemp.add( new Timestamp(temp.getTimeInMillis()));
				temp.add(Calendar.MINUTE, 30);
			}
			temp.add(Calendar.MINUTE, 30);
			timeReportStemp.add( new Timestamp(temp.getTimeInMillis()));
			
			for(int i=0; i<timeReportStemp.size()-1; i++) {
				String strStart = String.valueOf(timeReportStemp.get(i).getHours() + ":" + timeReportStemp.get(i).getMinutes());
				String strEnd = String.valueOf(timeReportStemp.get(i+1).getHours() + ":" + (timeReportStemp.get(i+1).getMinutes()-1));
				String qty = "0";
				int total =0;
				rs = stmt.executeQuery("SELECT count(distinct(transactionDate)) as qty, sum(price) as `total` FROM cstop.transactiondata where transactionDate>= '"+timeReportStemp.get(i)+"' and transactionDate< '"+timeReportStemp.get(i+1)+"' and department != 15 and department != 14 and department != 99 and department !=1002 and department != 1000;");
				while(rs.next()){
					qty = (rs.getString("qty"));
					total = (rs.getInt("total"));
				}			
				rs.close();
				
				rs = stmt.executeQuery("SELECT sum(tax) as tax FROM cstop.transactiontotal where transactionDate>= '"+timeReportStemp.get(i)+"' and transactionDate< '"+timeReportStemp.get(i+1)+"';");
				while(rs.next()){
					
					total += (rs.getInt("tax"));
				}			
				rs.close();
				
				strStart =  String.format("%-6s",strStart);
				strStart += "- ";
				strStart +=  String.format("%-10s",strEnd);
				qty = String.format("%-5s",qty);
				String strTotal = String.format("%9s",df2.format(Double.parseDouble((String.valueOf(total)))/100));
				
				timeReport.add(strStart+qty+strTotal);
			}
			
		} catch(Exception e) {
			  System.out.print(e);
		  }
		  finally {
			    if (rs != null) {
			        try {
			            rs.close();
			        } catch (SQLException sqlEx) { } // ignore

			        rs = null;
			    }

			    if (stmt != null) {
			        try {
			            stmt.close();
			        } catch (SQLException sqlEx) { } // ignore

			        stmt = null;
			    }
		}
		
	}
	
}
