import javax.swing.JFrame;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import javax.swing.DropMode;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigInteger;
import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class PriceCheck {
	private JTextField textField;
	
	public PriceCheck(){
		init();
	}
	
	public void init() {
		JFrame priceCheckFrame = new JFrame();
		priceCheckFrame.setVisible(true);
		priceCheckFrame.setSize(450, 250);;
		priceCheckFrame.getContentPane().setLayout(null);
		priceCheckFrame.setLocationRelativeTo(null);
		
		
		JTextField barcode = new JTextField();
		barcode.setBounds(10, 10, 400, 28);
		priceCheckFrame.getContentPane().add(barcode);
		barcode.setColumns(10);
		
		JLabel lblBarCode = new JLabel("BARCODE");
		lblBarCode.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblBarCode.setBounds(10, 48, 68, 13);
		priceCheckFrame.getContentPane().add(lblBarCode);
		
		JLabel lblPrice = new JLabel("PRICE");
		lblPrice.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblPrice.setBounds(271, 48, 68, 13);
		priceCheckFrame.getContentPane().add(lblPrice);
		
		JLabel lblShortDec = new JLabel("SHORT DESCRIPTION");
		lblShortDec.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblShortDec.setBounds(271, 121, 131, 13);
		priceCheckFrame.getContentPane().add(lblShortDec);
		
		JLabel lblFullDec = new JLabel("FULL DESCRIPTION");
		lblFullDec.setFont(new Font("Monospaced", Font.PLAIN, 12));
		lblFullDec.setBounds(10, 121, 131, 13);
		priceCheckFrame.getContentPane().add(lblFullDec);
		
		JTextArea showBarcode = new JTextArea();
		showBarcode.setBounds(10, 71, 131, 22);
		priceCheckFrame.getContentPane().add(showBarcode);
		
		JTextArea showPrice = new JTextArea();
		showPrice.setBounds(271, 71, 72, 22);
		priceCheckFrame.getContentPane().add(showPrice);
		
		JTextArea showFullDes = new JTextArea();
		showFullDes.setBounds(10, 144, 211, 59);
		priceCheckFrame.getContentPane().add(showFullDes);
		
		JTextArea showShortDes = new JTextArea();
		showShortDes.setBounds(271, 144, 139, 22);
		priceCheckFrame.getContentPane().add(showShortDes);
	
		
		barcode.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER){
					DecimalFormat df2 = new DecimalFormat("#0.00");
							
					String searchBarcode = barcode.getText();
					Item searchItem = new Item(new BigInteger(searchBarcode));
										
					showPrice.setText(df2.format((Double.parseDouble(String.valueOf(searchItem.priceEach))/100)));
					showFullDes.setText(searchItem.fullDescription);
					showShortDes.setText(searchItem.shortDescription);
					showBarcode.setText(String.valueOf(searchItem.barcode));

			        barcode.setText("");
		        }
			}
		});
	}
}
