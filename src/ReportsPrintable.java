import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ReportsPrintable implements Printable {
	ArrayList<String> strReport = new ArrayList();
 
	
	public ReportsPrintable(ArrayList<String> x) {
		strReport = x;
	}
	
	public int print(Graphics graphics, PageFormat pageFormat,int pageIndex) throws PrinterException {    
		int result = NO_SUCH_PAGE;    
		if (pageIndex == 0) {                    
			Graphics2D g2d = (Graphics2D) graphics;                    
			
			try{
			    int y=20;
			    int yShift = 10;
			    int headerRectHeight=15;
			    g2d.setFont(new Font("Monospaced",Font.TYPE1_FONT,9));
			    for(int i=0; i< strReport.size(); i++) {
			    
			    	g2d.drawString(strReport.get(i),12,y);y+=yShift;
			    }
			    
			    
			}
			    
			    
			    
			    
			catch(Exception e){
				e.printStackTrace();
			}
			result = PAGE_EXISTS;    
		}    
		return result;    
	}
}