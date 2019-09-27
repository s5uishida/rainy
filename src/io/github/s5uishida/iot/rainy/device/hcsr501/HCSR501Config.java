package io.github.s5uishida.iot.rainy.device.hcsr501;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import io.github.s5uishida.iot.device.hcsr501.driver.HCSR501Driver;
import io.github.s5uishida.iot.rainy.util.AbstractConfig;
import io.github.s5uishida.iot.rainy.util.ConfigParams;

/*
 * @author s5uishida
 *
 */
public class HCSR501Config extends AbstractConfig {
	private static final Logger LOG = LoggerFactory.getLogger(HCSR501Config.class);

	public static final String GPIO_PIN_KEY								= "gpioPin";
	public static final String INFLUXDB_KEY								= "influxDB";
	public static final String MQTT_KEY									= "mqtt";
	public static final String PRETTY_PRINTING_KEY						= "prettyPrinting";

	private static HCSR501Config config;
	private static List<Pin> gpioPinList = new ArrayList<Pin>();

	private HCSR501Config(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static HCSR501Config getInstance() {
		if (config == null) {
			config = new HCSR501Config(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.HCSR501_CONFIG_FILE);
			List<String> gpioPins = Arrays.asList(config.getConfig(GPIO_PIN_KEY, HCSR501Driver.getName(RaspiPin.GPIO_12)).split("\\s+"));
			for (String gpioPin : gpioPins) {
				if (gpioPin.equals(HCSR501Driver.getName(RaspiPin.GPIO_18))) {
					if (!gpioPinList.contains(RaspiPin.GPIO_18)) {
						gpioPinList.add(RaspiPin.GPIO_18);
					}
				} else if (gpioPin.equals(HCSR501Driver.getName(RaspiPin.GPIO_19))) {
					if (!gpioPinList.contains(RaspiPin.GPIO_19)) {
						gpioPinList.add(RaspiPin.GPIO_19);
					}
				} else if (gpioPin.equals(HCSR501Driver.getName(RaspiPin.GPIO_12))) {
					if (!gpioPinList.contains(RaspiPin.GPIO_12)) {
						gpioPinList.add(RaspiPin.GPIO_12);
					}
				} else if (gpioPin.equals(HCSR501Driver.getName(RaspiPin.GPIO_13))) {
					if (!gpioPinList.contains(RaspiPin.GPIO_13)) {
						gpioPinList.add(RaspiPin.GPIO_13);
					}
				} else {
					LOG.warn("The set " + gpioPin + " is not " +
							HCSR501Driver.getName(RaspiPin.GPIO_18) + ", " +
							HCSR501Driver.getName(RaspiPin.GPIO_19) + ", " +
							HCSR501Driver.getName(RaspiPin.GPIO_12) + " or " +
							HCSR501Driver.getName(RaspiPin.GPIO_13) + ".");
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
}
