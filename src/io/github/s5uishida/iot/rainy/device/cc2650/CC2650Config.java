package io.github.s5uishida.iot.rainy.device.cc2650;

import java.util.Arrays;
import java.util.List;

import io.github.s5uishida.iot.device.tisensortag.cc2650.driver.CC2650Driver;
import io.github.s5uishida.iot.rainy.util.AbstractConfig;
import io.github.s5uishida.iot.rainy.util.ConfigParams;

/*
 * @author s5uishida
 *
 */
public class CC2650Config extends AbstractConfig {
	public static final String BLUETOOTH_ADAPTER_KEY					= "bluetoothAdapter";
	public static final String INFLUXDB_KEY								= "influxDB";
	public static final String MQTT_KEY									= "mqtt";
	public static final String PRETTY_PRINTING_KEY						= "prettyPrinting";
	public static final String READ_CRONTAB_KEY							= "readCrontab";
	public static final String BATTERY_LEVEL_KEY						= "batteryLevel";
	public static final String TEMPERATURE_KEY							= "temperature";
	public static final String TEMPERATURE_NOTIFY_KEY					= "temperatureNotify";
	public static final String TEMPERATURE_NOTIFICATION_PERIOD_KEY	= "temperatureNotificationPeriod";
	public static final String HUMIDITY_KEY								= "humidity";
	public static final String HUMIDITY_NOTIFY_KEY						= "humidityNotify";
	public static final String HUMIDITY_NOTIFICATION_PERIOD_KEY		= "humidityNotificationPeriod";
	public static final String PRESSURE_KEY								= "pressure";
	public static final String PRESSURE_NOTIFY_KEY						= "pressureNotify";
	public static final String PRESSURE_NOTIFICATION_PERIOD_KEY		= "pressureNotificationPeriod";
	public static final String OPTICAL_KEY								= "optical";
	public static final String OPTICAL_NOTIFY_KEY						= "opticalNotify";
	public static final String OPTICAL_NOTIFICATION_PERIOD_KEY		= "opticalNotificationPeriod";
	public static final String GYROSCOPE_KEY								= "gyroscope";
	public static final String ACCELEROMETER_KEY						= "accelerometer";
	public static final String MAGNETOMETER_KEY							= "magnetometer";
	public static final String MOVEMENT_NOTIFY_KEY						= "movementNotify";
	public static final String MOVEMENT_NOTIFICATION_PERIOD_KEY		= "movementNotificationPeriod";
	public static final String WAKE_ON_MOTION_KEY						= "wakeOnMotion";
	public static final String ACCELEROMETER_RANGE_KEY					= "accelerometerRange";
	public static final String DEVICES_KEY								= "devices";

	private static CC2650Config config;
	private static List<String> devices;

	private CC2650Config(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static CC2650Config getInstance() {
		if (config == null) {
			config = new CC2650Config(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.CC2650_CONFIG_FILE);
			devices = Arrays.asList(config.getConfig(DEVICES_KEY, "").split("\\s+"));
		}
		return config;
	}

	public boolean selectableDevice(String address) {
		return devices.contains(address);
	}

	public String getBluetoothAdapter() {
		return getConfig(BLUETOOTH_ADAPTER_KEY, "hci0");
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

	public boolean getBatteryLevel() {
		return getConfig(BATTERY_LEVEL_KEY, false);
	}

	public boolean getTemperature() {
		return getConfig(TEMPERATURE_KEY, false);
	}

	public boolean getTemperatureNotify() {
		return getConfig(TEMPERATURE_NOTIFY_KEY, false);
	}

	public int getTemperatureNotificationPeriod() {
		return getConfig(TEMPERATURE_NOTIFICATION_PERIOD_KEY, CC2650Driver.TEMPERATURE_NOTIFICATION_PERIOD_DEFAULT_MILLIS);
	}

	public boolean getHumidity() {
		return getConfig(HUMIDITY_KEY, false);
	}

	public boolean getHumidityNotify() {
		return getConfig(HUMIDITY_NOTIFY_KEY, false);
	}

	public int getHumidityNotificationPeriod() {
		return getConfig(HUMIDITY_NOTIFICATION_PERIOD_KEY, CC2650Driver.HUMIDITY_NOTIFICATION_PERIOD_DEFAULT_MILLIS);
	}

	public boolean getPressure() {
		return getConfig(PRESSURE_KEY, false);
	}

	public boolean getPressureNotify() {
		return getConfig(PRESSURE_NOTIFY_KEY, false);
	}

	public int getPressureNotificationPeriod() {
		return getConfig(PRESSURE_NOTIFICATION_PERIOD_KEY, CC2650Driver.PRESSURE_NOTIFICATION_PERIOD_DEFAULT_MILLIS);
	}

	public boolean getOptical() {
		return getConfig(OPTICAL_KEY, false);
	}

	public boolean getOpticalNotify() {
		return getConfig(OPTICAL_NOTIFY_KEY, false);
	}

	public int getOpticalNotificationPeriod() {
		return getConfig(OPTICAL_NOTIFICATION_PERIOD_KEY, CC2650Driver.OPTICAL_NOTIFICATION_PERIOD_DEFAULT_MILLIS);
	}

	public boolean getGyroscope() {
		return getConfig(GYROSCOPE_KEY, false);
	}

	public boolean getAccelerometer() {
		return getConfig(ACCELEROMETER_KEY, false);
	}

	public boolean getMagnetometer() {
		return getConfig(MAGNETOMETER_KEY, false);
	}

	public boolean getMovementNotify() {
		return getConfig(MOVEMENT_NOTIFY_KEY, false);
	}

	public int getMovementNotificationPeriod() {
		return getConfig(MOVEMENT_NOTIFICATION_PERIOD_KEY, CC2650Driver.MOVEMENT_NOTIFICATION_PERIOD_DEFAULT_MILLIS);
	}

	public boolean getWakeOnMotion() {
		return getConfig(WAKE_ON_MOTION_KEY, false);
	}

	public int getAccelerometerRange() {
		return getConfig(ACCELEROMETER_RANGE_KEY, 2);
	}

	public List<String> getDevices() {
		return devices;
	}
}
