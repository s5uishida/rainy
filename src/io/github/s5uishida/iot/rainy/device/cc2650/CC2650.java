package io.github.s5uishida.iot.rainy.device.cc2650;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler;
import org.freedesktop.dbus.interfaces.Properties.PropertiesChanged;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hypfvieh.bluetooth.DeviceManager;
import com.github.hypfvieh.bluetooth.DiscoveryFilter;
import com.github.hypfvieh.bluetooth.DiscoveryTransport;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattCharacteristic;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattDescriptor;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattService;

import io.github.s5uishida.iot.bluetooth.scanner.IScanHandler;
import io.github.s5uishida.iot.bluetooth.scanner.ScanData;
import io.github.s5uishida.iot.bluetooth.scanner.ScanProcess;
import io.github.s5uishida.iot.device.tisensortag.cc2650.driver.CC2650Driver;
import io.github.s5uishida.iot.rainy.device.IDevice;
import io.github.s5uishida.iot.rainy.device.cc2650.data.Accelerometer;
import io.github.s5uishida.iot.rainy.device.cc2650.data.AmbientTemperature;
import io.github.s5uishida.iot.rainy.device.cc2650.data.BatteryLevel;
import io.github.s5uishida.iot.rainy.device.cc2650.data.CC2650Data;
import io.github.s5uishida.iot.rainy.device.cc2650.data.Gyroscope;
import io.github.s5uishida.iot.rainy.device.cc2650.data.Humidity;
import io.github.s5uishida.iot.rainy.device.cc2650.data.Magnetometer;
import io.github.s5uishida.iot.rainy.device.cc2650.data.ObjectTemperature;
import io.github.s5uishida.iot.rainy.device.cc2650.data.Optical;
import io.github.s5uishida.iot.rainy.device.cc2650.data.Pressure;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import it.sauronsoftware.cron4j.Scheduler;

/*
 * @author s5uishida
 *
 */
public class CC2650 implements IDevice {
	private static final Logger LOG = LoggerFactory.getLogger(CC2650.class);
	private static final CC2650Config config = CC2650Config.getInstance();

	private final BlockingQueue<BluetoothDevice> deviceQueue = new LinkedBlockingQueue<BluetoothDevice>();
	private final ConcurrentMap<String, CC2650Driver> deviceMap = new ConcurrentHashMap<String, CC2650Driver>();

	private final String clientID;
	private final String adapterDeviceName;
	private final CC2650ScanHandler cc2650ScanHandler;
	private final ScanProcess scanProcess;
	private final DeviceManager manager;
	private final List<IDataSender> senders = new ArrayList<IDataSender>();
	private final CC2650SetupThread cc2650SetupThread;
	private final Scheduler cc2650ReadScheduler;
	private final String crontab;

	public CC2650(String clientID) throws IOException {
		this.clientID = clientID;
		this.adapterDeviceName = config.getBluetoothAdapter();

		Map<DiscoveryFilter, Object> filter = new HashMap<DiscoveryFilter, Object>();
		filter.put(DiscoveryFilter.Transport, DiscoveryTransport.LE);
		cc2650ScanHandler = new CC2650ScanHandler(deviceQueue);
		scanProcess = new ScanProcess(adapterDeviceName, cc2650ScanHandler, filter);

		manager = scanProcess.getDeviceManager();

		if (config.getInfluxDB()) {
			senders.add(new CC2650InfluxDBSender());
			LOG.info("registered sender - {}", CC2650InfluxDBSender.class.getSimpleName());
		}
		if (config.getMqtt()) {
			senders.add(new CC2650MqttSender());
			LOG.info("registered sender - {}", CC2650MqttSender.class.getSimpleName());
		}

		cc2650SetupThread = new CC2650SetupThread(deviceMap, deviceQueue);
		cc2650SetupThread.setDaemon(true);

		cc2650ReadScheduler = new Scheduler();
		crontab = config.getReadCrontab();
		cc2650ReadScheduler.schedule(crontab, new CC2650ReadScheduledTask(deviceMap, senders, this.clientID));
		cc2650ReadScheduler.setDaemon(true);
		LOG.info("crontab of direct reading - {}", crontab);

		try {
			manager.registerPropertyHandler(new CC2650PropertiesChangedHandler(deviceMap, senders, this.clientID));
		} catch (DBusException e) {
			throw new IOException(e);
		}
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
		cc2650SetupThread.start();
		cc2650ReadScheduler.start();
		scanProcess.start();
		LOG.info("sensing CC2650 started.");
	}

	public void stop() {
		cc2650ReadScheduler.stop();
		for (IDataSender sender : senders) {
			try {
				sender.disconnect();
			} catch (IOException e) {
				LOG.warn("caught - {}", e.toString());
			}
		}
		scanProcess.stop();
		cc2650ScanHandler.stop();
		deviceQueue.clear();
		cc2650SetupThread.stopThread();
		Iterator<Entry<String, CC2650Driver>> it = deviceMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, CC2650Driver> entry = it.next();
			CC2650Driver cc2650 = entry.getValue();
			try {
				cc2650.disconnect();
			} catch (DBusExecutionException e) {
				String logPrefix = "[" + cc2650.getAdapterDeviceName() + "] " + cc2650.getAddress() + " ";
				LOG.warn(logPrefix + "caught - {}", e.toString());
			}
		}
		LOG.info("sensing CC2650 stopped.");
	}

	public static void main(String[] args) throws IOException {
		CC2650 cc2650 = new CC2650("client0");
		cc2650.start();

//		cc2650.stop();
	}
}

class CC2650ScanHandler implements IScanHandler {
	private static final Logger LOG = LoggerFactory.getLogger(CC2650ScanHandler.class);
	private static final CC2650Config config = CC2650Config.getInstance();

	private static final boolean selectForcibly = false;

	private final BlockingQueue<BluetoothDevice> deviceQueue;

	private boolean running = true;

	public CC2650ScanHandler(BlockingQueue<BluetoothDevice> deviceQueue) {
		this.deviceQueue = deviceQueue;
	}

	public void stop() {
		running = false;
	}

	@Override
	public void handle(BluetoothDevice device, ScanData data) {
		try {
			if (running && data.getName().equals(CC2650Driver.NAME)) {
				if (!selectForcibly && !config.selectableDevice(data.getAddress())) {
					return;
				}
				deviceQueue.offer(device);
				LOG.debug("offer - {}", device.toString());
			}
		} catch (NoSuchFieldException e) {
		}
	}
}

class CC2650SetupThread extends Thread {
	private static final Logger LOG = LoggerFactory.getLogger(CC2650SetupThread.class);
	private static final CC2650Config config = CC2650Config.getInstance();

	private final ConcurrentMap<String, CC2650Driver> deviceMap;
	private final BlockingQueue<BluetoothDevice> deviceQueue;

	private boolean running = true;

	public CC2650SetupThread(ConcurrentMap<String, CC2650Driver> deviceMap, BlockingQueue<BluetoothDevice> deviceQueue) {
		this.deviceMap = deviceMap;
		this.deviceQueue = deviceQueue;
	}

	public void stopThread() {
		running = false;
	}

	public void run() {
		while (running) {
			CC2650Driver cc2650 = null;
			String logPrefix = "";
			try {
				BluetoothDevice device = deviceQueue.take();

				String address = device.getAddress();
				if (!deviceMap.containsKey(address)) {
					deviceMap.put(address, new CC2650Driver(device));
				} else {
					if (deviceMap.get(address).isConnected()) {
						continue;
					}
					deviceMap.get(address).disconnect();
				}

				cc2650 = deviceMap.get(address);

				logPrefix = "[" + cc2650.getAdapterDeviceName() + "] " + cc2650.getAddress() + " ";

				if (!cc2650.connect()) {
					continue;
				}

				LOG.info(logPrefix + "firmware:{} ({})", cc2650.getFirmwareVersion(), cc2650.getFirmwareVersionFloat());
				if (cc2650.isEnableBatteryLevel()) {
					LOG.info(logPrefix + "battery:{}", cc2650.readBatteryLevel());
				}

				if (config.getTemperature()) {
					cc2650.enableTemperature();
				}
				if (config.getTemperatureNotify()) {
					cc2650.setTemperatureNotificationPeriod(config.getTemperatureNotificationPeriod());
					cc2650.enableTemperatureNotification();
				}

				if (config.getHumidity()) {
					cc2650.enableHumidity();
				}
				if (config.getHumidityNotify()) {
					cc2650.setHumidityNotificationPeriod(config.getHumidityNotificationPeriod());
					cc2650.enableHumidityNotification();
				}

				if (config.getPressure()) {
					cc2650.enablePressure();
				}
				if (config.getPressureNotify()) {
					cc2650.setPressureNotificationPeriod(config.getPressureNotificationPeriod());
					cc2650.enablePressureNotification();
				}

				if (config.getOptical()) {
					cc2650.enableOptical();
				}
				if (config.getOpticalNotify()) {
					cc2650.setOpticalNotificationPeriod(config.getOpticalNotificationPeriod());
					cc2650.enableOpticalNotification();
				}

				int movementConfig = 0x00000000;
				if (config.getGyroscope()) {
					movementConfig |= CC2650Driver.GYROSCOPE_X_ENABLE |
							CC2650Driver.GYROSCOPE_Y_ENABLE |
							CC2650Driver.GYROSCOPE_Z_ENABLE;
				}
				if (config.getAccelerometer()) {
					movementConfig |= CC2650Driver.ACCELEROMETER_X_ENABLE |
							CC2650Driver.ACCELEROMETER_Y_ENABLE |
							CC2650Driver.ACCELEROMETER_Z_ENABLE;
				}
				if (config.getMagnetometer()) {
					movementConfig |= CC2650Driver.MAGNETOMETER_ENABLE;
				}
				int accelerometerRange = config.getAccelerometerRange();
				if (accelerometerRange == 2) {
					movementConfig |= CC2650Driver.ACCELEROMETER_RANGE_2G;
				} else if (accelerometerRange == 4) {
					movementConfig |= CC2650Driver.ACCELEROMETER_RANGE_4G;
				} else if (accelerometerRange == 8) {
					movementConfig |= CC2650Driver.ACCELEROMETER_RANGE_8G;
				} else if (accelerometerRange == 18) {
					movementConfig |= CC2650Driver.ACCELEROMETER_RANGE_16G;
				} else {
					movementConfig |= CC2650Driver.ACCELEROMETER_RANGE_2G;
					LOG.debug(logPrefix + "set 2 because a value other than 2, 4, 8 or 16 was specified for accelerometerRange.");
				}
				if (config.getWakeOnMotion()) {
					movementConfig |= CC2650Driver.WAKE_ON_MOTION_ENABLE;
				}
				cc2650.enableMovement(movementConfig);
				if (config.getMovementNotify()) {
					cc2650.setMovementNotificationPeriod(config.getMovementNotificationPeriod());
					cc2650.enableMovementNotification();
				}
			} catch (InterruptedException e) {
				LOG.warn(logPrefix + "caught - {}", e.toString());
				break;
			} catch (IOException e) {
				LOG.warn(logPrefix + "caught - {}", e.toString());
			} catch (UnsupportedOperationException e) {
				LOG.warn(logPrefix + "caught - {}", e.toString());
			} catch (DBusExecutionException e) {
				LOG.warn(logPrefix + "caught - {}", e.toString());
				if (cc2650 != null) {
					try {
						cc2650.disconnect();
					} catch (DBusExecutionException e1) {
						LOG.warn(logPrefix + "caught - {}", e1.toString());
					}
				}
			}
		}
	}
}

class CC2650ReadScheduledTask implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(CC2650ReadScheduledTask.class);
	private static final CC2650Config config = CC2650Config.getInstance();

	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

	private final ConcurrentMap<String, CC2650Driver> deviceMap;
	private final List<IDataSender> senders;
	private final String clientID;

	public CC2650ReadScheduledTask(ConcurrentMap<String, CC2650Driver> deviceMap, List<IDataSender> senders, String clientID) {
		this.deviceMap = deviceMap;
		this.senders = senders;
		this.clientID = clientID;
	}

	@Override
	public void run() {
		Iterator<Entry<String, CC2650Driver>> it = deviceMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, CC2650Driver> entry = it.next();
			CC2650Driver cc2650 = entry.getValue();

			if (!cc2650.isConnected() || !cc2650.isReady()) {
				continue;
			}

			Date date = new Date();
			String dateString = sdf.format(date);

			String logPrefix = "[" + cc2650.getAdapterDeviceName() + "] " + cc2650.getAddress() + " ";

			CC2650Data cc2650Data = new CC2650Data();

			cc2650Data.clientID = clientID;
			cc2650Data.deviceID = cc2650.getAddress();
			cc2650Data.samplingDate = dateString;
			cc2650Data.samplingTimeMillis = date.getTime();

			try {
				cc2650Data.firmwareVersion = cc2650.getFirmwareVersion();
			} catch (UnsupportedOperationException e) {
				LOG.warn(logPrefix + "caught - {}", e.toString());
				continue;
			}

			boolean isData = false;

			try {
				if (config.getBatteryLevel() && cc2650.isEnableBatteryLevel()) {
					try {
						int batteryLevel = cc2650.readBatteryLevel();

						cc2650Data.batteryLevel = new BatteryLevel();
						cc2650Data.batteryLevel.value = batteryLevel;

						isData = true;

						LOG.debug(logPrefix + "battery:{}", batteryLevel);
					} catch (IOException e) {
						LOG.warn(logPrefix + "caught - {}", e.toString());
					}
				}

				if (cc2650.isEnableTemperature() && !cc2650.isNotifyingTemperature()) {
					try {
						float[] temp = cc2650.readTemperature();

						cc2650Data.objectTemperature = new ObjectTemperature();
						cc2650Data.objectTemperature.value = temp[0];

						cc2650Data.ambientTemperature = new AmbientTemperature();
						cc2650Data.ambientTemperature.value = temp[1];

						isData = true;

						LOG.debug(logPrefix + "obj:{} amb:{}", temp[0], temp[1]);
					} catch (IOException e) {
						LOG.warn(logPrefix + "caught - {}", e.toString());
					}
				}

				if (cc2650.isEnableHumidity() && !cc2650.isNotifyingHumidity()) {
					try {
						float humidity = cc2650.readHumidity();

						cc2650Data.humidity = new Humidity();
						cc2650Data.humidity.value = humidity;

						isData = true;

						LOG.debug(logPrefix + "humidity:{}", humidity);
					} catch (IOException e) {
						LOG.warn(logPrefix + "caught - {}", e.toString());
					}
				}

				if (cc2650.isEnablePressure() && !cc2650.isNotifyingPressure()) {
					try {
						float pressure = cc2650.readPressure();

						cc2650Data.pressure = new Pressure();
						cc2650Data.pressure.value = pressure;

						isData = true;

						LOG.debug(logPrefix + "pressure:{}", pressure);
					} catch (IOException e) {
						LOG.warn(logPrefix + "caught - {}", e.toString());
					}
				}

				if (cc2650.isEnableOptical() && !cc2650.isNotifyingOptical()) {
					try {
						float optical = cc2650.readOptical();

						cc2650Data.optical = new Optical();
						cc2650Data.optical.value = optical;

						isData = true;

						LOG.debug(logPrefix + "optical:{}", optical);
					} catch (IOException e) {
						LOG.warn(logPrefix + "caught - {}", e.toString());
					}
				}

				if (cc2650.isEnableMovement() && !cc2650.isNotifyingMovement()) {
					try {
						float[] mov = cc2650.readMovement();

						cc2650Data.gyroscope = new Gyroscope();
						cc2650Data.gyroscope.x = mov[0];
						cc2650Data.gyroscope.y = mov[1];
						cc2650Data.gyroscope.z = mov[2];

						cc2650Data.accelerometer = new Accelerometer();
						cc2650Data.accelerometer.x = mov[3];
						cc2650Data.accelerometer.y = mov[4];
						cc2650Data.accelerometer.z = mov[5];

						cc2650Data.magnetometer = new Magnetometer();
						cc2650Data.magnetometer.x = mov[6];
						cc2650Data.magnetometer.y = mov[7];
						cc2650Data.magnetometer.z = mov[8];

						isData = true;

						LOG.debug(logPrefix + "gyr[x]:{}", mov[0]);
						LOG.debug(logPrefix + "gyr[y]:{}", mov[1]);
						LOG.debug(logPrefix + "gyr[z]:{}", mov[2]);
						LOG.debug(logPrefix + "acc[x]:{}", mov[3]);
						LOG.debug(logPrefix + "acc[y]:{}", mov[4]);
						LOG.debug(logPrefix + "acc[z]:{}", mov[5]);
						LOG.debug(logPrefix + "mag[x]:{}", mov[6]);
						LOG.debug(logPrefix + "mag[y]:{}", mov[7]);
						LOG.debug(logPrefix + "mag[z]:{}", mov[8]);
					} catch (IOException e) {
						LOG.warn(logPrefix + "caught - {}", e.toString());
					}
				}

				if (!isData) {
					continue;
				}

				for (IDataSender sender : senders) {
					try {
						if (sender.isConnected()) {
							sender.send(cc2650Data);
						}
					} catch (IOException e) {
						LOG.warn(logPrefix + "{} caught - {}", sender.getClass().getSimpleName(), e.toString());
					}
				}
			} catch (UnsupportedOperationException e) {
				LOG.warn(logPrefix + "caught - {}", e.toString());
			} catch (ArrayIndexOutOfBoundsException e) {
				LOG.warn(logPrefix + "caught - {}", e.toString());
			} catch (DBusExecutionException e) {
				LOG.warn(logPrefix + "caught - {}", e.toString());
				try {
					cc2650.disconnect();
				} catch (DBusExecutionException e1) {
					LOG.warn(logPrefix + "caught - {}", e1.toString());
				}
			}
		}
	}
}

class CC2650PropertiesChangedHandler extends AbstractPropertiesChangedHandler {
	private static final Logger LOG = LoggerFactory.getLogger(CC2650PropertiesChangedHandler.class);

	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

	private final ConcurrentMap<String, CC2650Driver> deviceMap;
	private final List<IDataSender> senders;
	private final String clientID;

	public CC2650PropertiesChangedHandler(ConcurrentMap<String, CC2650Driver> deviceMap, List<IDataSender> senders, String clientID) {
		this.deviceMap = deviceMap;
		this.senders = senders;
		this.clientID = clientID;
	}

	private Object lookupObject(String dbusPath) {
		Iterator<Entry<String, CC2650Driver>> it = deviceMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, CC2650Driver> entry = it.next();
			BluetoothDevice device = entry.getValue().getBluetoothDevice();
			if  (device.getDbusPath().equals(dbusPath)) {
				return device;
			}
			for (BluetoothGattService service : device.getGattServices()) {
				if (service.getDbusPath().equals(dbusPath)) {
					return service;
				}
				for (BluetoothGattCharacteristic characteristic : service.getGattCharacteristics()) {
					if (characteristic.getDbusPath().equals(dbusPath)) {
						return characteristic;
					}
					for (BluetoothGattDescriptor descriptor : characteristic.getGattDescriptors()) {
						if (descriptor.getDbusPath().equals(dbusPath)) {
							return descriptor;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void handle(PropertiesChanged properties) {
		LOG.trace("path:{} sig:{} interface:{}", properties.getPath(), properties.getName(), properties.getInterfaceName());

		Object btObject = lookupObject(properties.getPath());
		if ((btObject == null) || !(btObject instanceof BluetoothGattCharacteristic)) {
			return;
		}

		Date date = new Date();
		String dateString = sdf.format(date);

		BluetoothGattCharacteristic charObject = (BluetoothGattCharacteristic)btObject;
		String uuid = charObject.getUuid();

		CC2650Driver cc2650 = deviceMap.get(charObject.getService().getDevice().getAddress());

		String logPrefix = "[" + cc2650.getAdapterDeviceName() + "] " + cc2650.getAddress() + " ";

		CC2650Data cc2650Data = new CC2650Data();

		cc2650Data.clientID = clientID;
		cc2650Data.deviceID = cc2650.getAddress();
		cc2650Data.samplingDate = dateString;
		cc2650Data.samplingTimeMillis = date.getTime();

		cc2650Data.firmwareVersion = cc2650.getFirmwareVersion();

		boolean isData = false;

		Map<String, Variant<?>> propMap = properties.getPropertiesChanged();
		Iterator<Entry<String, Variant<?>>> it = propMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Variant<?>> entry = it.next();
			Object object = entry.getValue().getValue();

			LOG.trace(logPrefix + "[{}] {} -> {} uuid:{}", object.getClass().getName(), entry.getKey(), object, uuid);

			if ((object == null) || !(object instanceof byte[])) {
				continue;
			}

			byte[] data = (byte[])object;

			if (uuid.equals(CC2650Driver.UUID_TEMPERATURE_DATA) && cc2650.isNotifyingTemperature()) {
				float[] temp = CC2650Driver.calculateTemperature(data);

				cc2650Data.objectTemperature = new ObjectTemperature();
				cc2650Data.objectTemperature.value = temp[0];

				cc2650Data.ambientTemperature = new AmbientTemperature();
				cc2650Data.ambientTemperature.value = temp[1];

				isData = true;

				LOG.debug(logPrefix + "obj:{} amb:{}", temp[0], temp[1]);
			} else if (uuid.equals(CC2650Driver.UUID_HUMIDITY_DATA) && cc2650.isNotifyingHumidity()) {
				float humidity = CC2650Driver.calculateHumidity(data);

				cc2650Data.humidity = new Humidity();
				cc2650Data.humidity.value = humidity;

				isData = true;

				LOG.debug(logPrefix + "humidity:{}", humidity);
			} else if (uuid.equals(CC2650Driver.UUID_PRESSURE_DATA) && cc2650.isNotifyingPressure()) {
				float pressure = CC2650Driver.calculatePressure(data);

				cc2650Data.pressure = new Pressure();
				cc2650Data.pressure.value = pressure;

				isData = true;

				LOG.debug(logPrefix + "pressure:{}", pressure);
			} else if (uuid.equals(CC2650Driver.UUID_OPTICAL_DATA) && cc2650.isNotifyingOptical()) {
				float optical = CC2650Driver.calculateOptical(data);

				cc2650Data.optical = new Optical();
				cc2650Data.optical.value = optical;

				isData = true;

				LOG.debug(logPrefix + "optical:{}", optical);
			} else if (uuid.equals(CC2650Driver.UUID_MOVEMENT_DATA) && cc2650.isNotifyingMovement()) {
				float[] gyr = CC2650Driver.calculateGyroscope(data);
				float[] acc = CC2650Driver.calculateAccelerometer(data, cc2650.getAccelerometerRange());
				float[] mag = CC2650Driver.calculateMagnetometer(data);

				cc2650Data.gyroscope = new Gyroscope();
				cc2650Data.gyroscope.x = gyr[0];
				cc2650Data.gyroscope.y = gyr[1];
				cc2650Data.gyroscope.z = gyr[2];

				cc2650Data.accelerometer = new Accelerometer();
				cc2650Data.accelerometer.x = acc[0];
				cc2650Data.accelerometer.y = acc[1];
				cc2650Data.accelerometer.z = acc[2];

				cc2650Data.magnetometer = new Magnetometer();
				cc2650Data.magnetometer.x = mag[0];
				cc2650Data.magnetometer.y = mag[1];
				cc2650Data.magnetometer.z = mag[2];

				isData = true;

				LOG.debug(logPrefix + "gyr[x]:{}", gyr[0]);
				LOG.debug(logPrefix + "gyr[y]:{}", gyr[1]);
				LOG.debug(logPrefix + "gyr[z]:{}", gyr[2]);
				LOG.debug(logPrefix + "acc[x]:{}", acc[0]);
				LOG.debug(logPrefix + "acc[y]:{}", acc[1]);
				LOG.debug(logPrefix + "acc[z]:{}", acc[2]);
				LOG.debug(logPrefix + "mag[x]:{}", mag[0]);
				LOG.debug(logPrefix + "mag[y]:{}", mag[1]);
				LOG.debug(logPrefix + "mag[z]:{}", mag[2]);
			}
		}

		if (!isData) {
			return;
		}

		for (IDataSender sender : senders) {
			try {
				if (sender.isConnected()) {
					sender.send(cc2650Data);
				}
			} catch (IOException e) {
				LOG.warn(logPrefix + "{} caught - {}", sender.getClass().getSimpleName(), e.toString());
			}
		}
	}
}
