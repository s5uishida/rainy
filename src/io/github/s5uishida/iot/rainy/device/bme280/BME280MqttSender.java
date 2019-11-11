package io.github.s5uishida.iot.rainy.device.bme280;

import java.io.IOException;

import io.github.s5uishida.iot.rainy.device.bme280.data.BME280Data;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.mqtt.AbstractMqttSender;

/*
 * @author s5uishida
 *
 */
public class BME280MqttSender extends AbstractMqttSender implements IDataSender {
	private static final BME280Config config = BME280Config.getInstance();

	private final boolean prettyPrinting;

	public BME280MqttSender() throws IOException {
		super();
		prettyPrinting = config.getPrettyPrinting();
	}

	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof BME280Data)) {
			return;
		}

		BME280Data bme280 = (BME280Data)object;
		String subTopic = formatSubTopic(bme280.clientID + "_" + bme280.deviceID);

		String data = mapper.writeValueAsString(bme280);
		if (LOG.isDebugEnabled() && prettyPrinting) {
			LOG.debug("BME280 JSON -\n{}", prettyMapper.writeValueAsString(bme280));
		}

		execute(subTopic, data);
	}
}
