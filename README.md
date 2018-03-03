# piwifi
Simple Java application for running a Wi-Fi repeater from a Raspberry Pi

## Usage
To install:
1. Download the `piwifi.jar` file to a folder of your choice.
2. If you already have hostapd or dnsmasq installed, first run `sudo apt-get purge dnsmasq hostapd` to purge the configuration files.
2. Run `sudo apt-get install -y hostapd dnsmasq` to install the necessary dependencies.
3. Edit `\etc\sysctl.conf` and uncomment `net.ipv4.ip_forward=1` to enable ipv4 forwarding.
4. *Recommended:* Disable the built-in wifi on RPi3 by editing `/boot/config.txt` and adding `dtoverlay=pi3-disable-wifi`. It often causes interference.

To run:
1. `sudo java -jar \path\to\piwifi.jar`
2. You can edit the following text fields:
* SSID - the name of the network
* Password - the password to use for the network
* Source interface - the interface that the internet comes into
* AP interface - the interface that is used to host the access point
* WiFi Channel - the WiFi channel that the AP uses.
 * Allowed - a number from 1 to 11
 * Recommended - 1, 6, or 11
3. Press the "START" button. The first time, configuration may take a while. Subsequent launches should be faster.

After running the hotspot, all fields are saved in `/etc/piwifi-config`.

## Troubleshooting
* Make sure that whatever interface you are using for the AP interface is Access Point capable (tested with the official RPi wifi dongle)
* Make sure that the source interface is connected to the internet. It sometimes disconnects when the hotspot is started for the first time.
* Unplug and replug the dongle used for wlan1 to make sure that it is actually wlan1
* If the above does not work, feel free to post an issue.
* Tested on a fresh install of Raspbian Jessie with a generic dongle as wlan0 and the offical Rpi dongle as wlan1.
