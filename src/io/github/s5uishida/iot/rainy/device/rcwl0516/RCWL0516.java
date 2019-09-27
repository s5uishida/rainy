package io.github.s5uishida.iot.rainy.device.rcwl0516;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.Pin;

import io.github.s5uishida.iot.device.rcwl0516.driver.IRCWL0516Handler;
import io.github.s5uishida.iot.device.rcwl0516.driver.RCWL0516Driver;
import io.github.s5uishida.iot.rainy.device.IDevice;
import io.github.s5uishida.iot.rainy.device.rcwl0516.data.Detection;
import io.github.s5uishida.iot.rainy.device.rcwl0516.data.RCWL0516Data;
import io.github.s5uishida.iot.rainy.sender.IDataSender;

/*
 * @author s5uishida
 *
 */
public class RCWL0516 implements IDevice {
	private static final Logger LOG = LoggerFactory.getLogger(RCWL0516.class);
	private static final RCWL0516Config config = RCWL0516Config.getInstance();

	private final String clientID;
	private final List<RCWL0516Driver> rcwl0516List = new ArrayList<RCWL0516Driver>();
	private final List<IDataSender> senders = new ArrayList<IDataSender>();

	public RCWL0516(String clientID) throws IOException {
		this.clientID = clientID;

		if (config.getInfluxDB()) {
			senders.add(new RCWL0516InfluxDBSender());
			LOG.info("registered sender - {}", RCWL0516InfluxDBSender.class.getSimpleName());
		}
		if (config.getMqtt()) {
			senders.add(new RCWL0516MqttSender());
			LOG.info("registered sender - {}", RCWL0516MqttSender.class.getSimpleName());
		}

		for (Pin gpioPin : config.getGpioPin()) {
			RCWL0516Driver rcwl0516 = RCWL0516Driver.getInstance(gpioPin, new RCWL0516Handler(senders, this.clientID));
			rcwl0516List.add(rcwl0516);
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
		for (RCWL0516Driver rcwl0516 : rcwl0516List) {
			rcwl0516.open();
		}
		LOG.info("sensing RCWL-0516 started.");
	}

	public void stop() {
		for (RCWL0516Driver rcwl0516 : rcwl0516List) {
			rcwl0516.close();
		}
		for (IDataSender sender : senders) {
			try {
				sender.disconnect();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		LOG.info("sensing RCWL-0516 stopped.");
	}

	public static void main(String[] args) throws IOException {
		RCWL0516 rcwl0516 = new RCWL0516("client0");
		rcwl0516.start();

//		rcwl0516.stop();
	}
}

class RCWL0516Handler implements IRCWL0516Handler {
	private static final Logger LOG = LoggerFactory.getLogger(RCWL0516Handler.class);

	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

	private final List<IDataSender> senders;
	private final String clientID;

	public RCWL0516Handler(List<IDataSender> senders, String clientID) {
		this.senders = senders;
		this.clientID = clientID;
	}

	@Override
	public void handle(String pinName, boolean detect, Date date) {
		RCWL0516Data rcwl0516Data = new RCWL0516Data();

		rcwl0516Data.clientID = clientID;
		rcwl0516Data.deviceID = pinName;
		rcwl0516Data.samplingDate = sdf.format(date);
		rcwl0516Data.samplingTimeMillis = date.getTime();

		rcwl0516Data.detection = new Detection();
		rcwl0516Data.detection.value = detect;

		String logPrefix = "[" + pinName + "] ";

		LOG.debug(logPrefix + "detect:{}", detect);

		for (IDataSender sender : senders) {
			try {
				if (sender.isConnected()) {
					sender.send(rcwl0516Data);
				}
			} catch (IOException e) {
				LOG.warn(logPrefix + "{} caught - {}", sender.getClass().getSimpleName(), e.toString());
			}
		}
	}
}
