package io.github.s5uishida.iot.rainy.device.bme280;

import java.io.IOException;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point.Builder;

import io.github.s5uishida.iot.rainy.device.bme280.data.BME280Data;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.influxdb.AbstractInfluxDBSender;

/*
 * @author s5uishida
 *
 */
public class BME280InfluxDBSender extends AbstractInfluxDBSender implements IDataSender {
	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof BME280Data)) {
			return;
		}

		BME280Data bme280 = (BME280Data)object;
		String dbName = formatDBName(bme280.clientID + "_" + bme280.deviceID);

		BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy(retentionPolicy).build();

		Builder builder = setCommonFields("bme280", bme280);

		if (bme280.temperature != null) {
			String field = "temperature";
			builder.addField(field + "_value", bme280.temperature.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", bme280.temperature.unit);
			}
		}

		if (bme280.humidity != null) {
			String field = "humidity";
			builder.addField(field + "_value", bme280.humidity.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", bme280.humidity.unit);
			}
		}

		if (bme280.pressure != null) {
			String field = "pressure";
			builder.addField(field + "_value", bme280.pressure.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", bme280.pressure.unit);
			}
		}

		batchPoints.point(builder.build());

		execute(dbName, batchPoints);
	}
}
