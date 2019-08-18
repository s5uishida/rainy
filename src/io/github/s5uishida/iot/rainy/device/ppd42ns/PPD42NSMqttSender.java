package io.github.s5uishida.iot.rainy.device.ppd42ns;

import java.io.IOException;

import io.github.s5uishida.iot.rainy.device.ppd42ns.data.PPD42NSData;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.mqtt.AbstractMqttSender;

/*
 * @author s5uishida
 *
 */
public class PPD42NSMqttSender extends AbstractMqttSender implements IDataSender {
	private static final PPD42NSConfig config = PPD42NSConfig.getInstance();

	private final boolean prettyPrinting;

	public PPD42NSMqttSender() throws IOException {
		super();
		prettyPrinting = config.getPrettyPrinting();
	}

	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof PPD42NSData)) {
			return;
		}

		PPD42NSData ppd42ns = (PPD42NSData)object;
		String subTopic = formatSubTopic(ppd42ns.clientID + "_" + ppd42ns.deviceID);

		String data = mapper.writeValueAsString(ppd42ns);
		if (LOG.isDebugEnabled() && prettyPrinting) {
			LOG.debug("PPD42NS JSON -\n{}", prettyMapper.writeValueAsString(ppd42ns));
		}

		execute(subTopic, data);
	}
}
