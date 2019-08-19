package io.github.s5uishida.iot.rainy.device.ppd42ns;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.s5uishida.iot.device.ppd42ns.driver.PPD42NSDriver;
import io.github.s5uishida.iot.device.ppd42ns.driver.PPD42NSObservationData;
import io.github.s5uishida.iot.rainy.device.IDevice;
import io.github.s5uishida.iot.rainy.device.ppd42ns.data.PPD42NSData;
import io.github.s5uishida.iot.rainy.device.ppd42ns.data.Pcs;
import io.github.s5uishida.iot.rainy.device.ppd42ns.data.Ugm3;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import it.sauronsoftware.cron4j.Scheduler;

/*
 * @author s5uishida
 *
 */
public class PPD42NS implements IDevice {
	private static final Logger LOG = LoggerFactory.getLogger(PPD42NS.class);
	private static final PPD42NSConfig config = PPD42NSConfig.getInstance();

	private final String clientID;
	private final PPD42NSDriver ppd42ns;
	private final List<IDataSender> senders = new ArrayList<IDataSender>();
	private final String crontab;

	private Scheduler ppd42nsReadScheduler;

	public PPD42NS(String clientID) throws IOException {
		this.clientID = clientID;

		ppd42ns = PPD42NSDriver.getInstance();
		ppd42ns.open();

		if (config.getInfluxDB()) {
			senders.add(new PPD42NSInfluxDBSender());
			LOG.info("registered sender - {}", PPD42NSInfluxDBSender.class.getSimpleName());
		}
		if (config.getMqtt()) {
			senders.add(new PPD42NSMqttSender());
			LOG.info("registered sender - {}", PPD42NSMqttSender.class.getSimpleName());
		}

		ppd42nsReadScheduler = new Scheduler();
		crontab = config.getReadCrontab();
		ppd42nsReadScheduler.schedule(crontab, new PPD42NSReadScheduledTask(ppd42ns, senders, this.clientID));
		ppd42nsReadScheduler.setDaemon(true);
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
		ppd42nsReadScheduler.start();
		LOG.info("sensing PPD42NS started.");
	}

	public void stop() {
		ppd42nsReadScheduler.stop();
		for (IDataSender sender : senders) {
			try {
				sender.disconnect();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		ppd42ns.close();
		LOG.info("sensing PPD42NS stopped.");
	}

	public static void main(String[] args) throws IOException {
		PPD42NS ppd42ns = new PPD42NS("client0");
		ppd42ns.start();

//		ppd42ns.stop();
	}
}

class PPD42NSReadScheduledTask implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(PPD42NSReadScheduledTask.class);

	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

	private final PPD42NSDriver ppd42ns;
	private final List<IDataSender> senders;
	private final String clientID;

	public PPD42NSReadScheduledTask(PPD42NSDriver ppd42ns, List<IDataSender> senders, String clientID) {
		this.ppd42ns = ppd42ns;
		this.senders = senders;
		this.clientID = clientID;
	}

	@Override
	public void run() {
		Date date = new Date();
		String dateString = sdf.format(date);

		String logPrefix = "[" + ppd42ns.getName() + "] ";

		PPD42NSData ppd42nsData = new PPD42NSData();

		ppd42nsData.clientID = clientID;
		ppd42nsData.deviceID = ppd42ns.getName();
		ppd42nsData.samplingDate = dateString;
		ppd42nsData.samplingTimeMillis = date.getTime();

		PPD42NSObservationData data = ppd42ns.read();
		if (data == null) {
			LOG.info(logPrefix + "read timeout.");
			return;
		}

		ppd42nsData.pcs = new Pcs();
		ppd42nsData.pcs.value = data.getPcs();

		ppd42nsData.ugm3 = new Ugm3();
		ppd42nsData.ugm3.value = data.getUgm3();

		LOG.debug(logPrefix + "pcs:{} ugm3:{}", data.getPcs(), data.getUgm3());

		for (IDataSender sender : senders) {
			try {
				if (sender.isConnected()) {
					sender.send(ppd42nsData);
				}
			} catch (IOException e) {
				LOG.warn(logPrefix + "{} caught - {}", sender.getClass().getSimpleName(), e.toString());
			}
		}
	}
}
