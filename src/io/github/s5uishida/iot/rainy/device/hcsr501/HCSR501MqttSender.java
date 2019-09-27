package io.github.s5uishida.iot.rainy.device.hcsr501;

import java.io.IOException;

import io.github.s5uishida.iot.rainy.device.hcsr501.data.HCSR501Data;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.mqtt.AbstractMqttSender;

/*
 * @author s5uishida
 *
 */
public class HCSR501MqttSender extends AbstractMqttSender implements IDataSender {
	private static final HCSR501Config config = HCSR501Config.getInstance();

	private final boolean prettyPrinting;

	public HCSR501MqttSender() throws IOException {
		super();
		prettyPrinting = config.getPrettyPrinting();
	}

	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof HCSR501Data)) {
			return;
		}

		HCSR501Data hcsr501 = (HCSR501Data)object;
		String subTopic = formatSubTopic(hcsr501.clientID + "_" + hcsr501.deviceID);

		String data = mapper.writeValueAsString(hcsr501);
		if (LOG.isDebugEnabled() && prettyPrinting) {
			LOG.debug("HC-SR501 JSON -\n{}", prettyMapper.writeValueAsString(hcsr501));
		}

		execute(subTopic, data);
	}
}
