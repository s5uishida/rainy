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

	private static final int DETECTION_RANGE_2000 = 2000;
	private static final int DETECTION_RANGE_5000 = 5000;

	private final String clientID;
	private final List<MHZ19BDriver> mhz19bList = new ArrayList<MHZ19BDriver>();
	private final List<IDataSender> senders = new ArrayList<IDataSender>();
	private final String crontab = config.getReadCrontab();
	private final Scheduler mhz19bReadScheduler = new Scheduler();

	public MHZ19B(String clientID) throws IOException {
		this.clientID = clientID;

		if (config.getInfluxDB()) {
			senders.add(new MHZ19BInfluxDBSender());
			LOG.info("registered sender - {}", MHZ19BInfluxDBSender.class.getSimpleName());
		}
		if (config.getMqtt()) {
			senders.add(new MHZ19BMqttSender());
			LOG.info("registered sender - {}", MHZ19BMqttSender.class.getSimpleName());
		}

		for (String portName : config.getPortName()) {
			MHZ19BDriver mhz19b = MHZ19BDriver.getInstance(portName);
			mhz19bList.add(mhz19b);
		}

		mhz19bReadScheduler.schedule(crontab, new MHZ19BReadScheduledTask(mhz19bList, senders, this.clientID));
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
		for (MHZ19BDriver mhz19b : mhz19bList) {
			try {
				mhz19b.open();

				int range = config.getDetectionRange();
				if (range == DETECTION_RANGE_2000) {
					mhz19b.setDetectionRange2000();
				} else if (range == DETECTION_RANGE_5000) {
					mhz19b.setDetectionRange5000();
				}

				if (config.getZeroCalibration()) {
					mhz19b.setCalibrateZeroPoint();
				}

				mhz19b.setAutoCalibration(config.getAutoCalibration());
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		mhz19bReadScheduler.start();
		LOG.info("sensing MH-Z19B started.");
	}

	public void stop() {
		mhz19bReadScheduler.stop();
		for (MHZ19BDriver mhz19b : mhz19bList) {
			try {
				mhz19b.close();
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
		LOG.info("sensing MH-Z19B stopped.");
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

	private final List<MHZ19BDriver> mhz19bList;
	private final List<IDataSender> senders;
	private final String clientID;

	public MHZ19BReadScheduledTask(List<MHZ19BDriver> mhz19bList, List<IDataSender> senders, String clientID) {
		this.mhz19bList = mhz19bList;
		this.senders = senders;
		this.clientID = clientID;
	}

	@Override
	public void run() {
		for (MHZ19BDriver mhz19b : mhz19bList) {
			if (!mhz19b.isOpened() ) {
				continue;
			}

			Date date = new Date();
			String dateString = sdf.format(date);

			MHZ19BData mhz19bData = new MHZ19BData();

			mhz19bData.clientID = clientID;
			mhz19bData.deviceID = mhz19b.getPortName();
			mhz19bData.samplingDate = dateString;
			mhz19bData.samplingTimeMillis = date.getTime();

			try {
				int value = mhz19b.getGasConcentration();

				mhz19bData.co2GasConcentration = new Co2GasConcentration();
				mhz19bData.co2GasConcentration.value = value;

				LOG.debug(mhz19b.getLogPrefix() + "co2:{}", value);

				for (IDataSender sender : senders) {
					try {
						if (sender.isConnected()) {
							sender.send(mhz19bData);
						}
					} catch (IOException e) {
						LOG.warn(mhz19b.getLogPrefix() + "{} caught - {}", sender.getClass().getSimpleName(), e.toString());
					}
				}
			} catch (IOException e) {
				LOG.warn(mhz19b.getLogPrefix() + "caught - {}", e.toString());
			}
		}
	}
}
