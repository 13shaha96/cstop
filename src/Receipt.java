import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Receipt {
	DecimalFormat df2 = new DecimalFormat("#0.00");
	ArrayList<String> receiptView = new ArrayList();
	String trasactionId = "";
	String shiftNum = "";
	String transactionDate = "";
	String preTax = "0";
	String tax = "0";
	String total = "0";
	String cash = "0";
	String credit = "0";
	String check = "0";
	String change = "0";
	
	
	ArrayList<String> items = new ArrayList();
	
	public Receipt() {
		initReceipt();
		getView();
	}
	
	
	public void initReceipt() {
		Statement stmt = null;
		ResultSet rs = null;
		try 
		{
			SqlConn sqlInfo = new SqlConn();		  	  
			Connection conn = DriverManager.getConnection(sqlInfo.url, sqlInfo.username, sqlInfo.password);
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM cstop.counters;");
			while(rs.next()){
				trasactionId = String.valueOf(rs.getInt("transactionID")-1);
				shiftNum = rs.getString("shiftNum");    
			}
			rs.close();
			rs = stmt.executeQuery("SELECT * FROM cstop.transactiontotal where transactionId =" +trasactionId+ "  and shiftNum= " + shiftNum+";");
			
			while(rs.next()){
				transactionDate = String.valueOf(rs.getString("transactionDate"));
				preTax = String.valueOf(rs.getString("preTax"));    
				tax = String.valueOf(rs.getString("tax"));
				total = String.valueOf(rs.getString("total"));    
				cash = String.valueOf(rs.getString("cash"));
				credit = String.valueOf(rs.getString("credit"));    
				check = String.valueOf(rs.getString("checks"));
				change = String.valueOf(rs.getString("change"));    
			}
			rs.close();
			rs = stmt.executeQuery("SELECT * FROM cstop.transactiondata where transactionId = "+trasactionId+" and shiftNum= " + shiftNum+";");
			
			while(rs.next()){			
				items.add(String.valueOf(rs.getString("transactionReturn")));
				items.add(String.valueOf(rs.getString("shortDes")));    
				items.add(String.valueOf(rs.getString("qty")));
				items.add(String.valueOf(rs.getString("price")));      
			}
			
			rs.close();
			stmt.close();
			conn.close();
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
	
	public void getView() {     
		receiptView.add("       C STOP CONVENIENT STORE       ");
	    receiptView.add("           2761 US RT 20 E           ");
	    receiptView.add("          COLLINS OH 44826           ");
	    receiptView.add("            419-660-1088             ");
	    receiptView.add("-------------------------------------");
	    
	   
	    String line1 = transactionDate + "  ";
	    line1 += String.format("%-13s", trasactionId);
	    line1 += String.format(shiftNum, "%3s" );

	    receiptView.add(line1);
	 
	    for(int s=0; s<items.size()/4; s++){
	    	String itemLines = String.format("%-18s", items.get((s*4)+1));
	    	String temp = "x" + items.get((s*4)+2);
	    	itemLines += String.format("%6s", temp);
	    	String temp2 =  df2.format(Double.parseDouble(items.get((s*4)+3))/100);
	    	itemLines += String.format("%11s", temp2 );
	    	
	    	receiptView.add(itemLines);
	    }
	    receiptView.add("-------------------------------------");
	    String line2 = String.format("%24s", "SUBTOTAL");
	    line2 += String.format("%11s", df2.format(Double.parseDouble(preTax)/100));
	    receiptView.add(line2);
	    
	    String line3 = String.format("%24s", "TAX");
	    line3 += String.format("%11s", df2.format(Double.parseDouble(tax)/100));
	    receiptView.add(line3);
	    
	    String line4 = String.format("%24s", "TOTAL");
	    line4 += String.format("%11s", df2.format(Double.parseDouble(total)/100));
	    receiptView.add(line4);
	    
	    if (Integer.parseInt(cash)>0) {
	    	String line5 = String.format("%24s", "CASH");
		    line5 += String.format("%11s", df2.format(Double.parseDouble(cash)/100));
		    receiptView.add(line5);
	    }
	    
	    if (Integer.parseInt(credit)>0) {
	    	String line6 = String.format("%24s", "CREDIT");
		    line6 += String.format("%11s", df2.format(Double.parseDouble(credit)/100));
		    receiptView.add(line6);
	    }
	    
	    if (Integer.parseInt(check)>0) {
	    	String line7 = String.format("%24s", "CHECK");
		    line7 += String.format("%11s", df2.format(Double.parseDouble(check)/100));
		    receiptView.add(line7);
	    }
	    
	    String line8 = String.format("%24s", "CHANGE");
	    line8 += String.format("%11s", df2.format(Double.parseDouble(change)/100));
	    receiptView.add(line8);
	    receiptView.add("*************************************");
	    receiptView.add("     THANK YOU FOR YOUR BUSINESS     ");
	}
}
