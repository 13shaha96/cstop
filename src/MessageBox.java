
import javax.swing.JOptionPane;

public class MessageBox{

    public static void infoBox(String titleBar, String infoMessage)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
}