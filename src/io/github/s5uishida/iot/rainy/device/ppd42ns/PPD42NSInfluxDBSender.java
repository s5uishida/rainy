package io.github.s5uishida.iot.rainy.device.ppd42ns;

import java.io.IOException;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point.Builder;

import io.github.s5uishida.iot.rainy.device.ppd42ns.data.PPD42NSData;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.influxdb.AbstractInfluxDBSender;

/*
 * @author s5uishida
 *
 */
public class PPD42NSInfluxDBSender extends AbstractInfluxDBSender implements IDataSender {
	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof PPD42NSData)) {
			return;
		}

		PPD42NSData ppd42ns = (PPD42NSData)object;
		String dbName = formatDBName(ppd42ns.clientID + "_" + ppd42ns.deviceID);

		BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy(retentionPolicy).build();

		Builder builder = setCommonFields("ppd42ns", ppd42ns);

		if (ppd42ns.pcs != null) {
			String field = "pcs";
			builder.addField(field + "_value", ppd42ns.pcs.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", ppd42ns.pcs.unit);
			}
		}
		if (ppd42ns.ugm3 != null) {
			String field = "ugm3";
			builder.addField(field + "_value", ppd42ns.ugm3.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", ppd42ns.ugm3.unit);
			}
		}

		batchPoints.point(builder.build());

		execute(dbName, batchPoints);
	}
}
