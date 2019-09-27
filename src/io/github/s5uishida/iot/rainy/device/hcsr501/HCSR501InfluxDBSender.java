package io.github.s5uishida.iot.rainy.device.hcsr501;

import java.io.IOException;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point.Builder;

import io.github.s5uishida.iot.rainy.device.hcsr501.data.HCSR501Data;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.influxdb.AbstractInfluxDBSender;

/*
 * @author s5uishida
 *
 */
public class HCSR501InfluxDBSender extends AbstractInfluxDBSender implements IDataSender {
	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof HCSR501Data)) {
			return;
		}

		HCSR501Data hcsr501 = (HCSR501Data)object;
		String dbName = formatDBName(hcsr501.clientID + "_" + hcsr501.deviceID);

		BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy(retentionPolicy).build();

		Builder builder = setCommonFields("hcsr501", hcsr501);

		if (hcsr501.detection != null) {
			String field = "detection";
			builder.addField(field + "_value", hcsr501.detection.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", hcsr501.detection.unit);
			}
		}

		batchPoints.point(builder.build());

		execute(dbName, batchPoints);
	}
}
