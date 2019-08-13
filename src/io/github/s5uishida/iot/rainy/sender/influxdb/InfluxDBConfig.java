package io.github.s5uishida.iot.rainy.sender.influxdb;

import io.github.s5uishida.iot.rainy.util.AbstractConfig;
import io.github.s5uishida.iot.rainy.util.ConfigParams;

/*
 * @author s5uishida
 *
 */
public class InfluxDBConfig extends AbstractConfig {
	public static final String INFLUXDB_URL_KEY		= "influxDBUrl";
	public static final String USER_NAME_KEY			= "userName";
	public static final String PASSWORD_KEY			= "password";
	public static final String ACTIONS_KEY			= "actions";
	public static final String FLUSH_DURATION_KEY	= "flushDuration";
	public static final String DATA_ONLY_KEY			= "dataOnly";

	private static InfluxDBConfig config;

	private InfluxDBConfig(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static InfluxDBConfig getInstance() {
		if (config == null) {
			config = new InfluxDBConfig(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.INFLUXDB_CONFIG_FILE);
		}
		return config;
	}

	public String getInfluxDBUrl() {
		return getConfig(INFLUXDB_URL_KEY, null);
	}

	public String getUserName() {
		return getConfig(USER_NAME_KEY, null);
	}

	public String getPassword() {
		return getConfig(PASSWORD_KEY, null);
	}

	public int getActions() {
		return getConfig(ACTIONS_KEY, 1000);
	}

	public int getFlushDuration() {
		return getConfig(FLUSH_DURATION_KEY, 1000);
	}

	public boolean getDataOnly() {
		return getConfig(DATA_ONLY_KEY, true);
	}
}
