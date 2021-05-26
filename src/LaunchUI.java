
import java.awt.Container;
import java.awt.EventQueue;

import java.util.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.print.PrintService;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Font;

public class LaunchUI {
	
	private JFrame frame;
	private JTextField barcode;
 
	ButtonsList buttons = new ButtonsList();
	ArrayList<Buttons> btnList = buttons.list; 
	
	
	ArrayList<Item> checkOutList = new ArrayList<Item>();
	int[] subTotal = {0,0,0};
	int forButton =1;

	//cash, credit, check, change
	int[] paymentType = {0,0,0,0};
	boolean newTrans = true;
	int returns = 0;
	int shiftNum = 0;
	int taxExempt = 0;
	int taxExemptedTax = 0;
	DecimalFormat df2 = new DecimalFormat("#0.00");
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LaunchUI window = new LaunchUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void openDrawer() {
		try {
            // Just run the cashdrawer.bat
            Process process = Runtime.getRuntime().exec("cashdrawer.bat");
        } catch (IOException ex) {
            System.out.print(ex);
        }
	}
	
	public String[] getPermission(String btn){
		String[] info = {"-1",btn,"btn not configured"};
		
		if(btn.equals("btnNoSale")) {
			if (checkOutList.size()==0) {
				info[0] = "1"; info[1] = btn; info[2] = "No Errors";
				return info;
			}else
			{
				info[0] = "-1"; info[1] = btn; info[2] = "WRONG SEQUENCE";
				return info;
			}
		}
		
		return info;
	}
	
	public String[] returnView() {
		String[] info = {"","","","",""};
		subTotal = returnPrice();
		
		
		for (int i=0; i<checkOutList.size(); i++)
		{
			String temp = String.format("%4s", i+1+". ");
			temp+= String.format("%-25s", checkOutList.get(i).shortDescription);
			temp+= "x";
			temp+= String.format("%-10s", checkOutList.get(i).qty);
			
			temp+=  df2.format(Double.parseDouble(String.valueOf(checkOutList.get(i).price))/100) + "\n";
			
			info[0] += temp;
			
			//info[0] += i+1 + ". " + checkOutList.get(i).shortDescription + "  x" + checkOutList.get(i).qty + "\n";
			//info[1] += df2.format(Double.parseDouble(String.valueOf(checkOutList.get(i).price))/100) + "\n";
		}
		
		info[2] = " SUBTOTAL: " + df2.format(Double.parseDouble(String.valueOf(subTotal[0]))/100);
		info[3] = "      TAX: " + df2.format(Double.parseDouble(String.valueOf(subTotal[1]))/100);
		info[4] = "    TOTAL: " +df2.format(Double.parseDouble(String.valueOf(subTotal[2]))/100);
		
		String totalPrice = " SUBTOTAL: " + df2.format(Double.parseDouble(String.valueOf(subTotal[0]))/100);
		totalPrice += "\n" + "      TAX: " + df2.format(Double.parseDouble(String.valueOf(subTotal[1]))/100);
		totalPrice += "\n" + "    TOTAL: " +df2.format(Double.parseDouble(String.valueOf(subTotal[2]))/100);
		
		info[1] = totalPrice;
		//System.out.println(subTotal[0] + "asdfasdfas" + info[2]);
		return info;
	}
	
	public int[] returnPrice(){
		int[] info = {0,0,0};
		int price = 0;
		int discountItemsCtr =0;
		BigDecimal tax = new BigDecimal("0");

		BigDecimal totalTax =  new BigDecimal("0");
		for(int i=0; i< checkOutList.size(); i++ ) {
			int currPrice =0;
			if(checkOutList.get(i).discountId >0) {
				discountItemsCtr++;
			}
			currPrice = (checkOutList.get(i).priceEach) * (checkOutList.get(i).qty);
			checkOutList.get(i).updatePrice(currPrice);	
			//System.out.println(checkOutList.get(i).price);
		}
		
		
		
		if (discountItemsCtr>0) {
			ArrayList<Integer> discountIdList = new ArrayList();
			
			for(int i=0; i< checkOutList.size(); i++ ) {
				ArrayList<Item> tempCheckOutList = new ArrayList<Item>();
				int currDiscountId = checkOutList.get(i).discountId;
				if( currDiscountId >0 && !discountIdList.contains(currDiscountId)) {
					discountIdList.add(currDiscountId);
					int totalQty=0;
					for(int x=i; x<checkOutList.size(); x++ ) {
						if(currDiscountId == checkOutList.get(x).discountId) {
							totalQty += checkOutList.get(x).qty; 
							tempCheckOutList.add(checkOutList.get(x));
						}
					}
					Comparator<Item> priceSorter = (Item o1, Item o2) ->  String.valueOf(o1.price).compareTo(String.valueOf(o2.price));
					Collections.sort(tempCheckOutList, priceSorter);
					
					int discountQty=0;
					int discountPrice = 0;
					Statement stmt = null;
					ResultSet rs = null;
					try 
					{
						SqlConn sqlInfo = new SqlConn();		  	  
						Connection conn = DriverManager.getConnection(sqlInfo.url, sqlInfo.username, sqlInfo.password);
						stmt = conn.createStatement();
						rs = stmt.executeQuery("SELECT * FROM cstop.discount where id =" +currDiscountId+" ;");
						while(rs.next()){
							discountQty = rs.getInt("qty");
						    discountPrice = rs.getInt("price");    
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
					
					int priceChangeReq = (totalQty / discountQty) * discountQty;
					
					for(int x=0; x<tempCheckOutList.size(); x++) {
						int tempQtyCtr = tempCheckOutList.get(x).qty;
						if(priceChangeReq>=tempQtyCtr) {
							int priceBeforeDiscount = tempCheckOutList.get(x).price;
							tempCheckOutList.get(x).updatePrice(discountPrice*tempQtyCtr);
							tempCheckOutList.get(x).updateDiscountGiven(priceBeforeDiscount - tempCheckOutList.get(x).price, tempQtyCtr);
						}else if (priceChangeReq>0)
						{
							int priceBeforeDiscount = tempCheckOutList.get(x).price;
							tempCheckOutList.get(x).updatePrice((discountPrice*priceChangeReq) +(tempCheckOutList.get(x).priceEach * (tempQtyCtr-priceChangeReq)));
							tempCheckOutList.get(x).updateDiscountGiven(priceBeforeDiscount - tempCheckOutList.get(x).price, priceChangeReq);
						}
						priceChangeReq -= tempQtyCtr;
					}				
				}
			}
		}
		
		
		
		for(int i=0; i< checkOutList.size(); i++ ) {
			int currPrice =0;
			currPrice = checkOutList.get(i).price;
			price += currPrice;
			
			int x = currPrice*checkOutList.get(i).tax;
			tax = new BigDecimal(Integer.toString(x)).divide(new BigDecimal ("1000000"));
			tax = (tax.setScale(2, RoundingMode.HALF_UP)).multiply(new BigDecimal("100"));
			totalTax = totalTax.add(tax);
			 
		}
		
		
		if(returns==1) {
			info[0] = price*-1;
			info[1] = totalTax.intValue()*-1;
			info[2] = (totalTax.intValue() + price)*-1;
		}else {
			info[0] = price;
			info[1] = totalTax.intValue();
			info[2] = totalTax.intValue() + price;
		}
		
		if (taxExempt ==1) {
			taxExemptedTax = info[1];
			info[0] = price;
			info[1] = 0;
			info[2] = price;
		}
		
		////////////////DISPLAY TEST///////////////////////////////////////////////////
		//System.out.println(info[0]);
		//System.out.println(info[1]);
		//System.out.println(info[2]);
		
		return info;
	}
	
	public String dateTime () {
		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf = 
		     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return sdf.format(dt);
	}
	
	
	
	//write to sql
	public void checkOutProcess() {
		Statement stmt = null;
		String[] ctrs = updateCounters();

		
		try 
		{
			SqlConn sqlInfo = new SqlConn();		  	  
			Connection conn = DriverManager.getConnection(sqlInfo.url, sqlInfo.username, sqlInfo.password);
			stmt = conn.createStatement();
			
			for(int i=0; i<checkOutList.size(); i++) {
				PreparedStatement ps = conn.prepareStatement("insert into transactiondata(transactionId, barcode, department, price, taxRate, shortDes, fullDes, transactionReturn, shiftNum, qty, transactionDate, discountId, discountGiven, discountQty, taxExempt) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				ps.setString(1, ctrs[0].toString());
				ps.setString(2,checkOutList.get(i).barcode.toString());
				ps.setString(3,checkOutList.get(i).department);
				if (returns ==1) {
					ps.setInt(4,(checkOutList.get(i).price)*-1);	
				}else
					ps.setInt(4,checkOutList.get(i).price);
				ps.setInt(5,checkOutList.get(i).tax);
				ps.setString(6,checkOutList.get(i).shortDescription);
				ps.setString(7,checkOutList.get(i).fullDescription);			
				ps.setInt(8, returns);
				ps.setString(9, ctrs[1]);
				ps.setInt(10, checkOutList.get(i).qty);
				ps.setString(11, dateTime());
				ps.setInt(12, checkOutList.get(i).discountId);
				ps.setInt(13, checkOutList.get(i).discountGiven);
				ps.setInt(14, checkOutList.get(i).discountQty);
				ps.setInt(15, taxExempt);
				ps.executeUpdate();
			    
			}
			
			PreparedStatement ps = conn.prepareStatement("insert into transactiontotal(transactionId, preTax, tax, total, cash, credit, checks, `change`, shiftNum, transactionDate, transactionReturn, taxExempt, taxExemptedTax) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
			ps.setString(1, ctrs[0]);
			ps.setInt(2,subTotal[0]);
			ps.setInt(3,subTotal[1]);
			ps.setInt(4,subTotal[2]);
			ps.setInt(5,paymentType[0]);
			ps.setInt(6,paymentType[1]);			
			ps.setInt(7,paymentType[2]);
			ps.setInt(8,paymentType[3]);
			ps.setString(9, ctrs[1]);
			ps.setString(10, dateTime());
			ps.setInt(11, returns);
			ps.setInt(12, taxExempt);
			ps.setInt(13, taxExemptedTax);
			ps.executeUpdate();
			
			stmt.close();
			conn.close();
			
			    
		} 
		catch(Exception e) {
			System.out.print(e);
		}
		finally 
		{
			if (stmt != null) {
				try 
				{
					stmt.close();
				} 
				catch (SQLException sqlEx) {
					
				} // ignore

				stmt = null;
			}
		}
		returns =0;
		taxExempt =0;
	    checkOutList.clear();
		newTrans = true;		
		
			
	}	

	
	
	public String[] updateCounters() {
		String[] info = {"0","0"};
		
		Statement stmt = null;
		ResultSet rs = null;
		try 
		{
			SqlConn sqlInfo = new SqlConn();		  	  
			Connection conn = DriverManager.getConnection(sqlInfo.url, sqlInfo.username, sqlInfo.password);
			  
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM cstop.counters;");
			  
			while(rs.next()){
			//Retrieve by column name
				info[0] = String.valueOf(rs.getString("transactionID"));
			    info[1] = String.valueOf(rs.getString("shiftNum"));    
			}
			
			int result = stmt.executeUpdate("update cstop.counters set transactionID = transactionID + 1 where main = 1;");

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
		return info;	
	}	
	
	public int addToCheckOutList(BigInteger barcode, int qty, int type) {
		if(type>0) {
			Item temp = new Item(new BigInteger (String.valueOf(type)));
			temp.updatePriceEach(Integer.parseInt(String.valueOf(barcode)));
			checkOutList.add(temp);
		}else {
			Item temp = new Item(barcode);
			if(temp.exists) {
				for (int i=0; i<checkOutList.size(); i++) {
					if( checkOutList.get(i).barcode.equals(barcode)) {
						checkOutList.get(i).updateQty(checkOutList.get(i).qty + qty);
						return 0;
					}
				}
				checkOutList.add(temp);
			}else
			{
				MessageBox.infoBox("BARCODE", "ITEM NOT FOUND");
			}
		}
		return 1;
	}
	
	
	/**
	 * Create the application.
	 */
	public LaunchUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		frame = new JFrame();
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		frame.setBounds(100, 100, 1487, 925);
		//frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JTextArea totalPrice = new JTextArea();
		
		totalPrice.setEditable(false);
		totalPrice.setFont(new Font("Monospaced", Font.PLAIN, 13));
		totalPrice.setToolTipText("");
		totalPrice.setBounds(246, 364, 164, 154);
		

		frame.getContentPane().add(totalPrice);
		
		barcode = new JTextField();
		
		frame.getContentPane().add(barcode);
		barcode.setColumns(10);		
		
		KeyPad numPad = new KeyPad(barcode);
		frame.getContentPane().add(numPad);
		numPad.setBounds(500, 600, 200, 208);
		barcode.setBounds(10, 21, 400, 28);
		
		JTextArea itemTotalView; 
		itemTotalView = new JTextArea(10,20);
		itemTotalView.setFont(new Font("Monospaced", Font.PLAIN, 13));
		itemTotalView.setEditable(false);
			
		JScrollPane itemTotalScroll = new JScrollPane(itemTotalView);
		itemTotalScroll.setBounds(10, 60, 400, 294);
		frame.getContentPane().add(itemTotalScroll);
		
		totalPrice.setBorder(new LineBorder(Color.BLACK, 2));
		barcode.setBorder(new LineBorder(Color.BLACK, 2));
		itemTotalScroll.setBorder(new LineBorder(Color.BLACK, 2));
 
		JButton btnCash = new JButton("Cash");
		btnCash.setBounds(420, 135, 113, 39);
		frame.getContentPane().add(btnCash);
		
		JButton btnCc = new JButton("CC");
		btnCc.setBounds(420, 184, 113, 42);
		frame.getContentPane().add(btnCc);
		
		JButton btnCheck = new JButton("Check");
		btnCheck.setBounds(420, 240, 113, 42);
		frame.getContentPane().add(btnCheck);
 
		JButton btnVoid = new JButton("VOID");
		btnVoid.setBackground(Color.WHITE);
		btnVoid.setForeground(Color.BLACK);
		btnVoid.setBounds(10, 569, 105, 42);
		frame.getContentPane().add(btnVoid);
		
		JButton btnCoupon = new JButton("Coupon");
		btnCoupon.setBounds(127, 569, 105, 42);
		frame.getContentPane().add(btnCoupon);
 
		JButton btnFor = new JButton("For");
		btnFor.setBounds(127, 642, 115, 42);
		frame.getContentPane().add(btnFor);
		
		JButton btnNoSale = new JButton("No Sale");
		btnNoSale.setBounds(127, 694, 115, 42);
		frame.getContentPane().add(btnNoSale);
		
		JButton btnReceipt = new JButton("Receipt");
		btnReceipt.setBounds(10, 803, 115, 42);
		frame.getContentPane().add(btnReceipt);
 
		JButton btnClear = new JButton("Clear");
		btnClear.setBounds(121, 452, 115, 42);
		frame.getContentPane().add(btnClear);
 
		JButton btnEndOfDay = new JButton("END OF DAY");
		btnEndOfDay.setBounds(135, 364, 103, 42);
		frame.getContentPane().add(btnEndOfDay);
		
		JButton btnShift = new JButton("SHIFT REPORT");
		btnShift.setBounds(13, 364, 103, 42);
		frame.getContentPane().add(btnShift);
 
		JButton btnPriceInq = new JButton("Price INQ");
		btnPriceInq.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				PriceCheck priceCheckFrame = new PriceCheck();
			}
		});
		btnPriceInq.setBounds(127, 746, 115, 42);
		frame.getContentPane().add(btnPriceInq);
		
		JButton btnReturns = new JButton("RETURNS");
		btnReturns.setBounds(10, 642, 105, 42);
		frame.getContentPane().add(btnReturns);
		/////////////////////////////////////////////////////////
 
		
		ArrayList<ActionListener> actionList = new ArrayList();
		for(int i=0; i<btnList.size(); i++) {
			if(btnList.get(i).btnType==2) {
				ActionListener temp = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JButton o = (JButton)e.getSource();
						String name = o.getName();
				
						
						addToCheckOutList(buttons.list.get(Integer.parseInt(name)-1).barcode,1,0);
						
						String displayString[] = returnView();
						totalPrice.setText("");
	
						itemTotalView.setText("");
						itemTotalView.append(displayString[0]);
				
					    
					    totalPrice.append(displayString[1]+"\n");
					    
				        barcode.setText("");
				        barcode.requestFocus();
					}
				};
				actionList.add(temp);
			}
			
			if(btnList.get(i).btnType==1) {
				ActionListener temp = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JButton o = (JButton)e.getSource();
						String name = o.getName();
						
						
						String selection = barcode.getText();
						
						addToCheckOutList(new BigInteger(selection),1, Integer.parseInt((buttons.list.get(Integer.parseInt(name)-1).barcode).toString()));
						
						
						String displayString[] = returnView();
						
						totalPrice.setText("");
			
						itemTotalView.setText("");
						itemTotalView.append(displayString[0]);
			
					    
					    totalPrice.append(displayString[2]+"\n");
					    totalPrice.append(displayString[3]+"\n\n");
					    totalPrice.append(displayString[4] + "\n");
					    
				        barcode.setText("");
				        barcode.requestFocus();
					}
				};
				actionList.add(temp);
			}
			
			if(btnList.get(i).btnType==88) {
				ActionListener temp = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JButton o = (JButton)e.getSource();
						String name = o.getName();
						
						
						String selection = barcode.getText();
						addToCheckOutList(new BigInteger(selection).multiply(new BigInteger("-1")),1,Integer.parseInt((buttons.list.get(Integer.parseInt(name)-1).barcode).toString()));
						String displayString[] = returnView();
						totalPrice.setText("");
	
						itemTotalView.setText("");
						itemTotalView.append(displayString[0]);
					    totalPrice.append(displayString[1]+"\n");
					    
				        barcode.setText("");
				        barcode.requestFocus();

					}
				};
				actionList.add(temp);
			}
			
			if(btnList.get(i).btnType==3) {
				ActionListener temp = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JButton o = (JButton)e.getSource();
						String name = o.getName();

						String displayString[] = returnView();
						totalPrice.setText(displayString[1]);
						paymentType[0] += Integer.parseInt((buttons.list.get(Integer.parseInt(name)-1)).barcode.toString());
						//String change = String.format("%.2f",(Double.parseDouble(input)-Double.parseDouble(subTotal[2])));
						paymentType[3] =  paymentType[0] + paymentType[1] + paymentType[2] - subTotal[2];
						//totalPrice.setText("");
						barcode.setText("");
						
						totalPrice.append("\n" + "-----------------");
						
						if (paymentType[0] != 0) 
							
							totalPrice.append("\n" + "     CASH: " + df2.format(Double.parseDouble(String.valueOf(paymentType[0]))/100));
						if (paymentType[1] != 0) 
							totalPrice.append("\n" + "       CC: " + df2.format(Double.parseDouble(String.valueOf(paymentType[1]))/100));
						if (paymentType[2] != 0) 
							totalPrice.append("\n" + "    CHECK: " + df2.format(Double.parseDouble(String.valueOf(paymentType[2]))/100));
						
						
							
						
						
					    if (paymentType[3]>=0) {
					    	totalPrice.append("\n" + "   CHANGE: " + df2.format(Double.parseDouble(String.valueOf(paymentType[3]))/100));
					    	checkOutProcess();
					    	paymentType[0] = 0;
					    	paymentType[1] = 0;
					    	paymentType[2] = 0;
					    	paymentType[3] = 0;
					    }else {
					    	totalPrice.append("\n" + "  COLLECT: " + df2.format(Double.parseDouble(String.valueOf(paymentType[3]*-1))/100));
					    }

					}
				};
				actionList.add(temp);
			}
		 
		}
		
		
		int btnCtr = 0;
		while( btnCtr< btnList.size()) {
			
			JButton btnTemp = new JButton(btnList.get(btnCtr).name);
			String trytg = "Black";
			Color btnColor = new Color(btnList.get(btnCtr).r, btnList.get(btnCtr).g, btnList.get(btnCtr).b );
			btnTemp.setBackground(btnColor);
			btnTemp.setName(String.valueOf(btnList.get(btnCtr).id));
			btnTemp.setBounds(buttons.list.get(btnCtr).x, buttons.list.get(btnCtr).y, buttons.list.get(btnCtr).width, buttons.list.get(btnCtr).height);
			frame.getContentPane().add(btnTemp);
			
			 
			btnTemp.addActionListener(actionList.get(btnCtr));
			
			
			btnCtr++;
		}
			
			
		barcode.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER){
					if (newTrans) {
						itemTotalView.setText("");
						totalPrice.setText("");
						
						newTrans = false;
					}
					
					String tempBarcode = barcode.getText();
					
					
					
					for(int i=0; i<forButton; i++)
						addToCheckOutList(new BigInteger(tempBarcode),1, 0);
					forButton=1;
					String displayString[] = returnView();
					
					totalPrice.setText("");
					
					itemTotalView.setText("");
					itemTotalView.append(displayString[0]);
					
				   
				    
				    totalPrice.append(displayString[1]);
				//    totalPrice.append(displayString[3]+"\n\n");
				 //   totalPrice.append(displayString[4] + "\n");
				    
			        barcode.setText("");
		        }
			}
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isDigit(c)) {
					e.consume();
				}
			}
		});
		
		btnReturns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				returns=1;
			}
		});
		btnReceipt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Print test = new Print("receipt");
			}
		});
		
		btnEndOfDay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Print test = new Print("endOfDay");
			}
		});
		
		btnShift.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Print test = new Print("shift");
			}
		});
		
		btnNoSale.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] permission = getPermission("btnNoSale");
				if (permission[0].equals("1")) {
					openDrawer();
					Statement stmt = null;
					try 
					{
						SqlConn sqlInfo = new SqlConn();		  	  
						Connection conn = DriverManager.getConnection(sqlInfo.url, sqlInfo.username, sqlInfo.password);
						stmt = conn.createStatement();
						int result = stmt.executeUpdate("update cstop.counters set noSale= noSale+1;");
						stmt.close();
						conn.close();
					} catch(Exception e1) {
						  System.out.print(e1);
					}
				}else {
					MessageBox.infoBox(permission[1], permission[2]);
				}				
			}
		});
		
		btnFor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 
				String selection = barcode.getText();
				forButton = Integer.parseInt(selection);	
		        barcode.setText("");
			}
		});
		
		btnVoid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selection = barcode.getText();
				int selc = Integer.parseInt(selection);
				
				if(checkOutList.size()>=(selc) && selc>0) {
					checkOutList.remove(selc-1);
				}else {
					System.out.println("Invalid Selection");
				}
				String displayString[] = returnView();
				
				totalPrice.setText("");
				
				itemTotalView.setText("");
				itemTotalView.append(displayString[0]);
			    
			    
			    totalPrice.append(displayString[2]+"\n");
			    totalPrice.append(displayString[3]+"\n\n");
			    totalPrice.append(displayString[4] + "\n");
			    
		        barcode.setText("");
				
			}
		});
 
		
		btnCash.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = barcode.getText();
				String displayString[] = returnView();
				totalPrice.setText(displayString[1]);
				paymentType[0] += Integer.parseInt(input);
				//String change = String.format("%.2f",(Double.parseDouble(input)-Double.parseDouble(subTotal[2])));
				paymentType[3] =  paymentType[0] + paymentType[1] + paymentType[2] - subTotal[2];
				//totalPrice.setText("");
				barcode.setText("");
				
				totalPrice.append("\n" + "-----------------");
				
				if (paymentType[0] != 0) 
					
					totalPrice.append("\n" + "     CASH: " + df2.format(Double.parseDouble(String.valueOf(paymentType[0]))/100));
				if (paymentType[1] != 0) 
					totalPrice.append("\n" + "       CC: " + df2.format(Double.parseDouble(String.valueOf(paymentType[1]))/100));
				if (paymentType[2] != 0) 
					totalPrice.append("\n" + "    CHECK: " + df2.format(Double.parseDouble(String.valueOf(paymentType[2]))/100));
				
				
					
				
				
			    if (paymentType[3]>=0) {
			    	totalPrice.append("\n" + "   CHANGE: " + df2.format(Double.parseDouble(String.valueOf(paymentType[3]))/100));
			    	checkOutProcess();
			    	paymentType[0] = 0;
			    	paymentType[1] = 0;
			    	paymentType[2] = 0;
			    	paymentType[3] = 0;
			    }else {
			    	totalPrice.append("\n" + "  COLLECT: " + df2.format(Double.parseDouble(String.valueOf(paymentType[3]*-1))/100));
			    }
			}
		});
		
		btnCc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String input = barcode.getText();
				String displayString[] = returnView();
				totalPrice.setText(displayString[1]);
				paymentType[1] += Integer.parseInt(input);
				//String change = String.format("%.2f",(Double.parseDouble(input)-Double.parseDouble(subTotal[2])));
				paymentType[3] =  paymentType[0] + paymentType[1] + paymentType[2] - subTotal[2];
				//totalPrice.setText("");
				barcode.setText("");
				
				totalPrice.append("\n" + "-----------------");
				
				if (paymentType[0] != 0) 
					
					totalPrice.append("\n" + "     CASH: " + df2.format(Double.parseDouble(String.valueOf(paymentType[0]))/100));
				if (paymentType[1] != 0) 
					totalPrice.append("\n" + "       CC: " + df2.format(Double.parseDouble(String.valueOf(paymentType[1]))/100));
				if (paymentType[2] != 0) 
					totalPrice.append("\n" + "    CHECK: " + df2.format(Double.parseDouble(String.valueOf(paymentType[2]))/100));
				
				
					
				
				
			    if (paymentType[3]>=0) {
			    	totalPrice.append("\n" + "   CHANGE: " + df2.format(Double.parseDouble(String.valueOf(paymentType[3]))/100));
			    	checkOutProcess();
			    	paymentType[0] = 0;
			    	paymentType[1] = 0;
			    	paymentType[2] = 0;
			    	paymentType[3] = 0;
			    }else {
			    	totalPrice.append("\n" + "  COLLECT: " + df2.format(Double.parseDouble(String.valueOf(paymentType[3]*-1))/100));
			    }
			}
		});
		
		btnCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = barcode.getText();
				String displayString[] = returnView();
				totalPrice.setText(displayString[1]);
				paymentType[2] += Integer.parseInt(input);
				//String change = String.format("%.2f",(Double.parseDouble(input)-Double.parseDouble(subTotal[2])));
				paymentType[3] =  paymentType[0] + paymentType[1] + paymentType[2] - subTotal[2];
				//totalPrice.setText("");
				barcode.setText("");
				
				totalPrice.append("\n" + "-----------------");
				
				if (paymentType[0] != 0) 
					
					totalPrice.append("\n" + "     CASH: " + df2.format(Double.parseDouble(String.valueOf(paymentType[0]))/100));
				if (paymentType[1] != 0) 
					totalPrice.append("\n" + "       CC: " + df2.format(Double.parseDouble(String.valueOf(paymentType[1]))/100));
				if (paymentType[2] != 0) 
					totalPrice.append("\n" + "    CHECK: " + df2.format(Double.parseDouble(String.valueOf(paymentType[2]))/100));
				
				
					
				
				
			    if (paymentType[3]>=0) {
			    	totalPrice.append("\n" + "   CHANGE: " + df2.format(Double.parseDouble(String.valueOf(paymentType[3]))/100));
			    	checkOutProcess();
			    	paymentType[0] = 0;
			    	paymentType[1] = 0;
			    	paymentType[2] = 0;
			    	paymentType[3] = 0;
			    }else {
			    	totalPrice.append("\n" + "  COLLECT: " + df2.format(Double.parseDouble(String.valueOf(paymentType[3]*-1))/100));
			    }
			}
		});
		
		btnCoupon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String amount = barcode.getText();
				addToCheckOutList(new BigInteger(amount).multiply(new BigInteger("-1")),1, 19);
				
				String displayString[] = returnView();
				
				totalPrice.setText("");
				itemTotalView.setText("");
				itemTotalView.append(displayString[0]);
			   
			    totalPrice.append(displayString[2]+"\n");
			    totalPrice.append(displayString[3]+"\n\n");
			    totalPrice.append(displayString[4] + "\n");
			    
		        barcode.setText("");
				
			}
		});
		
		JButton btnExit = new JButton("EXIT");
		btnExit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				frame.dispose();
			}
		});
		btnExit.setBounds(10, 855, 115, 42);
		frame.getContentPane().add(btnExit);
		
		JButton btnCoffeeClub = new JButton("COFFEE CLUB");
		btnCoffeeClub.setBounds(262, 746, 105, 42);
		frame.getContentPane().add(btnCoffeeClub);
		
		JButton btnTaxExempt = new JButton("TAX EXEMPT");
		btnTaxExempt.setBounds(10, 436, 105, 42);
		frame.getContentPane().add(btnTaxExempt);
		
		JButton btnPaidOut = new JButton("PAID OUT");
		btnPaidOut.setBounds(10, 488, 105, 42);
		frame.getContentPane().add(btnPaidOut);
		
		btnPaidOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String amount = barcode.getText();
				addToCheckOutList(new BigInteger(amount).multiply(new BigInteger("-1")),1, 21);
				
				String displayString[] = returnView();
				
				totalPrice.setText("");
				itemTotalView.setText("");
				itemTotalView.append(displayString[0]);
			   
			    totalPrice.append(displayString[2]+"\n");
			    totalPrice.append(displayString[3]+"\n\n");
			    totalPrice.append(displayString[4] + "\n");
			    
		        barcode.setText("");
				
			}
		});
		
		btnCoffeeClub.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String amount = "20";
				addToCheckOutList(new BigInteger(amount),1, 0);
				
				String displayString[] = returnView();
				
				totalPrice.setText("");
				itemTotalView.setText("");
				itemTotalView.append(displayString[0]);
			   
			    totalPrice.append(displayString[2]+"\n");
			    totalPrice.append(displayString[3]+"\n\n");
			    totalPrice.append(displayString[4] + "\n");
			    
		        barcode.setText("");
				
			}
		});
		
		btnTaxExempt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				taxExempt=1;
			}
		});
	
	}
}
