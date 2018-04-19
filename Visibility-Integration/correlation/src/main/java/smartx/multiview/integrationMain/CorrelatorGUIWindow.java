package smartx.multiview.integrationMain;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JLabel;
import java.awt.Color;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFormattedTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CorrelatorGUIWindow {
	private JFrame frame;
	private JFormattedTextField formattedTextField;
	private JFormattedTextField formattedTextField_1;
	private JFormattedTextField formattedTextField_2;
	private JFormattedTextField formattedTextField_3;
	private JFormattedTextField formattedTextField_4;
	private JFormattedTextField formattedTextField_5;
	private JFormattedTextField formattedTextField_6;
	private JFormattedTextField formattedTextField_7;
	private JButton btnExit;
	public JTextArea textArea;
	protected static CorrelatorGUIWindow window;
	protected CorrelatorMain correlate;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new CorrelatorGUIWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public CorrelatorGUIWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 514, 395);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnCorrelate = new JButton("Correlate");
		btnCorrelate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("Correlation Process Started ...");
				textArea.setText(textArea.getText()+"\nProcess Start : "+new Date());
				correlate = new CorrelatorMain(formattedTextField.getText(),formattedTextField_1.getText(),formattedTextField_2.getText(),formattedTextField_3.getText(),formattedTextField_4.getText(),formattedTextField_6.getText(),formattedTextField_5.getText(),formattedTextField_7.getText(), window);
				
				//textArea.setText(textArea.getText()+"\nProcess Start Timestamp: ");
				textArea.setText(textArea.getText()+"\nProcess Stop :  "+new Date());
			}
		});
		btnCorrelate.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnCorrelate.setToolTipText("Click Button to Start Correlation");
		btnCorrelate.setBounds(159, 330, 89, 23);
		frame.getContentPane().add(btnCorrelate);
		
		btnExit = new JButton("Exit");
		btnExit.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnExit.setForeground(Color.RED);
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		btnExit.setBounds(250, 330, 89, 23);
		frame.getContentPane().add(btnExit);
		
		JLabel lblStartTime = new JLabel("Start Time");
		lblStartTime.setBackground(Color.WHITE);
		lblStartTime.setBounds(76, 64, 66, 14);
		frame.getContentPane().add(lblStartTime);
		
		JLabel lblEndTime = new JLabel("End Time");
		lblEndTime.setBounds(76, 91, 93, 14);
		frame.getContentPane().add(lblEndTime);
		
		JLabel lblSourceIp = new JLabel("Source IP");
		lblSourceIp.setBounds(76, 118, 93, 14);
		frame.getContentPane().add(lblSourceIp);
		
		JLabel lblDestinationIp = new JLabel("Destination IP");
		lblDestinationIp.setBounds(76, 143, 93, 14);
		frame.getContentPane().add(lblDestinationIp);
		
		JLabel lblHistorybasedVisibilityCorrelator = DefaultComponentFactory.getInstance().createTitle("History-based Visibility Correlator");
		lblHistorybasedVisibilityCorrelator.setFont(new Font("Cambria", Font.BOLD | Font.ITALIC, 18));
		lblHistorybasedVisibilityCorrelator.setBounds(76, 11, 334, 26);
		lblHistorybasedVisibilityCorrelator.setHorizontalAlignment(JLabel.CENTER) ;
		frame.getContentPane().add(lblHistorybasedVisibilityCorrelator);
		
		JLabel lblSourcePort = new JLabel("Source Port");
		lblSourcePort.setBounds(76, 168, 93, 14);
		frame.getContentPane().add(lblSourcePort);
		
		JLabel lblProtocol = new JLabel("Protocol");
		lblProtocol.setBounds(76, 196, 93, 14);
		frame.getContentPane().add(lblProtocol);
		
		JLabel lblSdnControllerIp = new JLabel("Developer Controller IP");
		lblSdnControllerIp.setBounds(76, 221, 135, 14);
		frame.getContentPane().add(lblSdnControllerIp);
		
		JLabel lblOperatorControllerIp = new JLabel("Operator Controller IP");
		lblOperatorControllerIp.setBounds(76, 249, 135, 14);
		frame.getContentPane().add(lblOperatorControllerIp);
		
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		MaskFormatter dateMask1 = null, dateMask2 = null, ipMask1 = null, ipMask2 = null;
		try {
			dateMask1 = new MaskFormatter("##-##-#### ##:##:##");
			dateMask2 = new MaskFormatter("##-##-#### ##:##:##");
			ipMask1   = new MaskFormatter("###.###.###.###");
			ipMask2   = new MaskFormatter("###.###.###.###");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		formattedTextField = new JFormattedTextField(df);
		formattedTextField.setToolTipText("dd-MM-yyyy HH:mm:ss");
		formattedTextField.setForeground(Color.BLUE);
        dateMask1.install(formattedTextField);
		formattedTextField.setBounds(241, 64, 169, 18);
		formattedTextField.setHorizontalAlignment(JTextField.CENTER);
		frame.getContentPane().add(formattedTextField);
		
		formattedTextField_1 = new JFormattedTextField(df);
		formattedTextField_1.setToolTipText("dd-MM-yyyy HH:mm:ss");
		formattedTextField_1.setForeground(Color.BLUE);
		dateMask2.install(formattedTextField_1);
		formattedTextField_1.setBounds(240, 91, 170, 18);
		formattedTextField_1.setHorizontalAlignment(JTextField.CENTER);
		frame.getContentPane().add(formattedTextField_1);
		
		formattedTextField_2 = new JFormattedTextField();
		formattedTextField_2.setText("192.168.1.1");
		formattedTextField_2.setForeground(Color.BLUE);
		//dateMask.install(formattedTextField_2);
		formattedTextField_2.setBounds(240, 118, 170, 18);
		formattedTextField_2.setHorizontalAlignment(JTextField.CENTER) ;
		frame.getContentPane().add(formattedTextField_2);
		
		formattedTextField_3 = new JFormattedTextField();
		formattedTextField_3.setText("192.168.1.2");
		formattedTextField_3.setForeground(Color.BLUE);
		//dateMask.install(formattedTextField_3);
		formattedTextField_3.setBounds(241, 143, 169, 18);
		formattedTextField_3.setHorizontalAlignment(JTextField.CENTER) ;
		frame.getContentPane().add(formattedTextField_3);
		
		formattedTextField_4 = new JFormattedTextField();
		formattedTextField_4.setForeground(Color.BLUE);
		//dateMask.install(formattedTextField_4);
		formattedTextField_4.setBounds(240, 168, 170, 18);
		formattedTextField_4.setHorizontalAlignment(JTextField.CENTER) ;
		frame.getContentPane().add(formattedTextField_4);
		
		formattedTextField_5 = new JFormattedTextField();
		formattedTextField_5.setForeground(Color.BLUE);
		//dateMask.install(formattedTextField_5);
		formattedTextField_5.setBounds(240, 196, 170, 18);
		formattedTextField_5.setHorizontalAlignment(JTextField.CENTER) ;
		frame.getContentPane().add(formattedTextField_5);
		
		formattedTextField_6 = new JFormattedTextField();
		formattedTextField_6.setText("x.x.x.x");
		formattedTextField_6.setForeground(Color.BLUE);
		//dateMask.install(formattedTextField_6);
		formattedTextField_6.setBounds(241, 221, 169, 18);
		formattedTextField_6.setHorizontalAlignment(JTextField.CENTER) ;
		frame.getContentPane().add(formattedTextField_6);
		
		formattedTextField_7 = new JFormattedTextField();
		formattedTextField_7.setText("x.x.x.x");
		formattedTextField_7.setForeground(Color.BLUE);
		//dateMask.install(formattedTextField_7);
		formattedTextField_7.setBounds(241, 249, 169, 18);
		formattedTextField_7.setHorizontalAlignment(JTextField.CENTER) ;
		frame.getContentPane().add(formattedTextField_7);
		
		textArea = new JTextArea();
		textArea.setRows(3);
		textArea.setBounds(76, 138, 497, 1805);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		frame.getContentPane().add(textArea);
		
		JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBounds(76, 269, 334, 50);
        frame.getContentPane().add(scroll);
		
	}
}
