package io.github.s5uishida.iot.rainy.device.mhz19b;

import java.io.IOException;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point.Builder;

import io.github.s5uishida.iot.rainy.device.mhz19b.data.MHZ19BData;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.influxdb.AbstractInfluxDBSender;

/*
 * @author s5uishida
 *
 */
public class MHZ19BInfluxDBSender extends AbstractInfluxDBSender implements IDataSender {
	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof MHZ19BData)) {
			return;
		}

		MHZ19BData mhz19b = (MHZ19BData)object;
		String dbName = formatDBName(mhz19b.clientID + "_" + mhz19b.deviceID);

		BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy(retentionPolicy).build();

		Builder builder = setCommonFields("mhz19b", mhz19b);

		if (mhz19b.co2GasConcentration != null) {
			String field = "co2GasConcentration";
			builder.addField(field + "_value", mhz19b.co2GasConcentration.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", mhz19b.co2GasConcentration.unit);
			}
		}

		batchPoints.point(builder.build());

		execute(dbName, batchPoints);
	}
}
