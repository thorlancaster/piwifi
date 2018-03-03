public class AppThread implements Runnable {
	public int status;

	public static final int IDLE = 0;
	public static final int WRITING_FILES = 1;
	public static final int RUNNING_SCRIPTS = 16;
	public static final int UPDATING_IPTABLES = 24;
	public static final int RUNNING = 28;
	public static final int FINISHED = 32;
	public static final int ERROR = 33;

	public static final int SETUP = 96;
	public static final int RUN = 97;
	public static final int SHUTDOWN = 98;

	private int task = 0;

	AppThread(int task) {
		if (task != SETUP && task != SHUTDOWN && task != RUN)
			throw new IllegalArgumentException(
					"task must be SETUP, RUN, or SHUTDOWN");
		this.task = task;
	}

	public void run() {
		try {
			if (task == SETUP) {
				status = IDLE;
				// Step 1 - dhcpcd
				System.out.println("AppThread: Writing dhcpcd");
				status = WRITING_FILES;
				String dhcpcd_conf = FileOperator.load("/etc/dhcpcd.conf");
				if (dhcpcd_conf == null || dhcpcd_conf.length() < 5)
					throw new RuntimeException(
							"Failed to load /etc/dhcpcd.conf. ABORT");

				if (dhcpcd_conf.indexOf("\n#PiWiFi") >= 0) {
					dhcpcd_conf = dhcpcd_conf.substring(0,
							dhcpcd_conf.indexOf("\n#PiWiFi"));
				}
				dhcpcd_conf += Config.dhcpcd_conf;
				dhcpcd_conf = dhcpcd_conf.replaceAll("PiWiFi-conn0", Config.CN0)
						.replaceAll("PiWiFi-conn1", Config.CN1);
				if (!FileOperator.save("/etc/dhcpcd.conf", dhcpcd_conf))
					throw new RuntimeException("Error writing to dhcpcd.conf");

				// Step 2 or later - sudo service dhcpcd restart // TODO done
				// run
				// cmd

				// Step 3 - hostapd
				System.out.println("AppThread: Writing hostapd");
				if (!FileOperator.save(
						"/etc/hostapd/hostapd.conf",
						Config.hostapd_conf.replaceAll("PiWiFi-conn0", Config.CN0)
								.replaceAll("PiWiFi-conn1", Config.CN1)
								.replaceAll("PiWiFi-name", Config.SSID)
								.replaceAll("PiWiFi-password", Config.PASS)
								.replaceAll("PiWiFi-channel", Config.CHANNEL)))
					throw new RuntimeException("Error writing to hostapd.conf");

				// Step 4 - dnsmasq
				System.out.println("AppThread: Writing dnsmasq");
				if (!FileOperator.save("/etc/dnsmasq.conf",
						Config.dnsmasq_conf.replaceAll("PiWiFi-conn0", Config.CN0)
								.replaceAll("PiWiFi-conn1", Config.CN1)))
					throw new RuntimeException("Error writing to dnsmasq.conf");

				// Step 6 - update iptables and run other scripts
				status = RUNNING_SCRIPTS;
				System.out.println("AppThread: running config scripts");
				String[] commands = Config.finish_cmds
						.replaceAll("PiWiFi-conn0", Config.CN0)
						.replaceAll("PiWiFi-conn1", Config.CN1).split("\n");
				for (int x = 0; x < commands.length; x++) {
					System.out.println(commands[x]);
					CommandLine.exec(commands[x]);
				}
				status = FINISHED;
			} else if (task == RUN) {
				status = RUNNING;
				String[] commands = Config.run_cmds
						.replaceAll("PiWiFi-conn0", Config.CN0)
						.replaceAll("PiWiFi-conn1", Config.CN1).split("\n");
				for (int x = 0; x < commands.length; x++) {
					System.out.println(commands[x]);
					CommandLine.exec(commands[x]);
				}
			} else if (task == SHUTDOWN) {
				status = RUNNING;
				String[] commands = Config.stop_cmds
						.replaceAll("PiWiFi-conn0", Config.CN0)
						.replaceAll("PiWiFi-conn1", Config.CN1).split("\n");
				for (int x = 0; x < commands.length; x++) {
					System.out.println(commands[x]);
					CommandLine.exec(commands[x]);
				}
				status = FINISHED;
			}
		} catch (Exception e) {
			status = ERROR;
		}
	}
}