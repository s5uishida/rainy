package io.github.s5uishida.iot.rainy.device.cc2650;

import java.io.IOException;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point.Builder;

import io.github.s5uishida.iot.rainy.device.cc2650.data.CC2650Data;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.influxdb.AbstractInfluxDBSender;

/*
 * @author s5uishida
 *
 */
public class CC2650InfluxDBSender extends AbstractInfluxDBSender implements IDataSender {
	private Builder setCC2650CommonFields(String field, CC2650Data cc2650) {
		Builder builder = setCommonFields(field, cc2650);
		if (!dataOnly) {
			builder.addField(field, cc2650.firmwareVersion);
		}
		return builder;
	}

	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof CC2650Data)) {
			return;
		}

		CC2650Data cc2650 = (CC2650Data)object;
		String dbName = formatDBName(cc2650.deviceID);

		BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy(retentionPolicy).build();

		Builder builder = setCC2650CommonFields("cc2650", cc2650);

		if (cc2650.batteryLevel != null) {
			String field = "batteryLevel";
			builder.addField(field + "_value", cc2650.batteryLevel.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", cc2650.batteryLevel.unit);
			}
		}

		if (cc2650.objectTemperature != null) {
			String field = "objectTemperature";
			builder.addField(field + "_value", cc2650.objectTemperature.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", cc2650.objectTemperature.unit);
			}
		}

		if (cc2650.ambientTemperature != null) {
			String field = "ambientTemperature";
			builder.addField(field + "_value", cc2650.ambientTemperature.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", cc2650.ambientTemperature.unit);
			}
		}

		if (cc2650.humidity != null) {
			String field = "humidity";
			builder.addField(field + "_value", cc2650.humidity.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", cc2650.humidity.unit);
			}
		}

		if (cc2650.pressure != null) {
			String field = "pressure";
			builder.addField(field + "_value", cc2650.pressure.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", cc2650.pressure.unit);
			}
		}

		if (cc2650.optical != null) {
			String field = "optical";
			builder.addField(field + "_value", cc2650.optical.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", cc2650.optical.unit);
			}
		}

		if (cc2650.gyroscope != null) {
			String field = "gyroscope";
			builder.addField(field + "_x", cc2650.gyroscope.x);
			builder.addField(field + "_y", cc2650.gyroscope.y);
			builder.addField(field + "_z", cc2650.gyroscope.z);
			if (!dataOnly) {
				builder.addField(field + "_unit", cc2650.gyroscope.unit);
			}
		}

		if (cc2650.accelerometer != null) {
			String field = "accelerometer";
			builder.addField(field + "_x", cc2650.accelerometer.x);
			builder.addField(field + "_y", cc2650.accelerometer.y);
			builder.addField(field + "_z", cc2650.accelerometer.z);
			if (!dataOnly) {
				builder.addField(field + "_unit", cc2650.accelerometer.unit);
			}
		}

		if (cc2650.magnetometer != null) {
			String field = "magnetometer";
			builder.addField(field + "_x", cc2650.magnetometer.x);
			builder.addField(field + "_y", cc2650.magnetometer.y);
			builder.addField(field + "_z", cc2650.magnetometer.z);
			if (!dataOnly) {
				builder.addField(field + "_unit", cc2650.magnetometer.unit);
			}
		}

		batchPoints.point(builder.build());

		execute(dbName, batchPoints);
	}
}
