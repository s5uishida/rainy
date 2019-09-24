package io.github.s5uishida.iot.rainy.device.ppd42ns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import io.github.s5uishida.iot.device.ppd42ns.driver.PPD42NSDriver;
import io.github.s5uishida.iot.rainy.util.AbstractConfig;
import io.github.s5uishida.iot.rainy.util.ConfigParams;

/*
 * @author s5uishida
 *
 */
public class PPD42NSConfig extends AbstractConfig {
	private static final Logger LOG = LoggerFactory.getLogger(PPD42NSConfig.class);

	public static final String GPIO_PIN_KEY								= "gpioPin";
	public static final String INFLUXDB_KEY								= "influxDB";
	public static final String MQTT_KEY									= "mqtt";
	public static final String PRETTY_PRINTING_KEY						= "prettyPrinting";
	public static final String READ_CRONTAB_KEY							= "readCrontab";

	private static PPD42NSConfig config;
	private static List<Pin> gpioPinList = new ArrayList<Pin>();

	private PPD42NSConfig(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static PPD42NSConfig getInstance() {
		if (config == null) {
			config = new PPD42NSConfig(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.PPD42NS_CONFIG_FILE);
			List<String> gpioPins = Arrays.asList(config.getConfig(GPIO_PIN_KEY, PPD42NSDriver.getName(RaspiPin.GPIO_10)).split("\\s+"));
			for (String gpioPin : gpioPins) {
				if (gpioPin.equals(PPD42NSDriver.getName(RaspiPin.GPIO_10))) {
					if (!gpioPinList.contains(RaspiPin.GPIO_10)) {
						gpioPinList.add(RaspiPin.GPIO_10);
					}
				} else if (gpioPin.equals(PPD42NSDriver.getName(RaspiPin.GPIO_20))) {
					if (!gpioPinList.contains(RaspiPin.GPIO_20)) {
						gpioPinList.add(RaspiPin.GPIO_20);
					}
				} else if (gpioPin.equals(PPD42NSDriver.getName(RaspiPin.GPIO_14))) {
					if (!gpioPinList.contains(RaspiPin.GPIO_14)) {
						gpioPinList.add(RaspiPin.GPIO_14);
					}
				} else {
					LOG.warn("The set " + gpioPin + " is not " +
							PPD42NSDriver.getName(RaspiPin.GPIO_10) + ", " +
							PPD42NSDriver.getName(RaspiPin.GPIO_20) + " or " +
							PPD42NSDriver.getName(RaspiPin.GPIO_14) + ".");
				}
			}
		}
		return config;
	}

	public List<Pin> getGpioPin() throws IOException {
		return gpioPinList;
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
