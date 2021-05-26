import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class ButtonsList {

	ArrayList<Buttons> list= new ArrayList();
	public ButtonsList(){
		initButtons();
	}
	
	public void initButtons() {
			
			Statement stmt = null;
			ResultSet rs = null;
			ResultSet taxRate = null;
			try 
			{
				SqlConn sqlInfo = new SqlConn();		  	  
				Connection conn = DriverManager.getConnection(sqlInfo.url, sqlInfo.username, sqlInfo.password);
				  
				stmt = conn.createStatement();
			 
				rs = stmt.executeQuery("SELECT * FROM cstop.quickbuttons order by id;");
				
				while(rs.next()){
				//Retrieve by column name
					Buttons temp = new Buttons();
					temp.barcode = new BigInteger (rs.getString("barcode"));
					temp.name = rs.getString("name");
					temp.id = rs.getInt("id");
					temp.x = rs.getInt("x");
					temp.y = rs.getInt("y");
					temp.width = rs.getInt("width");
					temp.height = rs.getInt("height");
					temp.btnColor = rs.getString("btnColor");
					temp.btnType = rs.getInt("btnType");
					temp.r = rs.getInt("r");
					temp.g = rs.getInt("g");
					temp.b = rs.getInt("b");
					list.add(temp);
				    
				    /////////////////////////////////////DISPLAY TEST///////////////////////////////////////////////////
				    /*
				    System.out.println(info[0]);
				    System.out.println(info[2]);
				    System.out.println(info[4]);
				    System.out.println(info[1]);
				    System.out.println(info[5]);
				    System.out.println(info[3]);     */
				}
				
				
				
			    stmt.close();
			    conn.close();
			  } catch(Exception e) {
				  System.out.print(e +"asdfa");
			  }
			  finally {
				    if (rs != null) {
				        try {
				            rs.close();
				        } catch (SQLException sqlEx) { } // ignore
	
				        rs = null;
				    }
				    
				    if (taxRate != null) {
				        try {
				        	taxRate.close();
				        } catch (SQLException sqlEx) { } // ignore
	
				        taxRate = null;
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
