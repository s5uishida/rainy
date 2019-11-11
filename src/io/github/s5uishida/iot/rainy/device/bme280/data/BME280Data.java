package io.github.s5uishida.iot.rainy.device.bme280.data;

import io.github.s5uishida.iot.rainy.data.CommonData;

/*
 * @author s5uishida
 *
 */
public class BME280Data extends CommonData {
	public Temperature temperature;
	public Humidity humidity;
	public Pressure pressure;
}
