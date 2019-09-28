package io.github.s5uishida.iot.rainy.device.hcsr501;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.Pin;

import io.github.s5uishida.iot.device.hcsr501.driver.HCSR501Driver;
import io.github.s5uishida.iot.device.hcsr501.driver.IHCSR501Handler;
import io.github.s5uishida.iot.rainy.device.IDevice;
import io.github.s5uishida.iot.rainy.device.hcsr501.data.Detection;
import io.github.s5uishida.iot.rainy.device.hcsr501.data.HCSR501Data;
import io.github.s5uishida.iot.rainy.sender.IDataSender;

/*
 * @author s5uishida
 *
 */
public class HCSR501 implements IDevice {
	private static final Logger LOG = LoggerFactory.getLogger(HCSR501.class);
	private static final HCSR501Config config = HCSR501Config.getInstance();

	private final String clientID;
	private final List<HCSR501Driver> hcsr501List = new ArrayList<HCSR501Driver>();
	private final List<IDataSender> senders = new ArrayList<IDataSender>();

	public HCSR501(String clientID) throws IOException {
		this.clientID = clientID;

		if (config.getInfluxDB()) {
			senders.add(new HCSR501InfluxDBSender());
			LOG.info("registered sender - {}", HCSR501InfluxDBSender.class.getSimpleName());
		}
		if (config.getMqtt()) {
			senders.add(new HCSR501MqttSender());
			LOG.info("registered sender - {}", HCSR501MqttSender.class.getSimpleName());
		}

		for (Pin gpioPin : config.getGpioPin()) {
			HCSR501Driver hcsr501 = HCSR501Driver.getInstance(gpioPin, new HCSR501Handler(senders, this.clientID));
			hcsr501List.add(hcsr501);
		}
	}

	public void start() {
		for (IDataSender sender : senders) {
			try {
				sender.connect();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		for (HCSR501Driver hcsr501 : hcsr501List) {
			hcsr501.open();
		}
		LOG.info("sensing HC-SR501 started.");
	}

	public void stop() {
		for (HCSR501Driver hcsr501 : hcsr501List) {
			hcsr501.close();
		}
		for (IDataSender sender : senders) {
			try {
				sender.disconnect();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		LOG.info("sensing HC-SR501 stopped.");
	}

	public static void main(String[] args) throws IOException {
		HCSR501 hcsr501 = new HCSR501("client0");
		hcsr501.start();

//		hcsr501.stop();
	}
}

class HCSR501Handler implements IHCSR501Handler {
	private static final Logger LOG = LoggerFactory.getLogger(HCSR501Handler.class);

	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

	private final List<IDataSender> senders;
	private final String clientID;

	public HCSR501Handler(List<IDataSender> senders, String clientID) {
		this.senders = senders;
		this.clientID = clientID;
	}

	@Override
	public void handle(String pinName, boolean detect, Date date) {
		HCSR501Data hcsr501Data = new HCSR501Data();

		hcsr501Data.clientID = clientID;
		hcsr501Data.deviceID = pinName;
		hcsr501Data.samplingDate = sdf.format(date);
		hcsr501Data.samplingTimeMillis = date.getTime();

		hcsr501Data.detection = new Detection();
		hcsr501Data.detection.value = detect ? 1 : 0;

		String logPrefix = "[" + pinName + "] ";

		LOG.debug(logPrefix + "detect:{}", detect);

		for (IDataSender sender : senders) {
			try {
				if (sender.isConnected()) {
					sender.send(hcsr501Data);
				}
			} catch (IOException e) {
				LOG.warn(logPrefix + "{} caught - {}", sender.getClass().getSimpleName(), e.toString());
			}
		}
	}
}
