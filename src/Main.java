import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

public class Main {

	JFrame frame;
	AppThread appThread;
	Thread runThread;
	JButton actionButton;
	boolean busy = false;

	JTextField SSID;
	JTextField PASS;
	JTextField IF0;
	JTextField IF1;
	JTextField CHL;

	final int OFF = 0;
	final int STARTING = 1;
	final int ON = 2;
	final int STOPPING = 3;
	int state = OFF;

	public static void main(String[] args) {
		System.out.println("Starting...");
		new Main().init(args);
	}

	public void init(String[] args) {
		try {
			String config = FileOperator.load(Config.saveLocation);
			if(config != null)
				Config.applySettings(config);
			
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createAndShowGUI();
				}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void createAndShowGUI() {
		frame = new JFrame();
		frame.setTitle("PiWiFi");
		frame.setPreferredSize(new Dimension(260, 162));
		frame.setResizable(false);
		frame.setLayout(new GridLayout(6, 1));

		JPanel SSIDopts = new JPanel();
		JPanel PASSopts = new JPanel();
		JPanel IFACEopts = new JPanel();
		JPanel IFACEoptsSrc = new JPanel();
		JPanel CHLopts = new JPanel();

		SSID = new JTextField(Config.SSID);
		PASS = new JTextField(Config.PASS);
		IF0 = new JTextField(Config.CN0);
		IF1 = new JTextField(Config.CN1);
		CHL = new JTextField(Config.CHANNEL);

		SSIDopts.setLayout(new BorderLayout());
		PASSopts.setLayout(new BorderLayout());
		IFACEopts.setLayout(new BorderLayout());
		IFACEoptsSrc.setLayout(new BorderLayout());
		CHLopts.setLayout(new BorderLayout());

		SSIDopts.add(new JLabel(" SSID: "), BorderLayout.WEST);
		PASSopts.add(new JLabel(" Password: "), BorderLayout.WEST);
		IFACEopts.add(new JLabel(" Source interface: "), BorderLayout.WEST);
		IFACEoptsSrc.add(new JLabel(" AP interface: "), BorderLayout.WEST);
		CHLopts.add(new JLabel(" WiFi Channel for AP: "), BorderLayout.WEST);

		SSIDopts.add(SSID, BorderLayout.CENTER);
		PASSopts.add(PASS, BorderLayout.CENTER);
		IFACEopts.add(IF0, BorderLayout.CENTER);
		IFACEoptsSrc.add(IF1, BorderLayout.CENTER);
		CHLopts.add(CHL, BorderLayout.CENTER);

		actionButton = new JButton();
		actionButton.setText("START");
		actionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (state == OFF) {
					state = STARTING;
					((JButton) e.getSource()).setText("STARTING...");
					setFieldsEnabled(false);
					Config.SSID = SSID.getText();
					Config.PASS = PASS.getText();
					Config.CN0 = IF0.getText();
					Config.CN1 = IF1.getText();
					Config.CHANNEL = CHL.getText();
					appThread = new AppThread(AppThread.SETUP);
					new Thread(appThread).start();
					System.out.println("Started AppThread as SETUP");
				}
				if (state == ON) {
					state = STOPPING;
					((JButton) e.getSource()).setText("STOPPING...");
					appThread = new AppThread(AppThread.SHUTDOWN);
					new Thread(appThread).start();
					System.out.println("Started AppThread as SHUTDOWN");
				}
			}
		});

		frame.add(SSIDopts);
		frame.add(PASSopts);
		frame.add(IFACEopts);
		frame.add(IFACEoptsSrc);
		frame.add(CHLopts);
		frame.add(actionButton);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				if(state == OFF){
					FileOperator.save(Config.saveLocation, Config.getSettings());
					System.exit(0);
				}
			}
		});
		ActionListener timerTickTask = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				timerTick();
			}
		};
		Timer timer = new Timer(200, timerTickTask);
		timer.start();
	}

	void timerTick() {
		try {
			if (appThread != null) {
				if (appThread.status == AppThread.FINISHED) {
					if (state == STARTING) {
						state = ON;
						actionButton.setText("STOP");
						appThread = null;
						runThread = new Thread(new AppThread(AppThread.RUN));
						runThread.start();
					}
					if (state == STOPPING) {
						state = OFF;
						setFieldsEnabled(true);
						actionButton.setText("START");
						appThread = null;
					}
				}
				if (appThread.status == AppThread.ERROR) {
					// TODO handle errors gracefully
					System.exit(0);
				}
			}
		} catch (Exception e) {
		}
	}

	void setFieldsEnabled(boolean val) {
		SSID.setEnabled(val);
		PASS.setEnabled(val);
		IF0.setEnabled(val);
		IF1.setEnabled(val);
		CHL.setEnabled(val);
	}

}
