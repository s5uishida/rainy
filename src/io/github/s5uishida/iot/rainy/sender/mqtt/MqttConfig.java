package io.github.s5uishida.iot.rainy.sender.mqtt;

import io.github.s5uishida.iot.rainy.util.AbstractConfig;
import io.github.s5uishida.iot.rainy.util.ConfigParams;

/*
 * @author s5uishida
 *
 */
public class MqttConfig extends AbstractConfig {
	public static final String BROKER_URI_KEY	= "brokerUri";
	public static final String USER_NAME_KEY		= "userName";
	public static final String PASSWORD_KEY		= "password";
	public static final String CLIENT_ID_KEY		= "clientID";
	public static final String QOS_KEY			= "qos";
	public static final String TOPIC_KEY			= "topic";

	private static MqttConfig config;

	private MqttConfig(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static MqttConfig getInstance() {
		if (config == null) {
			config = new MqttConfig(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.MQTT_CONFIG_FILE);
		}
		return config;
	}

	public String getBrokerUri() {
		return getConfig(BROKER_URI_KEY, "tcp://localhost:1883");
	}

	public String getUserName() {
		return getConfig(USER_NAME_KEY, null);
	}

	public String getPassword() {
		return getConfig(PASSWORD_KEY, null);
	}

	public String getClientID() {
		return getConfig(CLIENT_ID_KEY, null);
	}

	public int getQos() {
		return getConfig(QOS_KEY, 0);
	}

	public String getTopic() {
		return getConfig(TOPIC_KEY, "rainy");
	}
}
