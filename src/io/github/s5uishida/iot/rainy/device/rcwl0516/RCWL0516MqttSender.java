package io.github.s5uishida.iot.rainy.device.rcwl0516;

import java.io.IOException;

import io.github.s5uishida.iot.rainy.device.rcwl0516.data.RCWL0516Data;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.mqtt.AbstractMqttSender;

/*
 * @author s5uishida
 *
 */
public class RCWL0516MqttSender extends AbstractMqttSender implements IDataSender {
	private static final RCWL0516Config config = RCWL0516Config.getInstance();

	private final boolean prettyPrinting;

	public RCWL0516MqttSender() throws IOException {
		super();
		prettyPrinting = config.getPrettyPrinting();
	}

	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof RCWL0516Data)) {
			return;
		}

		RCWL0516Data rcwl0516 = (RCWL0516Data)object;
		String subTopic = formatSubTopic(rcwl0516.clientID + "_" + rcwl0516.deviceID);

		String data = mapper.writeValueAsString(rcwl0516);
		if (LOG.isDebugEnabled() && prettyPrinting) {
			LOG.debug("RCWL-0516 JSON -\n{}", prettyMapper.writeValueAsString(rcwl0516));
		}

		execute(subTopic, data);
	}
}
