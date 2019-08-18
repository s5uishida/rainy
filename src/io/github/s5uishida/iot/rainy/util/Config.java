package io.github.s5uishida.iot.rainy.util;

/*
 * @author s5uishida
 *
 */
public class Config extends AbstractConfig {
	public static final String CLIENT_ID_KEY		= "clientID";
	public static final String CC2650_KEY		= "cc2650";
	public static final String MHZ19B_KEY		= "mhz19b";
	public static final String PPD42NS_KEY		= "ppd42ns";
	public static final String OPCUA_KEY			= "opcua";

	private static Config config;

	private Config(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static Config getInstance() {
		if (config == null) {
			config = new Config(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.CONFIG_FILE);
		}
		return config;
	}

	public String getClientID() {
		return getConfig(CLIENT_ID_KEY, "client0");
	}

	public boolean getCC2650() {
		return getConfig(CC2650_KEY, false);
	}

	public boolean getMHZ19B() {
		return getConfig(MHZ19B_KEY, false);
	}

	public boolean getPPD42NS() {
		return getConfig(PPD42NS_KEY, false);
	}

	public boolean getOPCUA() {
		return getConfig(OPCUA_KEY, false);
	}
}
