package io.github.s5uishida.iot.rainy.device.cc2650;

import java.io.IOException;

import io.github.s5uishida.iot.rainy.device.cc2650.data.CC2650Data;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.mqtt.AbstractMqttSender;

/*
 * @author s5uishida
 *
 */
public class CC2650MqttSender extends AbstractMqttSender implements IDataSender {
	private static final CC2650Config config = CC2650Config.getInstance();

	private final boolean prettyPrinting;

	public CC2650MqttSender() throws IOException {
		super();
		prettyPrinting = config.getPrettyPrinting();
	}

	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof CC2650Data)) {
			return;
		}

		CC2650Data cc2650 = (CC2650Data)object;
		String subTopic = formatSubTopic(cc2650.deviceID);

		String data = mapper.writeValueAsString(cc2650);
		if (LOG.isDebugEnabled() && prettyPrinting) {
			LOG.debug("CC2650 JSON -\n{}", prettyMapper.writeValueAsString(cc2650));
		}

		execute(subTopic, data);
	}
}
