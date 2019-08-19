package io.github.s5uishida.iot.rainy.device.mhz19b;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.s5uishida.iot.device.mhz19b.driver.MHZ19BDriver;
import io.github.s5uishida.iot.rainy.device.IDevice;
import io.github.s5uishida.iot.rainy.device.mhz19b.data.Co2GasConcentration;
import io.github.s5uishida.iot.rainy.device.mhz19b.data.MHZ19BData;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import it.sauronsoftware.cron4j.Scheduler;

/*
 * @author s5uishida
 *
 */
public class MHZ19B implements IDevice {
	private static final Logger LOG = LoggerFactory.getLogger(MHZ19B.class);
	private static final MHZ19BConfig config = MHZ19BConfig.getInstance();

	private final String clientID;
	private final MHZ19BDriver mhz19b;
	private final List<IDataSender> senders = new ArrayList<IDataSender>();
	private final String crontab;

	private Scheduler mhz19bReadScheduler;

	public MHZ19B(String clientID) throws IOException, InterruptedException {
		this.clientID = clientID;

		mhz19b = MHZ19BDriver.getInstance(config.getPortName());
		mhz19b.open();
		mhz19b.setDetectionRange5000();

		if (config.getInfluxDB()) {
			senders.add(new MHZ19BInfluxDBSender());
			LOG.info("registered sender - {}", MHZ19BInfluxDBSender.class.getSimpleName());
		}
		if (config.getMqtt()) {
			senders.add(new MHZ19BMqttSender());
			LOG.info("registered sender - {}", MHZ19BMqttSender.class.getSimpleName());
		}

		mhz19bReadScheduler = new Scheduler();
		crontab = config.getReadCrontab();
		mhz19bReadScheduler.schedule(crontab, new MHZ19BReadScheduledTask(mhz19b, senders, this.clientID));
		mhz19bReadScheduler.setDaemon(true);
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
		mhz19bReadScheduler.start();
		LOG.info("sensing MH-Z19B started.");
	}

	public void stop() {
		mhz19bReadScheduler.stop();
		for (IDataSender sender : senders) {
			try {
				sender.disconnect();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		try {
			mhz19b.close();
			LOG.info("sensing MH-Z19B stopped.");
		} catch (IOException e) {
			LOG.warn("caught - {}", e.toString());
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		MHZ19B mhz19b = new MHZ19B("client0");
		mhz19b.start();

//		mhz19b.stop();
	}
}

class MHZ19BReadScheduledTask implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(MHZ19BReadScheduledTask.class);

	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

	private final MHZ19BDriver mhz19b;
	private final List<IDataSender> senders;
	private final String clientID;

	public MHZ19BReadScheduledTask(MHZ19BDriver mhz19b, List<IDataSender> senders, String clientID) {
		this.mhz19b = mhz19b;
		this.senders = senders;
		this.clientID = clientID;
	}

	@Override
	public void run() {
		Date date = new Date();
		String dateString = sdf.format(date);

		String logPrefix = "[" + mhz19b.getPortName() + "] ";

		MHZ19BData mhz19bData = new MHZ19BData();

		mhz19bData.clientID = clientID;
		mhz19bData.deviceID = mhz19b.getPortName();
		mhz19bData.samplingDate = dateString;
		mhz19bData.samplingTimeMillis = date.getTime();

		try {
			int value = mhz19b.getGasConcentration();

			mhz19bData.co2GasConcentration = new Co2GasConcentration();
			mhz19bData.co2GasConcentration.value = value;

			LOG.debug(logPrefix + "co2:{}", value);

			for (IDataSender sender : senders) {
				try {
					if (sender.isConnected()) {
						sender.send(mhz19bData);
					}
				} catch (IOException e) {
					LOG.warn(logPrefix + "{} caught - {}", sender.getClass().getSimpleName(), e.toString());
				}
			}
		} catch (IOException e) {
			LOG.warn(logPrefix + "caught - {}", e.toString());
		}
	}
}
