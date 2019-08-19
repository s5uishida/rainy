package io.github.s5uishida.iot.rainy.sender.influxdb;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.s5uishida.iot.rainy.data.CommonData;

/*
 * @author s5uishida
 *
 */
public class AbstractInfluxDBSender {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractInfluxDBSender.class);

	protected static final InfluxDBConfig config = InfluxDBConfig.getInstance();

	protected static final String tagName = "atag";
	protected static final String tagValue = "avalue";

	protected final Set<String> dbNamesSet = new HashSet<String>();
	protected final String retentionPolicy = "autogen";

	protected final String influxDBUrl;
	protected final String userName;
	protected final String password;
	protected final boolean dataOnly;

	protected InfluxDB client;

	protected AbstractInfluxDBSender() {
		this.influxDBUrl = Objects.requireNonNull(config.getInfluxDBUrl());
		this.userName = Objects.requireNonNull(config.getUserName());
		this.password = Objects.requireNonNull(config.getPassword());
		this.dataOnly = config.getDataOnly();
	}

	protected void existClient() throws IOException {
		if (client == null) {
			throw new IOException("not connected to " + influxDBUrl);
		}
	}

	protected String formatDBName(String dbName) {
		return dbName.replaceAll("[:;,\\.\\-/\\(\\)\\[\\]]", "_");
	}

	public void connect() throws IOException {
		try {
			LOG.info("connecting to {}...", influxDBUrl);
			client = InfluxDBFactory.connect(influxDBUrl, userName, password);
			BatchOptions options = BatchOptions.DEFAULTS.actions(config.getActions()).flushDuration(config.getFlushDuration());
			client.enableBatch(options);
			LOG.info("connected to {} version:{}", influxDBUrl, client.version());
		} catch (Exception e) {
			LOG.warn("failed to connect to {}", influxDBUrl);
			throw new IOException(e);
		}
	}

	public void disconnect() throws IOException {
		try {
			existClient();
			LOG.info("disconnecting to {}...", influxDBUrl);
			client.close();
			LOG.info("disconnected to {}", influxDBUrl);
		} catch (Exception e) {
			LOG.warn("failed to disconnect to {}", influxDBUrl);
			throw new IOException(e);
		}
	}

	public boolean isConnected() {
		try {
			existClient();
		} catch (IOException e) {
			return false;
		}
		return client.ping().isGood();
	}

	protected Builder setCommonFields(String field, CommonData commonData) {
		Builder builder = Point.measurement(field).tag(tagName, tagValue);
		if (!dataOnly) {
			builder.addField("clientID", commonData.clientID);
			builder.addField("samplingDate", commonData.samplingDate);
		}
		long samplingTime = commonData.samplingTimeMillis * 1000000 + commonData.samplingTimeNanos;
		builder.addField("samplingTime", samplingTime);
		builder.time(samplingTime, TimeUnit.NANOSECONDS);
		return builder;
	}

	protected void execute(String dbName, BatchPoints batchPoints) throws IOException {
		existClient();
		if (batchPoints.getPoints().size() == 0) {
			return;
		}
		if (!dbNamesSet.contains(dbName)) {
			client.query(new Query("CREATE DATABASE \"" + dbName + "\""));
			LOG.info("CREATE DATABASE {}", dbName);
			dbNamesSet.add(dbName);
		}
		client.setDatabase(dbName);
		client.write(batchPoints);
	}
}
