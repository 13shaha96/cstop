import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.Date;

public class Reports {
	
	ArrayList<String> reportView = new ArrayList();
	//strReport for the department totals
	HashMap<Integer, String> strReport = new HashMap<Integer, String>();
		
	//strTotal for the total at the top of the report
	HashMap<String, String> strTotal = new HashMap<>();
	
	ArrayList<String> departmentReport = new ArrayList();
	ArrayList<String> itemReport = new ArrayList();
	ArrayList<String> timeReport = new ArrayList();
	ArrayList<String> mixNmatchRpt = new ArrayList();
	
	DecimalFormat df2 = new DecimalFormat("#0.00");

	


	
	Date start= null;
	Date end = null;
	int grossSales = 0;
	int returns = 0;
	int taxExempt =0;
	int numReturns =0;
	int nonTaxSales= 0;
	int taxSales= 0;
	int shift =0;
	 
	public Reports(String x) {
		
		initReport(x);
		getView();
	}
	

	public void initReport(String x) {
		
		Statement stmt = null;
		ResultSet rs = null;
		

		try 
		{
			SqlConn sqlInfo = new SqlConn();		  	  
			Connection conn = DriverManager.getConnection(sqlInfo.url, sqlInfo.username, sqlInfo.password);
			stmt = conn.createStatement();
			
			String sqlDepartment = "select d.department, sum(d.qty) as qty, sum(d.price) as price, n.`name` as name, r.`name` as groupname from cstop.transactiondata d Inner join cstop.departments n on d.department = n.departmentId inner join cstop.reportgroupname r  on n.reportGroup = r.id group by d.department order by n.reportGroup, n.departmentId;";
			String title =" ";
			String sqlGSales = "select sum(price) as total from cstop.transactiondata where department != 15 and department != 14 and department != 99 and department != 1000 and department != 1002;";
			String sqlSales = "select sum(price) as total from cstop.transactiondata where department != 15 and department != 14 and department != 99 and taxRate = 0 and department != 1000 and department != 1002;";
			String sqlSalesTotal = "select sum(price) as total from cstop.transactiondata where department != 15 and department != 14 and department != 99 and taxRate > 0 and department != 100 and department != 1002;";
			String sqlInstSales = "select sum(qty) as qty, sum(price) as total from cstop.transactiondata where department >16 and department < 24;";
			String sqlReturns = "select sum(price) as `returns`, sum(transactionreturn) as qty from cstop.transactiondata where transactionreturn = 1;";
			String sqlReturnsTotal = "select sum(tax) as `returns`, sum(transactionreturn) as qty from cstop.transactiontotal where transactionreturn = 1;";
			String sqlTotals = "select sum(tax) as tax, sum(cash-`change`) as cash, sum(checks) as checks, sum(credit) as credit, min(transactionDate) as `start`, max(transactionDate) as `end` from cstop.transactiontotal;";
			String sqlItemReport = "select sum(y.qty) as qty, sum(y.price) as price, y.shortDes as des, x.`name` from cstop.reportitem x Inner join cstop.transactiondata y on x.barcode = y.barcode group by x.barcode order by x.`order`;";
			String sqlTaxExempt = "Select sum(taxExempt) as qty, sum(taxExemptedTax) as tax from cstop.transactiontotal where taxExempt=1;";
			if (x.equals("shift")) {
				rs = stmt.executeQuery("select * from cstop.counters where main=1;");
				while(rs.next()){
					shift = rs.getInt("shiftNum");
				}
				rs.close();
				sqlDepartment= "select d.department, sum(d.qty) as qty, sum(d.price) as price, n.`name` as name, r.`name` as groupname from cstop.transactiondata d Inner join cstop.departments n on d.department = n.departmentId inner join cstop.reportgroupname r  on n.reportGroup = r.id where d.shiftNum=" + shift + " group by d.department order by n.reportGroup, n.departmentId;";
				sqlGSales = "select sum(price) as total from cstop.transactiondata where shiftNum= " + shift + " and department != 15 and department != 14 and department != 99 and department != 1000 and department != 1002;";
				sqlSales = "select sum(price) as total from cstop.transactiondata where shiftNum= " + shift + " and department != 15 and department != 14 and department != 99 and taxRate = 0 and department != 1000 and department != 1002;";
				sqlSalesTotal = "select sum(price) as total from cstop.transactiondata where shiftNum= " + shift + " and department != 15 and department != 14 and department != 99 and taxRate > 0 and department != 1000 and department != 1002;";
				sqlInstSales = "select sum(qty) as qty, sum(price) as total from cstop.transactiondata where shiftNum= " + shift + " and department >16 and department < 24;";
				sqlReturns = "select sum(price) as `returns`, sum(transactionreturn) as qty from cstop.transactiondata where shiftNum= " + shift + " and transactionreturn = 1;";
				sqlReturnsTotal = "select sum(tax) as `returns`, sum(transactionreturn) as qty from cstop.transactiontotal where shiftNum= " + shift + " and  transactionreturn = 1;";
				sqlTotals = "select sum(tax) as tax, sum(cash-`change`) as cash, sum(checks) as checks, sum(credit) as credit, min(transactionDate) as `start`, max(transactionDate) as `end` from cstop.transactiontotal where shiftNum= " + shift + ";";
				sqlTaxExempt = "Select sum(taxExempt) as qty, sum(taxExemptedTax) as tax from cstop.transactiontotal where shiftNum= " + shift + " and taxExempt=1;";
				
				title = "            SHIFT REPORT             ";
			}else {
				title =  "        END OF THE DAY REPORT        ";
			}
			
			MixNMatchReport mixNmatchReport = new MixNMatchReport(shift);
			mixNmatchRpt = mixNmatchReport.mixNMatchReport;
			timeReport = new TimeReport(shift).timeReport;
			
			/*
			rs = stmt.executeQuery("SELECT reportGroup FROM cstop.departments group by reportGroup order by reportGroup;");
			while(rs.next()){
				if(rs.getInt("reportGroup")!=7)
					printOrder.add(rs.getInt("reportGroup"));
			}
			rs.close();*/
			
	
		 
			String departmentName = " ";
			rs = stmt.executeQuery(sqlDepartment);
			while(rs.next()){
				if (!departmentName.equals(rs.getString("groupname")) && !"COUPONS".equals(rs.getString("groupname")) && !"PAID OUT".equals(rs.getString("groupname"))) {
					departmentName = rs.getString("groupname");
					int strSize= (37-departmentName.length())/2;
					String tempStr = "";
					for (int i=0; i<strSize; i++) {
						tempStr += "-";
					}
					
					tempStr = tempStr + departmentName + tempStr;
					if (tempStr.length() <37)
						tempStr += "-";
					departmentReport.add("-------------------------------------");
					departmentReport.add(tempStr);
				}
				
				if( (rs.getInt("department")) == 99) {
					String num =  String.format("%-4s",rs.getString("department"));
					String name= String.format("%-16s",rs.getString("name"));
					String qty = String.format("%-5s",rs.getString("qty"));
					String amount = String.format("%9s",df2.format(Double.parseDouble(rs.getString("price"))/100));
					strReport.put(99, num + name + qty + amount);
				}else if( (rs.getInt("department")) == 1002) {
					String num =  String.format("%-4s",rs.getString("department"));
					String name= String.format("%-16s",rs.getString("name"));
					String qty = String.format("%-5s",rs.getString("qty"));
					String amount = String.format("%9s",df2.format(Double.parseDouble(rs.getString("price"))/100));
					strReport.put(1002, num + name + qty + amount);	
				}else{
					String num =  String.format("%-4s",rs.getString("department"));
					String name= String.format("%-16s",rs.getString("name"));
					String qty = String.format("%-5s",rs.getString("qty"));
					String amount = String.format("%9s",df2.format(Double.parseDouble(rs.getString("price"))/100));
					departmentReport.add(num + name+ qty+ amount);
				}
			}
			rs.close();
			
			
			
			rs = stmt.executeQuery(sqlGSales);
			while(rs.next()){
				if (rs.getString("total")!=null)
					grossSales = rs.getInt("total");
			}
			rs.close();
			
			rs = stmt.executeQuery(sqlSales);
			while(rs.next()){
				String nonTax = "0";
				if (rs.getString("total")!=null) {
					nonTaxSales = rs.getInt("total");
					nonTax = rs.getString("total");
				}
				nonTax = String.format("%22s",df2.format(Double.parseDouble(nonTax)/100));
				String nameNonTax= String.format("%-12s","NONTAX SALES");
				strTotal.put("nonTax", nameNonTax + nonTax);
			}
			rs.close();
			
			
			
			rs = stmt.executeQuery(sqlSalesTotal);
			while(rs.next()){
				String taxSalesStr = "0";
				if (rs.getString("total")!=null) {
					taxSales = rs.getInt("total");
					taxSalesStr = rs.getString("total");
				}
				taxSalesStr = String.format("%22s",df2.format(Double.parseDouble(taxSalesStr)/100));
				String nameTaxSales= String.format("%-12s","TAX SALES");
				strTotal.put("taxSales", nameTaxSales + taxSalesStr);
			}
			rs.close();
			//////////////////////////////////////////////////////////////
			rs = stmt.executeQuery(sqlInstSales);
			while(rs.next()){
				String num =  String.format("%-4s","11");
				String name= String.format("%-16s","INSTANT SALES");
				String qty = String.format("%-5s","1");
				String nullCheck = "0";
				if (rs.getString("total")!=null) {
					nullCheck = rs.getString("total");
				}
				String amount = String.format("%9s",df2.format(Double.parseDouble(nullCheck)/100));
				strReport.put(11, num + name + qty + amount);
			}
			rs.close();
			/////////////////////////////////////////////////////////////
			String netSales = String.format("%22s",df2.format(Double.parseDouble(String.valueOf(nonTaxSales+taxSales))/100));
			String nameNetSales= String.format("%-12s","NET SALES");
			strTotal.put("netSales", nameNetSales + netSales);
				
				
			rs = stmt.executeQuery(sqlReturns);
			while(rs.next()){
				returns = rs.getInt("returns");
				numReturns = rs.getInt("qty");
			}
			/////////////////////////////////////////////////////
			rs.close();
			if (numReturns >0) {
				rs = stmt.executeQuery(sqlReturnsTotal);
				while(rs.next()){
										
					String num =  String.format("%-4s","999");
					String name= String.format("%-16s","Returns"); 
					String qty = String.format("%-5s",rs.getString("qty"));
					String amount = String.format("%9s",df2.format(Double.parseDouble((String.valueOf(rs.getInt("returns")+returns)))/100));
					strReport.put(999, num + name + qty + amount);

				}
				rs.close();
			}
			
			//////////////////////////////////////////////////////
			
			
			rs = stmt.executeQuery(sqlTotals);
			while(rs.next()){
				String tax = String.format("%22s",df2.format(Double.parseDouble(rs.getString("tax"))/100));
				String nameTax= String.format("%-12s","TAX");
				strTotal.put("tax", nameTax + tax);
				
				String cash = String.format("%22s",df2.format(Double.parseDouble(rs.getString("cash"))/100));
				String nameCash= String.format("%-12s","CASH");
				strTotal.put("cash", nameCash + cash);
				
				String checks = String.format("%22s",df2.format(Double.parseDouble(rs.getString("checks"))/100));
				String nameChecks= String.format("%-12s","CHECK");
				strTotal.put("check", nameChecks + checks);
				
				String credit = String.format("%22s",df2.format(Double.parseDouble(rs.getString("credit"))/100));
				String nameCredit= String.format("%-12s","CREDIT");
				strTotal.put("credit", nameCredit + credit);
				
				strTotal.put("title", title);
				start = rs.getDate("start");
				end = rs.getDate("end");
				String start1 = String.format("%29s",rs.getString("start"));
				String end1 = String.format("%29s",rs.getString("end"));
				strTotal.put("start", "START" + start1);
				strTotal.put("end", "END  " + end1);
				
				String total = String.format("%22s",df2.format((Double.parseDouble(rs.getString("tax"))+grossSales)/100));
				String name= String.format("%-12s","GROSS SALES");
				strTotal.put("GrossSales", name + total);
				
			}			
			rs.close();
			
			if (x.equals("shift")) {
	 
				rs = stmt.executeQuery("select * from cstop.counters where main=1;");
				
				while(rs.next()){
					String qty = String.format("%22s",(rs.getString("noSale")));
					String name= String.format("%-12s","NO SALES");
					strTotal.put("noSale", name + qty);
				}
				int result = stmt.executeUpdate("update cstop.counters set transactionID = 1, shiftNum= shiftNum+1, noSaleDay= noSaleDay + noSale, noSale=0 where main = 1;");
				rs.close();
			}
			
			
			if (x.equals("endOfDay")) {
				rs= stmt.executeQuery(sqlItemReport);
				String iName = " ";
				String iQty = " ";
				String iDes = " ";
				String amount = " ";
				String tempStr = "";
				while(rs.next()) {
					if (!iName.equals(rs.getString("name"))) {
						tempStr = "";
						iName = rs.getString("name");
						int strSize= (37-iName.length())/2;
						
						for (int i=0; i<strSize; i++) {
							tempStr += "-";
						}
						tempStr = tempStr + iName + tempStr;
						if (tempStr.length() <37)
							tempStr += "-";
						itemReport.add("-------------------------------------");
						itemReport.add(tempStr);
					}
					tempStr= String.format("%-20s",rs.getString("des"));
					iQty = String.format("%-5s",rs.getString("qty"));
					amount = String.format("%9s",df2.format(Double.parseDouble(rs.getString("price"))/100));
					itemReport.add(tempStr + iQty + amount);
				}
				rs.close();
				
				rs = stmt.executeQuery("select * from cstop.counters where main=1;");
				
				while(rs.next()){
					String qty = String.format("%22s",(rs.getString("noSaleDay")));
					String name= String.format("%-12s","NO SALES");
					strTotal.put("noSale", name + qty);
				}
				int result = stmt.executeUpdate("update cstop.counters set transactionID = 1, shiftNum= 1, noSaleDay= 0, noSale=0 where main = 1;");
				
				int moveData = stmt.executeUpdate("insert into cstop.historytransactiondata select * from cstop.transactiondata;");
				
				int safeMode = stmt.executeUpdate("SET SQL_SAFE_UPDATES = 0;");
				int deleteData = stmt.executeUpdate("delete from cstop.transactiondata where transactionId >=0;");
				
				int moveDataTotal = stmt.executeUpdate("insert into cstop.historytransactiontotal select * from cstop.transactiontotal;");
				
				 
				int deleteDataTotal = stmt.executeUpdate("delete from cstop.transactiontotal where transactionId >=0;");
				rs.close();
			}
			
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
	
	public void getView(){               
	    reportView.add("      C STOP CONVENIENCE STORE       ");
	    reportView.add("          2761 US RT 20 E            ");
	    reportView.add("          COLLINS OH 44826           ");
	    reportView.add("            419-660-1088             ");
	    reportView.add(strTotal.get("title"));
	    reportView.add("-------------------------------------");
	    reportView.add(strTotal.get("start"));
	    reportView.add(strTotal.get("end"));
	    reportView.add("-------------------------------------");
	    reportView.add(strTotal.get("GrossSales"));
	    
	    
	    reportView.add(strTotal.get("nonTax"));
	    reportView.add(strTotal.get("taxSales"));
	    reportView.add(strTotal.get("netSales"));
	    
	    reportView.add(strTotal.get("tax"));
	    reportView.add(strTotal.get("cash"));
	    reportView.add(strTotal.get("check"));
	    reportView.add(strTotal.get("credit"));
	    
	    if(strReport.containsKey(99)) {
	    	reportView.add(strReport.get(99));
	    }
	    if(strReport.containsKey(1002)) {
	    	reportView.add(strReport.get(1002));
	    }
	    
	    if(strReport.containsKey(999)) {
	    	reportView.add(strReport.get(999));
	    }
	    reportView.add(strTotal.get("noSale"));
	    reportView.add("-------------------------------------");
	    reportView.add("-------------TIME REPORT-------------");    
	    
	    for(int i=0; i<timeReport.size(); i++) {
	    	//data by time
	    	reportView.add(timeReport.get(i));
	    	//g2d.drawString(timeReport.get(i),12,y);y+=yShift;
	    }
	    reportView.add("-------------------------------------");
	    reportView.add("-------------MIX N MATCH-------------");
	    
	    for(int i=0; i<mixNmatchRpt.size(); i++) {
	    	//Mix and match report
	    	reportView.add(mixNmatchRpt.get(i));
	    }
	    
	    for(int i =0; i<departmentReport.size(); i++) {
	    	reportView.add(departmentReport.get(i));
		}

	    for(int i =0; i<itemReport.size(); i++) {
	    	reportView.add(itemReport.get(i));
		}
	    
	    reportView.add("*************************************");
	    reportView.add("            END OF REPORT            ");
	}
}
