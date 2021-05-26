import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class ReceiptPrintable implements Printable {
	ArrayList<String> strReceipt = new ArrayList();
	
	public ReceiptPrintable(ArrayList<String> x) {
		strReceipt = x;
		
	}
	
	public int print(Graphics graphics, PageFormat pageFormat,int pageIndex) throws PrinterException {    
		int result = NO_SUCH_PAGE;    
		System.out.println("Asdfasdfasdf");
		if (pageIndex == 0) {                    
			Graphics2D g2d = (Graphics2D) graphics;   
			
			try{
			    int y=20;
			    int yShift = 10;
			    int headerRectHeight=15;
			    g2d.setFont(new Font("Monospaced",Font.TYPE1_FONT,9));
			    for(int i=0; i< strReceipt.size(); i++) {
			     
			    	g2d.drawString( strReceipt.get(i),12,y);y+=yShift;
			    }			    
			}catch(Exception e){
				e.printStackTrace();
			}
			result = PAGE_EXISTS;    
		}    
		return result;    
	}
}