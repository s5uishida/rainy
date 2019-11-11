package io.github.s5uishida.iot.rainy.device.bh1750fvi;

import java.io.IOException;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point.Builder;

import io.github.s5uishida.iot.rainy.device.bh1750fvi.data.BH1750FVIData;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.influxdb.AbstractInfluxDBSender;

/*
 * @author s5uishida
 *
 */
public class BH1750FVIInfluxDBSender extends AbstractInfluxDBSender implements IDataSender {
	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof BH1750FVIData)) {
			return;
		}

		BH1750FVIData bh1750fvi = (BH1750FVIData)object;
		String dbName = formatDBName(bh1750fvi.clientID + "_" + bh1750fvi.deviceID);

		BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy(retentionPolicy).build();

		Builder builder = setCommonFields("bh1750fvi", bh1750fvi);

		if (bh1750fvi.optical != null) {
			String field = "optical";
			builder.addField(field + "_value", bh1750fvi.optical.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", bh1750fvi.optical.unit);
			}
		}

		batchPoints.point(builder.build());

		execute(dbName, batchPoints);
	}
}
