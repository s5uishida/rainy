package io.github.s5uishida.iot.rainy.device.mhz19b;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	public static final String AUTO_CALIBRATION_KEY						= "autoCalibration";
	public static final String ZERO_CALIBRATION_KEY						= "zeroCalibration";
	public static final String DETECTION_RANGE_KEY						= "detectionRange";

	private static MHZ19BConfig config;
	private static List<String> portNameList = new ArrayList<String>();

	private MHZ19BConfig(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static MHZ19BConfig getInstance() {
		if (config == null) {
			config = new MHZ19BConfig(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.MHZ19B_CONFIG_FILE);
			List<String> portNames = Arrays.asList(config.getConfig(PORT_NAME_KEY, "/dev/ttyAMA0").split("\\s+"));
			for (String portName : portNames) {
				if (!portNameList.contains(portName)) {
					portNameList.add(portName);
				}
			}
		}
		return config;
	}

	public List<String> getPortName() {
		return portNameList;
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

	public boolean getAutoCalibration() {
		return getConfig(AUTO_CALIBRATION_KEY, false);
	}

	public boolean getZeroCalibration() {
		return getConfig(ZERO_CALIBRATION_KEY, false);
	}

	public int getDetectionRange() {
		int range = getConfig(DETECTION_RANGE_KEY, 5000);
		if (range != 2000 && range != 5000) {
			range = 5000;
		}
		return range;
	}
}
