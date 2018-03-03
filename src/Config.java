/* PREREQUISITES 
 * dnsmasq is installed
 * dhcpcd is installed
 * hostapd is installed
 * 
 * run this on a Raspberry Pi
 * hostapd-compatible dongle
 * another dongle for reception
 * ipv4 forwarding is enabled in /etc/sysctl.conf
 * 
 * BEFORE RUNNING, ENSURE
 * the internet is connected (may not be necessary)
 * the target interface is NOT ASSOCIATED with ANYTHING
 */

public class Config {
	static String SSID = "raspberry";
	static String PASS = "password";
	static String CN0 = "wlan0";
	static String CN1 = "wlan1";
	static String CHANNEL = "6";
	
	static String saveLocation = "/etc/piwifi-config";
	
	public static void applySettings(String config){
		String[] configs = config.split("\n");
		for (int x = 0; x < configs.length; x++) {
			if (configs[x].indexOf("ssid=") == 0) {
				SSID = configs[x].substring(5);
			}
			if (configs[x].indexOf("pass=") == 0) {
				PASS = configs[x].substring(5);
			}
			if (configs[x].indexOf("cn0=") == 0) {
				CN0 = configs[x].substring(4);
			}
			if (configs[x].indexOf("cn1=") == 0) {
				CN1 = configs[x].substring(4);
			}
			if (configs[x].indexOf("channel=") == 0) {
				CHANNEL = configs[x].substring(8);
			}
		}
	}
	public static String getSettings(){
		return "ssid="+SSID+
				"\npass="+PASS+
				"\ncn0="+CN0+
				"\ncn1="+CN1+
				"\nchannel="+CHANNEL;
	}
	
	// Step 1 - set /etc/dhcpcd.conf to the following after #PiWiFi\n
	public static String dhcpcd_conf = "\n#PiWiFi\n"
			+ "interface PiWiFi-conn1\n" + "static ip_address=192.168.4.1/24\n"
			+ "static routers=192.168.4.1\n"
			+ "static domain_name_servers=8.8.8.8";

	// Step 3 - set /etc/hostapd/hostapd.conf to the following
	public static String hostapd_conf = "# This file is managed by PiWiFi. Changes will be overwritten when PiWiFi is run.\n"
			+ "interface=PiWiFi-conn1\n"
			+ "driver=nl80211\n"
			+ "hw_mode=g\n"
			+ "channel=PiWiFi-channel\n"
			+ "#ieee80211n=1\n"
			+ "wmm_enabled=0\n"
			+ "ht_capab=[HT40][SHORT-GI-20][DSSS_CCK-40]\n"
			+ "macaddr_acl=0\n"
			+ "ignore_broadcast_ssid=0\n"
			+ "#auth_algs=1\n"
			+ "wpa=2\n"
			+ "wpa_key_mgmt=WPA-PSK\n"
			+ "rsn_pairwise=CCMP\n"
			+ "ssid=PiWiFi-name\n"
			+ "wpa_passphrase=PiWiFi-password\n";

	// Step 4 - set /etc/dnsmasq.conf to the following:
	public static String dnsmasq_conf = "# This file is managed by PiWiFi. Changes will be overwritten when PiWiFi is run.\n"
			+ "interface=PiWiFi-conn1      # Use interface PiWiFi-conn1\n"
			+ "domain-needed        # Don't forward short names\n"
			+ "bogus-priv           # Drop the non-routed address spaces.\n"
			+ "dhcp-range=192.168.4.8,192.168.4.250,12h";

	// Step 6 - update iptables iptable_cfg to forward traffic, start services,
	// and the access point is up!

	public static String finish_cmds = 
			  "sudo ifdown PiWiFi-conn1\n"
			+ "sudo iptables -t nat -A POSTROUTING -o PiWiFi-conn0 -j MASQUERADE\n"
			+ "sudo iptables -A FORWARD -i PiWiFi-conn0 -o PiWiFi-conn1 -m state --state RELATED,ESTABLISHED -j ACCEPT\n"
			+ "sudo iptables -A FORWARD -i PiWiFi-conn1 -o PiWiFi-conn0 -j ACCEPT\n"
			+ "sudo service hostapd restart\n"
			+ "sudo service dnsmasq restart\n"
	 		+ "sudo service dhcpcd restart\n";
	

	public static String run_cmds = "sudo hostapd -d /etc/hostapd/hostapd.conf";
	public static String stop_cmds = "sudo service hostapd stop\nsudo killall hostapd\nsudo service dnsmasq stop\nsudo ifup PiWiFi-conn1";
}
