package io.github.s5uishida.iot.rainy.device.bh1750fvi;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.s5uishida.iot.device.bh1750fvi.driver.BH1750FVIDriver;
import io.github.s5uishida.iot.rainy.device.IDevice;
import io.github.s5uishida.iot.rainy.device.bh1750fvi.data.BH1750FVIData;
import io.github.s5uishida.iot.rainy.device.bh1750fvi.data.Optical;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import it.sauronsoftware.cron4j.Scheduler;

/*
 * @author s5uishida
 *
 */
public class BH1750FVI implements IDevice {
	private static final Logger LOG = LoggerFactory.getLogger(BH1750FVI.class);
	private static final BH1750FVIConfig config = BH1750FVIConfig.getInstance();

	private final String clientID;
	private final List<BH1750FVIDriver> bh1750fviList = new ArrayList<BH1750FVIDriver>();
	private final List<IDataSender> senders = new ArrayList<IDataSender>();
	private final String crontab = config.getReadCrontab();
	private final Scheduler bh1750fviReadScheduler = new Scheduler();

	public BH1750FVI(String clientID) throws IOException {
		this.clientID = clientID;

		if (config.getInfluxDB()) {
			senders.add(new BH1750FVIInfluxDBSender());
			LOG.info("registered sender - {}", BH1750FVIInfluxDBSender.class.getSimpleName());
		}
		if (config.getMqtt()) {
			senders.add(new BH1750FVIMqttSender());
			LOG.info("registered sender - {}", BH1750FVIMqttSender.class.getSimpleName());
		}

		for (I2cBusAddress i2cBusAddress : config.getI2cBusAddress()) {
			BH1750FVIDriver bh1750fvi = BH1750FVIDriver.getInstance(i2cBusAddress.bus, i2cBusAddress.address);
			bh1750fviList.add(bh1750fvi);
		}

		bh1750fviReadScheduler.schedule(crontab, new BH1750FVIReadScheduledTask(bh1750fviList, senders, this.clientID));
		bh1750fviReadScheduler.setDaemon(true);
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
		for (BH1750FVIDriver bh1750fvi : bh1750fviList) {
			try {
				bh1750fvi.open();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		bh1750fviReadScheduler.start();
		LOG.info("sensing BH1750FVI started.");
	}

	public void stop() {
		bh1750fviReadScheduler.stop();
		for (BH1750FVIDriver bh1750fvi : bh1750fviList) {
			try {
				bh1750fvi.close();
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
		LOG.info("sensing BH1750FVI stopped.");
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		BH1750FVI bh1750fvi = new BH1750FVI("client0");
		bh1750fvi.start();

//		bh1750fvi.stop();
	}
}

class BH1750FVIReadScheduledTask implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(BH1750FVIReadScheduledTask.class);

	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

	private final List<BH1750FVIDriver> bh1750fviList;
	private final List<IDataSender> senders;
	private final String clientID;

	public BH1750FVIReadScheduledTask(List<BH1750FVIDriver> bh1750fviList, List<IDataSender> senders, String clientID) {
		this.bh1750fviList = bh1750fviList;
		this.senders = senders;
		this.clientID = clientID;
	}

	@Override
	public void run() {
		for (BH1750FVIDriver bh1750fvi : bh1750fviList) {
			Date date = new Date();
			String dateString = sdf.format(date);

			BH1750FVIData bh1750fviData = new BH1750FVIData();

			bh1750fviData.clientID = clientID;
			bh1750fviData.deviceID = bh1750fvi.getName();
			bh1750fviData.samplingDate = dateString;
			bh1750fviData.samplingTimeMillis = date.getTime();

			try {
				float value = bh1750fvi.getOptical();

				bh1750fviData.optical = new Optical();
				bh1750fviData.optical.value = value;

				LOG.debug(bh1750fvi.getLogPrefix() + "optical:{}", value);

				for (IDataSender sender : senders) {
					try {
						if (sender.isConnected()) {
							sender.send(bh1750fviData);
						}
					} catch (IOException e) {
						LOG.warn(bh1750fvi.getLogPrefix() + "{} caught - {}", sender.getClass().getSimpleName(), e.toString());
					}
				}
			} catch (IOException e) {
				LOG.warn(bh1750fvi.getLogPrefix() + "caught - {}", e.toString());
			}
		}
	}
}
