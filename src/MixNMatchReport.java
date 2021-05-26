import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MixNMatchReport {
	ArrayList<String> mixNMatchReport= new ArrayList();
	DecimalFormat df2 = new DecimalFormat("#0.00");
	public MixNMatchReport(int x){
		getReport(x);
	}
	
	public void getReport(int shift) {
		Statement stmt = null;
		ResultSet rs = null;
		String sqlMixNMatch = "SELECT d.discountId as id, u.`name` as `name`, sum(d.discountGiven) as amount, sum(d.discountQty) as qty FROM cstop.transactiondata d Inner join cstop.discount u on d.discountId = u.id where discountGiven >0 group by discountId;";
		if (shift != 0) {
			sqlMixNMatch = "SELECT d.discountId as id, u.`name` as `name`, sum(d.discountGiven) as amount, sum(d.discountQty) as qty FROM cstop.transactiondata d Inner join cstop.discount u on d.discountId = u.id where discountGiven >0 and shiftNum= "+shift + " group by discountId;";
		}
		
		try 
		{
			SqlConn sqlInfo = new SqlConn();		  	  
			Connection conn = DriverManager.getConnection(sqlInfo.url, sqlInfo.username, sqlInfo.password);
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sqlMixNMatch);
			while(rs.next()){
				String num =  String.format("%-4s",rs.getString("id"));
				String name= String.format("%-16s",(rs.getString("name")));
				String qty = String.format("%-5s",rs.getString("qty"));
				String amount = String.format("%9s",df2.format(Double.parseDouble(rs.getString("amount"))/100));
				mixNMatchReport.add(num + name + qty + amount);
			}			
			rs.close();
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
