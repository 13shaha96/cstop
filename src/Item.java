

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class Item{
	public BigInteger barcode;
	public String shortDescription;
	public String department;
	public int tax;
	public int price;
	public int priceEach;
	public String fullDescription;
	public int qty;
	public int discountId=0;
	public int discountGiven;
	public int discountQty;
	public boolean exists = false;
	
	public void updateQty(int x) {
		this.qty = x;
	}
	

	public void updatePrice(int x) {
		this.price = x;
	}
	
	public void updatePriceEach(int x) {
		this.priceEach = x;
	}

	public void updateDiscountGiven(int x, int y) {
		this.discountGiven = x;
		this.discountQty =y;
	}
	
	public Item(BigInteger x) {
		readSql(x);
		qty++;
		discountGiven = 0;
		discountQty =0;
	}
	
	
	// Reads Sql
	public void readSql(BigInteger x) {
		
		Statement stmt = null;
		ResultSet rs = null;
		ResultSet taxRate = null;
		try 
		{
			SqlConn sqlInfo = new SqlConn();		  	  
			Connection conn = DriverManager.getConnection(sqlInfo.url, sqlInfo.username, sqlInfo.password);
			  
			stmt = conn.createStatement();
			//System.out.println(x);
			rs = stmt.executeQuery("SELECT * FROM cstop.itemdata where barcode = "+x+" ;");
			
			while(rs.next()){
			//Retrieve by column name
				exists = true;
				this.barcode = new BigInteger (rs.getString("barcode"));
				this.department = rs.getString("department");
			    this.priceEach = rs.getInt("price");
			    
			    this.shortDescription = rs.getString("shortDes");
			    this.fullDescription = rs.getString("fullDes");
			    this.tax = rs.getInt("tax");
			    this.discountId = rs.getInt("discountId");
			    
			    
			    /////////////////////////////////////DISPLAY TEST///////////////////////////////////////////////////
			    /*
			    System.out.println(info[0]);
			    System.out.println(info[2]);
			    System.out.println(info[4]);
			    System.out.println(info[1]);
			    System.out.println(info[5]);
			    System.out.println(info[3]);     */
			}
			
			taxRate = stmt.executeQuery("SELECT * FROM cstop.taxrate where taxId = "+tax+" ;");
		    
			while(taxRate.next()){
				tax = taxRate.getInt("taxRate");
			    //System.out.println(info[3]);
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
