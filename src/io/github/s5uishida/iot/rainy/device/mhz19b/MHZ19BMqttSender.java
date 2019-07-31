package io.github.s5uishida.iot.rainy.device.mhz19b;

import java.io.IOException;

import io.github.s5uishida.iot.rainy.device.mhz19b.data.MHZ19BData;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.mqtt.AbstractMqttSender;

/*
 * @author s5uishida
 *
 */
public class MHZ19BMqttSender extends AbstractMqttSender implements IDataSender {
	private static final MHZ19BConfig config = MHZ19BConfig.getInstance();

	private final boolean prettyPrinting;

	public MHZ19BMqttSender() throws IOException {
		super();
		prettyPrinting = config.getPrettyPrinting();
	}

	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof MHZ19BData)) {
			return;
		}

		MHZ19BData mhz19b = (MHZ19BData)object;
		String subTopic = formatSubTopic(mhz19b.clientID + "_" + mhz19b.deviceID);

		String data = mapper.writeValueAsString(mhz19b);
		if (LOG.isDebugEnabled() && prettyPrinting) {
			LOG.debug("MH-Z19B JSON -\n{}", prettyMapper.writeValueAsString(mhz19b));
		}

		execute(subTopic, data);
	}
}
