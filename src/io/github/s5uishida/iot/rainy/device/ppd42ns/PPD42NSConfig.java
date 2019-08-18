package io.github.s5uishida.iot.rainy.device.ppd42ns;

import io.github.s5uishida.iot.rainy.util.AbstractConfig;
import io.github.s5uishida.iot.rainy.util.ConfigParams;

/*
 * @author s5uishida
 *
 */
public class PPD42NSConfig extends AbstractConfig {
	public static final String INFLUXDB_KEY								= "influxDB";
	public static final String MQTT_KEY									= "mqtt";
	public static final String PRETTY_PRINTING_KEY						= "prettyPrinting";
	public static final String READ_CRONTAB_KEY							= "readCrontab";

	private static PPD42NSConfig config;

	private PPD42NSConfig(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static PPD42NSConfig getInstance() {
		if (config == null) {
			config = new PPD42NSConfig(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.PPD42NS_CONFIG_FILE);
		}
		return config;
	}

	public boolean getInfluxDB() {
		return getConfig(INFLUXDB_KEY, false);
	}

	public boolean getMqtt() {
		return getConfig(MQTT_KEY, false);
	}

	public boolean getPrettyPrinting() {
		return getConfig(PRETTY_PRINTING_KEY, false);
	}

	public String getReadCrontab() {
		return getConfig(READ_CRONTAB_KEY, "* * * * *");
	}
}
