import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import static java.awt.print.Printable.NO_SUCH_PAGE;
import static java.awt.print.Printable.PAGE_EXISTS;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Print{
	 
	public Print(String x){
		PrinterJob pj = PrinterJob.getPrinterJob();
		if (x.equals("receipt")) {
			Receipt test = new Receipt();
			int len = test.receiptView.size();
			for(int i=0; i<test.receiptView.size(); i++) {
				System.out.println(test.receiptView.get(i));
			}
			pj.setPrintable(new ReceiptPrintable(test.receiptView),getPageFormat(pj, x, len));
		}else {
			Reports test = new Reports(x);
			int len = test.reportView.size();
			for(int i=0; i<test.reportView.size(); i++) {
				System.out.println(test.reportView.get(i));
			}
			pj.setPrintable(new ReportsPrintable(test.reportView),getPageFormat(pj,x,len));
		}
		try {
			pj.print();  
		}
		catch (PrinterException ex) {
			ex.printStackTrace();
		} 
    }
	
	public PageFormat getPageFormat(PrinterJob pj, String x, int len){
		PageFormat pf = pj.defaultPage();
		Paper paper = pf.getPaper();
		double bodyHeight = 0;
		if (x.equals("report") || x.equals("shift")) {
			bodyHeight = 12 + len; 
		}else
			bodyHeight = 6+ len; 
		
		double headerHeight = 1.0;                  
		double footerHeight = 5.0;        
		double width = cm_to_pp(18); 
		double height = cm_to_pp(headerHeight+bodyHeight+footerHeight); 
		paper.setSize(width, height);
		paper.setImageableArea(0,10,width,height - cm_to_pp(1));  
		        
		pf.setOrientation(PageFormat.PORTRAIT);  
		pf.setPaper(paper);    
		
		return pf;
    }
       
    protected static double cm_to_pp(double cm){            
    	return toPPI(cm * 0.393600787);            
    }
 
    protected static double toPPI(double inch){            
    	return inch * 72d;            
    }

   
}
  
   