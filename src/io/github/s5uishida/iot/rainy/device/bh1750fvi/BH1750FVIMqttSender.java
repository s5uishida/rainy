package io.github.s5uishida.iot.rainy.device.bh1750fvi;

import java.io.IOException;

import io.github.s5uishida.iot.rainy.device.bh1750fvi.data.BH1750FVIData;
import io.github.s5uishida.iot.rainy.sender.IDataSender;
import io.github.s5uishida.iot.rainy.sender.mqtt.AbstractMqttSender;

/*
 * @author s5uishida
 *
 */
public class BH1750FVIMqttSender extends AbstractMqttSender implements IDataSender {
	private static final BH1750FVIConfig config = BH1750FVIConfig.getInstance();

	private final boolean prettyPrinting;

	public BH1750FVIMqttSender() throws IOException {
		super();
		prettyPrinting = config.getPrettyPrinting();
	}

	@Override
	public void send(Object object) throws IOException {
		if (!(object instanceof BH1750FVIData)) {
			return;
		}

		BH1750FVIData bh1750fvi = (BH1750FVIData)object;
		String subTopic = formatSubTopic(bh1750fvi.clientID + "_" + bh1750fvi.deviceID);

		String data = mapper.writeValueAsString(bh1750fvi);
		if (LOG.isDebugEnabled() && prettyPrinting) {
			LOG.debug("BH1750FVI JSON -\n{}", prettyMapper.writeValueAsString(bh1750fvi));
		}

		execute(subTopic, data);
	}
}
