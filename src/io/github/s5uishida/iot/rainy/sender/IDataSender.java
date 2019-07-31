package io.github.s5uishida.iot.rainy.sender;

import java.io.IOException;

/*
 * @author s5uishida
 *
 */
public interface IDataSender {
	void connect() throws IOException;
	void disconnect() throws IOException;
	boolean isConnected();
	void send(Object object) throws IOException;
}
