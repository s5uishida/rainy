package io.github.s5uishida.iot.rainy.device.bme280;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CBus;

import io.github.s5uishida.iot.device.bme280.driver.BME280Driver;
import io.github.s5uishida.iot.rainy.device.I2cBusAddress;
import io.github.s5uishida.iot.rainy.util.AbstractConfig;
import io.github.s5uishida.iot.rainy.util.ConfigParams;

/*
 * @author s5uishida
 *
 */
public class BME280Config extends AbstractConfig {
	private static final Logger LOG = LoggerFactory.getLogger(BME280Config.class);

	public static final String I2C_BUS_ADDRESS_KEY						= "i2cBusAddress";
	public static final String INFLUXDB_KEY								= "influxDB";
	public static final String MQTT_KEY									= "mqtt";
	public static final String PRETTY_PRINTING_KEY						= "prettyPrinting";
	public static final String READ_CRONTAB_KEY							= "readCrontab";

	private static BME280Config config;
	private static List<I2cBusAddress> i2cBusAddressList = new ArrayList<I2cBusAddress>();

	private BME280Config(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static BME280Config getInstance() {
		if (config == null) {
			config = new BME280Config(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.BME280_CONFIG_FILE);
			List<String> i2cBusAddresses = Arrays.asList(config.getConfig(I2C_BUS_ADDRESS_KEY, "1:76").split("\\s+"));
			for (String i2cBusAddress : i2cBusAddresses) {
				String[] items = i2cBusAddress.split(":");
				if (items.length < 2 || items.length > 2) {
					LOG.warn("Illegal format:{}", i2cBusAddress);
					continue;
				}
				try {
					int i2cBusNumber = Integer.valueOf(items[0]);
					byte i2cAddress = Byte.valueOf(items[1], 16);

					if (i2cBusNumber != I2CBus.BUS_0 && i2cBusNumber != I2CBus.BUS_1) {
						LOG.warn("The set " + i2cBusNumber + " is not " + I2CBus.BUS_0 + " or " + I2CBus.BUS_1 + ".");
						continue;
					}
					if (i2cAddress != BME280Driver.I2C_ADDRESS_76 && i2cAddress != BME280Driver.I2C_ADDRESS_77) {
						LOG.warn("The set " + String.format("%x", i2cAddress) + " is not " +
								String.format("%x", BME280Driver.I2C_ADDRESS_76) + " or " + String.format("%x", BME280Driver.I2C_ADDRESS_77) + ".");
						continue;
					}

					I2cBusAddress entry = new I2cBusAddress(i2cBusNumber, i2cAddress);
					if (!i2cBusAddressList.contains(entry)) {
						i2cBusAddressList.add(entry);
					}
				} catch (NumberFormatException e) {
					LOG.warn("caught - {} - Illegal format:{}", e.toString(), i2cBusAddress);
				}
			}
		}
		return config;
	}

	public List<I2cBusAddress> getI2cBusAddress() {
		return i2cBusAddressList;
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
