package io.github.s5uishida.iot.rainy.device.cc2650.data;

import io.github.s5uishida.iot.rainy.data.CommonData;

/*
 * @author s5uishida
 *
 */
public class CC2650Data extends CommonData {
	public String firmwareVersion;
	public BatteryLevel batteryLevel;

	public ObjectTemperature objectTemperature;
	public AmbientTemperature ambientTemperature;
	public Humidity humidity;
	public Pressure pressure;
	public Optical optical;
	public Gyroscope gyroscope;
	public Accelerometer accelerometer;
	public Magnetometer magnetometer;
}
