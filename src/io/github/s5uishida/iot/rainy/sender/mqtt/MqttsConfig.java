package io.github.s5uishida.iot.rainy.sender.mqtt;

import io.github.s5uishida.iot.rainy.util.AbstractConfig;
import io.github.s5uishida.iot.rainy.util.ConfigParams;

/*
 * @author s5uishida
 *
 */
public class MqttsConfig extends AbstractConfig {
	public static final String PROTOCOL_KEY					= "com.ibm.ssl.protocol";
	public static final String CONTEXT_PROVIDER_KEY			= "com.ibm.ssl.contextProvider";
	public static final String KEY_STORE_KEY					= "com.ibm.ssl.keyStore";
	public static final String KEY_STORE_PASSWORD_KEY		= "com.ibm.ssl.keyStorePassword";
	public static final String KEY_STORE_TYPE_KEY			= "com.ibm.ssl.keyStoreType";
	public static final String KEY_STORE_PROVIDER_KEY		= "com.ibm.ssl.keyStoreProvider";
	public static final String TRUST_STORE_KEY				= "com.ibm.ssl.trustStore";
	public static final String TRUST_STORE_PASSWORD_KEY	= "com.ibm.ssl.trustStorePassword";
	public static final String TRUST_STORE_TYPE_KEY			= "com.ibm.ssl.trustStoreType";
	public static final String TRUST_STORE_PROVIDER_KEY	= "com.ibm.ssl.trustStoreProvider";
	public static final String ENABLED_CIPHER_SUITES_KEY	= "com.ibm.ssl.enabledCipherSuites";
	public static final String KEY_MANAGER_KEY				= "com.ibm.ssl.keyManager";
	public static final String TRUST_MANAGER_KEY			= "com.ibm.ssl.trustManager";

	private static MqttsConfig config;

	private MqttsConfig(String dirParam, String fileName) {
		super(dirParam, fileName);
	}

	public static MqttsConfig getInstance() {
		if (config == null) {
			config = new MqttsConfig(ConfigParams.CONFIG_DIR_PARAM, ConfigParams.MQTTS_CONFIG_FILE);
		}
		return config;
	}

	public String getProtocol() {
		return getConfig(PROTOCOL_KEY, null);
	}

	public String getContextProvider() {
		return getConfig(CONTEXT_PROVIDER_KEY, null);
	}

	public String getKeyStore() {
		return getConfig(KEY_STORE_KEY, null);
	}

	public String getKeyStorePassword() {
		return getConfig(KEY_STORE_PASSWORD_KEY, null);
	}

	public String getKeyStoreType() {
		return getConfig(KEY_STORE_TYPE_KEY, null);
	}

	public String getKeyStoreProvider() {
		return getConfig(KEY_STORE_PROVIDER_KEY, null);
	}

	public String getTrustStore() {
		return getConfig(TRUST_STORE_KEY, null);
	}

	public String getTrustStorePassword() {
		return getConfig(TRUST_STORE_PASSWORD_KEY, null);
	}

	public String getTrustStoreType() {
		return getConfig(TRUST_STORE_TYPE_KEY, null);
	}

	public String getTrustStoreProvider() {
		return getConfig(TRUST_STORE_PROVIDER_KEY, null);
	}

	public String getEnabledCipherSuites() {
		return getConfig(ENABLED_CIPHER_SUITES_KEY, null);
	}

	public String getKeyManager() {
		return getConfig(KEY_MANAGER_KEY, null);
	}

	public String getTrustManager() {
		return getConfig(TRUST_MANAGER_KEY, null);
	}
}
