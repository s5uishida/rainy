package io.github.s5uishida.iot.rainy.device;

/*
 * @author s5uishida
 *
 */
public class I2cBusAddress {
	private final int bus;
	private final byte address;

	public I2cBusAddress(int bus, byte address) {
		this.bus = bus;
		this.address = address;
	}

	public int getBus() {
		return bus;
	}

	public byte getAddress() {
		return address;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}

		if (!(object instanceof I2cBusAddress)) {
			return false;
		}

		I2cBusAddress i2cBusAddress = (I2cBusAddress)object;
		return (this.bus == i2cBusAddress.bus) && (this.address == i2cBusAddress.address);
	}

	@Override
	public int hashCode() {
		return bus * 256 + address;
	}

	@Override
	public String toString() {
		return bus + ":" + String.format("%x", address);
	}
}
