package io.github.s5uishida.iot.rainy.device.mhz19b;

import io.github.s5uishida.iot.rainy.util.AbstractConfig;
import io.github.s5uishida.iot.rainy.util.ConfigParams;

/*
 * @author s5uishida
 *
 */
public class MHZ19BConfig extends AbstractConfig {
	public static final String PORT_NAME_KEY								= "portName";
	public static final String INFLUXDB_KEY								= "influxDB";
	public static final String MQTT_KEY									= "mqtt";
	public static final String PRETTY_PRINTING_KEY						= "prettyPrinting";
	public static final String READ_CRONTAB_KEY							= "readCrontab";

	private static MHZ19BConfig config;

	private MHZ19BConfig(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static MHZ19BConfig getInstance() {
		if (config == null) {
			config = new MHZ19BConfig(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.MHZ19B_CONFIG_FILE);
		}
		return config;
	}

	public String getPortName() {
		return getConfig(PORT_NAME_KEY, "/dev/ttyAMA0");
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
