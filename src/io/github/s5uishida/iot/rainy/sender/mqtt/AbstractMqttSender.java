package io.github.s5uishida.iot.rainy.sender.mqtt;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/*
 * @author s5uishida
 *
 */
public class AbstractMqttSender {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractMqttSender.class);

	protected static final MqttConfig config = MqttConfig.getInstance();
	protected static final MqttsConfig sslConfig = MqttsConfig.getInstance();

	protected final ObjectMapper mapper = new ObjectMapper();
	protected final ObjectMapper prettyMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	protected final String brokerUri;
	protected final String userName;
	protected final String password;
	protected final String clientID;
	protected final boolean ssl;
	protected final int qos;
	protected final String topic;

	protected final MqttConnectOptions connOpts;
	protected final MqttClient client;

	protected String formatSubTopic(String subTopic) {
		return subTopic.replaceAll("[:;,\\.\\-/\\(\\)\\[\\]]", "_");
	}

	protected AbstractMqttSender() throws IOException {
		brokerUri = Objects.requireNonNull(config.getBrokerUri());
		userName = config.getUserName();
		password = config.getPassword();
		clientID = Objects.requireNonNull(config.getClientID()) + "_" + UUID.randomUUID().toString().split("\\-")[0];

		if (brokerUri.toLowerCase().startsWith("ssl://")) {
			ssl = true;
		} else {
			ssl = false;
		}

		qos = config.getQos();
		if (qos < 0 || qos > 2) {
			throw new IllegalStateException("QoS:" + qos + " is incorrect.");
		}

		topic = Objects.requireNonNull(config.getTopic());

		connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		connOpts.setAutomaticReconnect(true);
		if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
			connOpts.setUserName(userName);
			connOpts.setPassword(password.toCharArray());
		}

		if (ssl) {
			File file = new File(sslConfig.getFilePath());
			if (file.exists()) {
				Properties prop = new Properties();
				prop.load(file.toURI().toURL().openStream());
				connOpts.setSSLProperties(prop);
			} else {
				throw new IllegalStateException("sslConfig:" + sslConfig.getFilePath() + " not found.");
			}
		}

		try {
			client = new MqttClient(brokerUri, clientID, new MemoryPersistence());
		} catch (MqttException e) {
			throw new IOException(e);
		}
	}

	public void connect() throws IOException {
		try {
			LOG.info("[{}] connecting to {}...", clientID, brokerUri);
			client.connect();
			LOG.info("[{}] connected to {}", clientID, brokerUri);
		} catch (MqttException e) {
			LOG.warn("[{}] failed to connect to {}", clientID, brokerUri);
			throw new IOException(e);
		}
	}

	public void disconnect() throws IOException {
		try {
			LOG.info("[{}] disconnecting to {}...", clientID, brokerUri);
			client.disconnectForcibly();
			LOG.info("[{}] disconnected to {}", clientID, brokerUri);
		} catch (MqttException e) {
			LOG.warn("[{}] failed to disconnect to {}", clientID, brokerUri);
			throw new IOException(e);
		}
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	protected void execute(String subTopic, String data) throws IOException {
		try {
			MqttMessage message = new MqttMessage(data.getBytes());
			message.setQos(qos);
			String joinedTopic = topic;
			if (topic.endsWith("/")) {
				joinedTopic += subTopic;
			} else {
				joinedTopic += "/" + subTopic;
			}
			client.publish(joinedTopic, message);
		} catch (MqttException e) {
			throw new IOException(e);
		}
	}
}
