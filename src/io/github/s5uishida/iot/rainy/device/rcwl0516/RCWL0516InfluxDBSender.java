package io.github.s5uishida.iot.rainy.device.rcwl0516;

import java.io.IOException;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point.Builder;

import io.github.s5uishida.iot.rainy.device.rcwl0516.data.RCWL0516Data;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.influxdb.AbstractInfluxDBSender;

/*
 * @author s5uishida
 *
 */
public class RCWL0516InfluxDBSender extends AbstractInfluxDBSender implements IDataSender {
	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof RCWL0516Data)) {
			return;
		}

		RCWL0516Data rcwl0516 = (RCWL0516Data)object;
		String dbName = formatDBName(rcwl0516.clientID + "_" + rcwl0516.deviceID);

		BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy(retentionPolicy).build();

		Builder builder = setCommonFields("rcwl0516", rcwl0516);

		if (rcwl0516.detection != null) {
			String field = "detection";
			builder.addField(field + "_value", rcwl0516.detection.value);
			if (!dataOnly) {
				builder.addField(field + "_unit", rcwl0516.detection.unit);
			}
		}

		batchPoints.point(builder.build());

		execute(dbName, batchPoints);
	}
}
