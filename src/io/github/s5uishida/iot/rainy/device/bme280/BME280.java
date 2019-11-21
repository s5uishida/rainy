package io.github.s5uishida.iot.rainy.device.bme280;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.s5uishida.iot.device.bme280.driver.BME280Driver;
import io.github.s5uishida.iot.rainy.device.I2cBusAddress;
import io.github.s5uishida.iot.rainy.device.IDevice;
import io.github.s5uishida.iot.rainy.device.bme280.data.BME280Data;
import io.github.s5uishida.iot.rainy.device.bme280.data.Humidity;
import io.github.s5uishida.iot.rainy.device.bme280.data.Pressure;
import io.github.s5uishida.iot.rainy.device.bme280.data.Temperature;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import it.sauronsoftware.cron4j.Scheduler;

/*
 * @author s5uishida
 *
 */
public class BME280 implements IDevice {
	private static final Logger LOG = LoggerFactory.getLogger(BME280.class);
	private static final BME280Config config = BME280Config.getInstance();

	private final String clientID;
	private final List<BME280Driver> bme280List = new ArrayList<BME280Driver>();
	private final List<IDataSender> senders = new ArrayList<IDataSender>();
	private final String crontab = config.getReadCrontab();
	private final Scheduler bme280ReadScheduler = new Scheduler();

	public BME280(String clientID) throws IOException {
		this.clientID = clientID;

		if (config.getInfluxDB()) {
			senders.add(new BME280InfluxDBSender());
			LOG.info("registered sender - {}", BME280InfluxDBSender.class.getSimpleName());
		}
		if (config.getMqtt()) {
			senders.add(new BME280MqttSender());
			LOG.info("registered sender - {}", BME280MqttSender.class.getSimpleName());
		}

		for (I2cBusAddress i2cBusAddress : config.getI2cBusAddress()) {
			BME280Driver bme280 = BME280Driver.getInstance(i2cBusAddress.getBus(), i2cBusAddress.getAddress());
			bme280List.add(bme280);
		}

		bme280ReadScheduler.schedule(crontab, new BME280ReadScheduledTask(bme280List, senders, this.clientID));
		bme280ReadScheduler.setDaemon(true);
		LOG.info("crontab of direct reading - {}", crontab);
	}

	public String getCrontab() {
		return crontab;
	}

	public void start() {
		for (IDataSender sender : senders) {
			try {
				sender.connect();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		for (BME280Driver bme280 : bme280List) {
			try {
				bme280.open();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		bme280ReadScheduler.start();
		LOG.info("sensing BME280 started.");
	}

	public void stop() {
		bme280ReadScheduler.stop();
		for (BME280Driver bme280 : bme280List) {
			try {
				bme280.close();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		for (IDataSender sender : senders) {
			try {
				sender.disconnect();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		LOG.info("sensing BME280 stopped.");
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		BME280 bme280 = new BME280("client0");
		bme280.start();

//		bme280.stop();
	}
}

class BME280ReadScheduledTask implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(BME280ReadScheduledTask.class);

	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

	private final List<BME280Driver> bme280List;
	private final List<IDataSender> senders;
	private final String clientID;

	public BME280ReadScheduledTask(List<BME280Driver> bme280List, List<IDataSender> senders, String clientID) {
		this.bme280List = bme280List;
		this.senders = senders;
		this.clientID = clientID;
	}

	@Override
	public void run() {
		for (BME280Driver bme280 : bme280List) {
			Date date = new Date();
			String dateString = sdf.format(date);

			BME280Data bme280Data = new BME280Data();

			bme280Data.clientID = clientID;
			bme280Data.deviceID = bme280.getName();
			bme280Data.samplingDate = dateString;
			bme280Data.samplingTimeMillis = date.getTime();

			try {
				float[] values = bme280.getSensorValues();

				bme280Data.temperature = new Temperature();
				bme280Data.temperature.value = values[0];

				bme280Data.humidity = new Humidity();
				bme280Data.humidity.value = values[1];

				bme280Data.pressure = new Pressure();
				bme280Data.pressure.value = values[2];

				LOG.debug(bme280.getLogPrefix() + "temperature:{}", values[0]);
				LOG.debug(bme280.getLogPrefix() + "humidity:{}", values[1]);
				LOG.debug(bme280.getLogPrefix() + "pressure:{}", values[2]);

				for (IDataSender sender : senders) {
					try {
						if (sender.isConnected()) {
							sender.send(bme280Data);
						}
					} catch (IOException e) {
						LOG.warn(bme280.getLogPrefix() + "{} caught - {}", sender.getClass().getSimpleName(), e.toString());
					}
				}
			} catch (IOException e) {
				LOG.warn(bme280.getLogPrefix() + "caught - {}", e.toString());
			}
		}
	}
}
