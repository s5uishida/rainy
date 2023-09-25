# rainy - a tiny tool for iot data collection and monitoring
rainy is a tiny tool for IoT data collection and monitoring, and supports the following devices and protocols:  
- [TI SensorTag CC2650](http://processors.wiki.ti.com/index.php/CC2650_SensorTag_User's_Guide) - [java driver](https://github.com/s5uishida/cc2650-driver) by [bluez-dbus](https://github.com/hypfvieh/bluez-dbus)
  - IR Temperature (Object / Ambience)
  - Relative humidity
  - Barometric pressure
  - Optical
  - Movement (Gyroscope / Accelerometer / Magnetometer)
- [BME280](http://static.cactus.io/docs/sensors/barometric/bme280/BST-BME280_DS001-10.pdf) - [java driver](https://github.com/s5uishida/bme280-driver) by [Pi4J](https://pi4j.com/)
  - Temperature
  - Relative humidity
  - Barometric pressure  
  **If you do not have CC2650, you can substitute these sensors with BME280.**
- [BH1750FVI](https://www.mouser.com/datasheet/2/348/bh1750fvi-e-186247.pdf) - [java driver](https://github.com/s5uishida/bh1750fvi-driver) by [Pi4J](https://pi4j.com/)  
  Optical  
  **If you do not have CC2650, you can substitute this sensor with BH1750FVI.**
- [MH-Z19B](https://www.winsen-sensor.com/d/files/infrared-gas-sensor/mh-z19b-co2-ver1_0.pdf) - [java driver](https://github.com/s5uishida/mh-z19b-driver) by [jSerialComm](https://github.com/Fazecast/jSerialComm)  
  CO2 gas concentration
- [PPD42NS](http://wiki.seeedstudio.com/Grove-Dust_Sensor/) - [java driver](https://github.com/s5uishida/ppd42ns-driver) by [Pi4J](https://pi4j.com/)  
  PM2.5 dust concentration
- [RCWL-0516](https://www.epitran.it/ebayDrive/datasheet/19.pdf) - [java driver](https://github.com/s5uishida/rcwl-0516-driver) by [Pi4J](https://pi4j.com/)  
  Motion detector (Microwave)
- [HC-SR501](https://www.mpja.com/download/31227sc.pdf) - [java driver](https://github.com/s5uishida/hc-sr501-driver) by [Pi4J](https://pi4j.com/)  
  Motion detector (PIR)
- [OPC-UA](https://opcfoundation.org/developer-tools/specifications-unified-architecture) - [java driver](https://github.com/s5uishida/rainy-opcua) by [Eclipse Milo](https://github.com/eclipse/milo)  
  Protocol of industrial automation

These data can be sent to [InfluxDB](https://www.influxdata.com/) (Time Series Database) for visualization, or sent to MQTT Broker to be used as a data source for any other purposes.

rainy runs on [Apache Felix](https://felix.apache.org/) (OSGi). I think that rainy can be embedded in the environment without OSGi.

I releases this in the form of the Eclipse plug-in project and **this tool -** [**rainy-felix.zip**](https://github.com/s5uishida/rainy/releases).
The list of my source codes and third party bundles is [Bundle list](#bundle_list).
You need Java 8.
I have confirmed that it works in Raspberry Pi 3B ([Raspbian Buster Lite OS](https://www.raspberrypi.org/downloads/raspbian/) (2019-07-10)).

The following figure is overview of rainy.

<img src="./images/rainy_overview_0.png" title="./images/rainy_overview_0.png" width=800px></img>

The following figure is overview which the monitoring is running together with rainy on same Raspberry Pi 3B.
I checked easily that it also works with Raspberry Pi 4B.

<img src="./images/rainy_overview_2.png" title="./images/rainy_overview_2.png" width=800px></img>

The following image shows the hardware configuration.

<img src="./images/rainy_hardware_1.png" title="./images/rainy_hardware_1_1.png" width=700px></img>

---
<h2>Table of Contents</h2>

- [Purpose](#purpose)
- [Setup OS](#setup_os)
- [Setup sending data](#setup_sending_data)
  - [Install InfluxDB and startup](#install_influxdb)
  - [Install Mosquitto (MQTT Broker) and startup](#install_mosquitto)
- [Setup visualization tools](#install_visualization)
  - [Install Grafana and startup](#install_grafana)
  - [Install Chronograf and startup](#install_chronograf)
  - [Testing HTTPS connection with self-signed certificate](#test_https_connection)
    - [Create a self-signed certificate and private key with file names cert.pem and cert.key](#create_self_signed_cert)
    - [Create /etc/rainy directory and place these files there](#create_directory)
    - [Setting Grafana HTTPS connetion](#set_grafana_https)
    - [Setting Chronograf HTTPS connetion](#set_chronograf_https)
- [Configuration - rainy/conf](#configuration)
  - [rainy.properties](#rainy_properties)
  - [Setting the connection for sending data](#setting_connection_sending_data)
    - [InfluxDB - influxdb.properties](#influxdb_properties)
    - [MQTT - mqtt.properties](#mqtt_properties)
  - [Setting sensors and protocols](#setting_sensors)
    - [CC2650 - cc2650.properties](#cc2650_properties)
    - [BME280 - bme280.properties](#bme280_properties)
    - [BH1750FVI - bh1750fvi.properties](#bh1750fvi_properties)
    - [MH-Z19B - mhz19b.properties](#mhz19b_properties)
    - [PPD42NS - ppd42ns.properties](#ppd42ns_properties)
    - [RCWL-0516 - rcwl0516.properties](#rcwl0516_properties)
    - [HC-SR501 - hcsr501.properties](#hcsr501_properties)
    - [OPC-UA - opcua.properties](#opcua_properties)
      - [OPC-UA server - conf/opcua/milo-public-demo.properties](#opcua_server_properties)
- [Run rainy](#run_rainy)
  - [Output sensor values to the log file](#output_sensor_value)
  - [Check the database name for each device created in InfluxDB](#check_database)
- [Create dashboards with visualization tools](#create_dashboards)
  - [Case Grafana](#case_grafana)
  - [Case Chronograf](#case_chronograf)
- [Check the data sent to MQTT broker](#check_mqtt_data)
- [Limitations](#limitations)
- [Bundle list](#bundle_list)
- [P.S.](#ps)

---

<a id="purpose"></a>

## Purpose

The purpose of this tool is briefly as follows.

- For private / office / factory room, easily collect general environmental information and industrial / factory equipment running information via OPC-UA.
- Real-time monitoring and convert these information to MQTT as a data source for any other purposes.
- Runs as Java / OSGi application on Raspberry Pi 3B (arm) and Ubuntu machine (amd64).

The concept is as follows.

<img src="./images/rainy_concept_0.png" title="./images/rainy_concept_0.png" width=700px></img>

Although I think the functionality and performance of this tool are not sufficient for formal operation, it may be an easy-to-try tool.

<a id="setup_os"></a>

## Setup OS

Please refer to [here](https://github.com/s5uishida/bme280-driver) for setting RaspberryPi 3B as an environment for running rainy.
Both Bluetooth, serial communication, GPIO and I2C can be enabled.

<a id="setup_sending_data"></a>

## Setup sending data

rainy supports InfluxDB and MQTT broker as sending data.

<a id="install_influxdb"></a>

### Install InfluxDB and startup

I am using [InfluxDB](https://www.influxdata.com/) on Ubuntu 18.04.
The installation is as follows from [here](https://portal.influxdata.com/downloads/).
```
# wget https://dl.influxdata.com/influxdb/releases/influxdb_1.7.8_amd64.deb
# dpkg -i influxdb_1.7.8_amd64.deb
# systemctl enable influxdb.service
# systemctl start influxdb.service
```
In addition, the step to install InfluxDB on Raspberry Pi 3B is as follows.
```
# wget https://dl.influxdata.com/influxdb/releases/influxdb_1.7.8_armhf.deb
# dpkg -i influxdb_1.7.8_armhf.deb
# systemctl enable influxdb.service
# systemctl start influxdb.service
```

<a id="install_mosquitto"></a>

### Install Mosquitto (MQTT Broker) and startup

I am using [Mosquitto](https://mosquitto.org/) as MQTT broker on Ubuntu 18.04.
The installation is as follows.
```
# apt-get update
# apt-get install mosquitto
# apt-get install mosquitto-clients
# systemctl enable mosquitto.service
# systemctl start mosquitto.service
```
In addition, the step to install Mosquitto on Raspberry Pi 3B is the same as above.

<a id="install_visualization"></a>

## Setup visualization tools

<a id="install_grafana"></a>

### Install Grafana and startup

I am using [Grafana](https://grafana.com/) on Ubuntu 18.04. The installation is as follows from [here](https://grafana.com/grafana/download?platform=linux).
```
# wget https://dl.grafana.com/oss/release/grafana_6.4.3_amd64.deb
# dpkg -i grafana_6.4.3_amd64.deb
# systemctl enable grafana-server.service
# systemctl start grafana-server.service
```
In addition, the step to install Grafana on Raspberry Pi 3B is as follows.
```
# wget https://dl.grafana.com/oss/release/grafana_6.4.3_armhf.deb
# dpkg -i grafana_6.4.3_armhf.deb
# systemctl enable grafana-server.service
# systemctl start grafana-server.service
```

<a id="install_chronograf"></a>

### Install Chronograf and startup

I am using [Chronograf](https://www.influxdata.com/time-series-platform/chronograf/) on Ubuntu 18.04. The installation is as follows from [here](https://portal.influxdata.com/downloads/).
```
# wget https://dl.influxdata.com/chronograf/releases/chronograf_1.7.14_amd64.deb
# dpkg -i chronograf_1.7.14_amd64.deb
# systemctl enable chronograf.service
# systemctl start chronograf.service
```
In addition, the step to install Chronograf on Raspberry Pi 3B is as follows.
```
# wget https://dl.influxdata.com/chronograf/releases/chronograf_1.7.14_armhf.deb
# dpkg -i chronograf_1.7.14_armhf.deb
# systemctl enable chronograf.service
# systemctl start chronograf.service
```

<a id="test_https_connection"></a>

### Testing HTTPS connection with self-signed certificate

In general, it is not recommended to use a self-signed certificate for formal operation, but it is sufficient for testing purposes.

<a id="create_self_signed_cert"></a>

#### Create a self-signed certificate and private key with file names cert.pem and cert.key

```
# openssl req -x509 -newkey rsa:4096 -sha256 -nodes -keyout cert.key -out cert.pem -subj "/CN=localhost" -days 365
```

<a id="create_directory"></a>

#### Create /etc/rainy directory and place these files there

```
# mkdir /etc/rainy
# cp cert.pem cert.key /etc/rainy/
# cd /etc/rainy
# chmod 0644 cert.pem cert.key
```

<a id="set_grafana_https"></a>

#### Setting Grafana HTTPS connetion

See [here](https://grafana.com/docs/installation/configuration/) for details.

- Edit `/etc/grafana/grafana.ini`  
```
@@ -29,7 +29,7 @@
 #################################### Server ####################################
 [server]
 # Protocol (http, https, socket)
-;protocol = http
+protocol = https
 
 # The ip address to bind to, empty will bind to all interfaces
 ;http_addr =
@@ -58,8 +58,8 @@
 ;enable_gzip = false
 
 # https certs & key file
-;cert_file =
-;cert_key =
+cert_file = /etc/rainy/cert.pem
+cert_key = /etc/rainy/cert.key
 
 # Unix socket path
 ;socket =
```

- Restart Grafana and connect to `https://hostAddress:3000/` with a browser  
```
# systemctl restart grafana-server.service
```

<a id="set_chronograf_https"></a>

#### Setting Chronograf HTTPS connetion

See [here](https://docs.influxdata.com/chronograf/v1.7/administration/managing-security/) for details.

- Edit `/etc/default/chronograf`  
```
TLS_CERTIFICATE=/etc/rainy/cert.pem
TLS_PRIVATE_KEY=/etc/rainy/cert.key
```

- Restart Chronograf and connect to `https://hostAddress:8888/` with a browser  
```
# systemctl restart chronograf.service
```

<a id="configuration"></a>

## Configuration - rainy/conf

<a id="rainy_properties"></a>

### rainy.properties

- **`clientID`**  
  Set a unique client identifier for running rainy.
- **`cc2650`**  
  Set to `true` when using CC2650. default is `false`.  
- **`bme280`**  
  Set to `true` when using BME280. default is `false`.  
- **`bh1750fvi`**  
  Set to `true` when using BH1750FVI. default is `false`.  
- **`mhz19b`**  
  Set to `true` when using MH-Z19B. default is `false`.  
- **`ppd42ns`**  
  Set to `true` when using PPD42NS. default is `false`.  
- **`rcwl0516`**  
  Set to `true` when using RCWL-0516. default is `false`.  
- **`hcsr501`**  
  Set to `true` when using HC-SR501. default is `false`.  
- **`opcua`**  
  Set to `true` when using OPC-UA. default is `false`.

**Note. This tool uses Pi4J for BME280/BH1750FVI/PPD42NS/RCWL-0516/HC-SR501, so these devices can only be used with Raspberry Pi series (arm). Therefore, their feature of this tool does not work on amd64 Linux machines, so set them to `false` on amd64 Linux machines.**  

<a id="setting_connection_sending_data"></a>

### Setting the connection for sending data
  
<a id="influxdb_properties"></a>

#### InfluxDB - influxdb.properties

- **`influxDBUrl`**  
  default is `http://localhost:8086`.
- **`userName`**
- **`password`**
- `actions`  
  Set the number of actions to collect. default is `1000`.
- `flushDuration`  
  Set the time to wait at most (msec). default is `1000`.
- `dataOnly`  
  Set to `true` when collecting only data. default is `true`.

<a id="mqtt_properties"></a>

#### MQTT - mqtt.properties

- **`brokerUri`**  
  default is `tcp://localhost:1883`.
- `userName`
- `password`
- **`clientID`**  
  First half string of unique client identifier when connecting to MQTT broker. After this string, this system adds a random string to complete the clientID string.
- `qos`  
  default is `0`.
- `topic`  
  Topic when publishing data to MQTT broker. default is `rainy`.

<h3 id="setting_sensors">Setting sensors and protocols<h3>

<a id="cc2650_properties"></a>

#### CC2650 - cc2650.properties

[Here](https://github.com/s5uishida/cc2650-driver) is also helpful.
- **`bluetoothAdapter`**  
  Bluetooth adapter name. default is `hci0`.
  Check the list of adapter names with the `hciconfig` command.
```
# hciconfig -a
hci0:   Type: Primary  Bus: UART
        BD Address: B8:27:EB:7D:0F:7D  ACL MTU: 1021:8  SCO MTU: 64:1
        UP RUNNING 
        RX bytes:28342 acl:441 sco:0 events:2265 errors:0
        TX bytes:34605 acl:441 sco:0 commands:1955 errors:0
        Features: 0xbf 0xfe 0xcf 0xfe 0xdb 0xff 0x7b 0x87
        Packet type: DM1 DM3 DM5 DH1 DH3 DH5 HV1 HV2 HV3 
        Link policy: RSWITCH SNIFF 
        Link mode: SLAVE ACCEPT 
        Name: 'raspberrypi'
        Class: 0x000000
        Service Classes: Unspecified
        Device Class: Miscellaneous, 
        HCI Version: 4.1 (0x7)  Revision: 0x168
        LMP Version: 4.1 (0x7)  Subversion: 0x2209
        Manufacturer: Broadcom Corporation (15)
```
- **`influxDB`**  
  Set to `true` when sending data to InfluxDB. default is `false`.
- **`mqtt`**  
  Set to `true` when sending data to MQTT broker. default is `false`.
- `prettyPrinting`  
  Set to `true` when indenting the log output of JSON format data. default is `false`.
  It is also necessary to change the following log level of `conf/logging.properties`.  
  ```
  #io.github.s5uishida.level=INFO
  -->
  io.github.s5uishida.level=FINE
  ```
- `readCrontab`  
  Set the schedule for sensing data in crontab format. default is every minute.
- `batteryLevel`  
  Set to `true` when getting battery level. default is `false`.
- Example of setting Temperature sensor
  - **`temperature`**  
    Set to `true` when using a Temperature sensor. default is `false`.
  - `temperatureNotify`  
    Set to `true` when using the notification function. default is `false`. When the notification function is enabled, the readCrontab schedule for Temperature sensor is disabled.
  - `temperatureNotificationPeriod`  
    Set the notification time interval in milliseconds. default is `1000`.
- Movement
  - `gyroscope`  
    Set to `true` when using a Gyroscope sensor. default is `false`.
  - `accelerometer`  
    Set to `true` when using a Accelerometer sensor. default is `false`.
  - `magnetometer`  
    Set to `true` when using a Magnetometer sensor. default is `false`.
  - `movementNotify`  
    Set to `true` when using the notification function. default is `false`. When the notification function is enabled, the readCrontab schedule for Movement sensor is disabled.
  - `movementNotificationPeriod`  
    Set the notification time interval in milliseconds. default is `1000`.
  - `wakeOnMotion`  
    Set to `true` when sending movement data when a shake is detected. It sends `Movement` data at a time interval specified by notification for 10 seconds. default is `false`.
  - `accelerometerRange`  
    Set Accelerometer range. default is `2`.
- **`devices`**  
  List the addresses of the target CC2650 devices. To check the address of CC2650, use `hcitool` command as follows.
```
# hcitool lescan
LE Scan ...
B0:B4:48:B9:92:86 (unknown)
B0:B4:48:B9:92:86 CC2650 SensorTag
```
After launching `hcitool` command, press the power button of CC2650 and the scan results will be displayed as above.

<a id="bme280_properties"></a>

#### BME280 - bme280.properties

[Here](https://github.com/s5uishida/bme280-driver) is also helpful.

- **`i2cBusAddress`**  
  Set the I2C bus number and address. default is `1:76`.
- **`influxDB`**  
  Set to `true` when sending data to InfluxDB. default is `false`.
- **`mqtt`**  
  Set to `true` when sending data to MQTT broker. default is `false`.
- `prettyPrinting`  
  Set to `true` when indenting the log output of JSON format data. default is `false`.
  It is also necessary to change the following log level of `conf/logging.properties`.  
  ```
  #io.github.s5uishida.level=INFO
  -->
  io.github.s5uishida.level=FINE
  ```
- `readCrontab`  
  Set the schedule for sensing data in crontab format. default is every minute.

<a id="bh1750fvi_properties"></a>

#### BH1750FVI - bh1750fvi.properties

[Here](https://github.com/s5uishida/bh1750fvi-driver) is also helpful.

- **`i2cBusAddress`**  
  Set the I2C bus number and address. default is `1:23`.
- **`influxDB`**  
  Set to `true` when sending data to InfluxDB. default is `false`.
- **`mqtt`**  
  Set to `true` when sending data to MQTT broker. default is `false`.
- `prettyPrinting`  
  Set to `true` when indenting the log output of JSON format data. default is `false`.
  It is also necessary to change the following log level of `conf/logging.properties`.  
  ```
  #io.github.s5uishida.level=INFO
  -->
  io.github.s5uishida.level=FINE
  ```
- `readCrontab`  
  Set the schedule for sensing data in crontab format. default is every minute.

<a id="mhz19b_properties"></a>

#### MH-Z19B - mhz19b.properties

[Here](https://github.com/s5uishida/mh-z19b-driver) is also helpful.

**Note. When using with PPD42NS, please connect pin#4 (Yellow) of PPD42NS to pin#19 (GPIO10) or pin#38 (GPIO20) of Raspberry Pi 3B.
Or when connecting pin#4 (Yellow) of PPD42NS to pin#8 (GPIO14) of Raspberry Pi 3B, connect MH-Z19B to Raspberry Pi 3B via USB serial adapter
(etc. DSD TECH SH-U09C USB to TTL Serial Adapter with FTDI FT232RL Chip).
When connecting MH-Z19B to a USB serial adapter, you should specify `/dev/ttyUSB0` for the port name.**
- **`portName`**  
  Set the serial port name. default is `/dev/ttyAMA0`.
- **`influxDB`**  
  Set to `true` when sending data to InfluxDB. default is `false`.
- **`mqtt`**  
  Set to `true` when sending data to MQTT broker. default is `false`.
- `prettyPrinting`  
  Set to `true` when indenting the log output of JSON format data. default is `false`.
  It is also necessary to change the following log level of `conf/logging.properties`.  
  ```
  #io.github.s5uishida.level=INFO
  -->
  io.github.s5uishida.level=FINE
  ```
- `readCrontab`  
  Set the schedule for sensing data in crontab format. default is every minute.
- `autoCalibration`  
  Set whether to calibrate automatically every 24 hours after powered on. default is `false`.
- `zeroCalibration`  
  Set whether to calibrate to `400` ppm. default is `false`.
- `detectionRange`  
  Set the detection range to `2000` or `5000` ppm. default is `5000`ppm.

<a id="ppd42ns_properties"></a>

#### PPD42NS - ppd42ns.properties

[Here](https://github.com/s5uishida/ppd42ns-driver) is also helpful.

- **`gpioPin`**  
  Set to `GPIO_10`, `GPIO_20` or `GPIO_14`. default is `GPIO_10`.
- **`influxDB`**  
  Set to `true` when sending data to InfluxDB. default is `false`.
- **`mqtt`**  
  Set to `true` when sending data to MQTT broker. default is `false`.
- `prettyPrinting`  
  Set to `true` when indenting the log output of JSON format data. default is `false`.
  It is also necessary to change the following log level of `conf/logging.properties`.  
  ```
  #io.github.s5uishida.level=INFO
  -->
  io.github.s5uishida.level=FINE
  ```
- `readCrontab`  
  Set the schedule for sensing data in crontab format. default is every minute.

<a id="rcwl0516_properties"></a>

#### RCWL-0516 - rcwl0516.properties

[Here](https://github.com/s5uishida/rcwl-0516-driver) is also helpful.

- **`gpioPin`**  
  Set to `GPIO_18`, `GPIO_19`, `GPIO_12` or `GPIO_13`. default is `GPIO_18`.
- **`influxDB`**  
  Set to `true` when sending data to InfluxDB. default is `false`.
- **`mqtt`**  
  Set to `true` when sending data to MQTT broker. default is `false`.
- `prettyPrinting`  
  Set to `true` when indenting the log output of JSON format data. default is `false`.
  It is also necessary to change the following log level of `conf/logging.properties`.  
  ```
  #io.github.s5uishida.level=INFO
  -->
  io.github.s5uishida.level=FINE
  ```

When a motion is detected, an event occurs immediately. If `mqtt` is set to `true`, this event can be received from the MQTT broker.

<a id="hcsr501_properties"></a>

#### HC-SR501 - hcsr501.properties

[Here](https://github.com/s5uishida/hc-sr501-driver) is also helpful.

- **`gpioPin`**  
  Set to `GPIO_18`, `GPIO_19`, `GPIO_12` or `GPIO_13`. default is `GPIO_19`.
- **`influxDB`**  
  Set to `true` when sending data to InfluxDB. default is `false`.
- **`mqtt`**  
  Set to `true` when sending data to MQTT broker. default is `false`.
- `prettyPrinting`  
  Set to `true` when indenting the log output of JSON format data. default is `false`.
  It is also necessary to change the following log level of `conf/logging.properties`.  
  ```
  #io.github.s5uishida.level=INFO
  -->
  io.github.s5uishida.level=FINE
  ```
  
When a motion is detected, an event occurs immediately. If `mqtt` is set to `true`, this event can be received from the MQTT broker.

<a id="opcua_properties"></a>

#### OPC-UA - opcua.properties

- **`influxDB`**  
  Set to `true` when sending data to InfluxDB. default is `false`.
- **`mqtt`**  
  Set to `true` when sending data to MQTT broker. default is `false`.
- `prettyPrinting`  
  Set to `true` when indenting the log output of JSON format data. default is `false`.
  It is also necessary to change the following log level of `conf/logging.properties`.  
  ```
  #io.github.s5uishida.level=INFO
  -->
  io.github.s5uishida.level=FINE
  ```
- `keyStoreType`
- `keyStoreAlias`
- `keyStorePassword`
- `certificate`

<a id="opcua_server_properties"></a>

##### OPC-UA server - conf/opcua/milo-public-demo.properties

The following is an example of [Public Demo Server of Eclipse Milo](https://github.com/eclipse/milo).
- **`use`**  
  Set to `true` to use this server. default is `false`.
- **`serverName`**  
  Set the OPC-UA server name.
- **`endpointIP`**  
  Set the OPC-UA server address.
- **`endpointPort`**  
  Set the OPC-UA server port number.
- `securityPolicy`  
  Set one of `Basic128Rsa15`, `Basic256`, `Basic256Sha256` or `None` in securityPolicy. default is `None`.
- `securityMode`  
  Set one of `Sign`, `SignAndEncrypt` or `None` to securityMode. default is `None`.
- `userName`
- `password`
- `requestTimeout`  
  default is `10000` (msec).
- `sessionTimeout`  
  default is `10000` (msec).
- `publishingInterval`  
  Set publishingInterval (msec). default is `1000` (msec).
- `samplingInterval`  
  Set samplingInterval (msec). default is `500` (msec).
- `queueSize`  
  Set queueSize. default is `10`.
- `dataChangeTrigger`  
  Set one of the following to dataChangeTrigger:  
  - `0` for DataChangeTrigger.Status
  - `1` for DataChangeTrigger.StatusValue
  - `2` for DataChangeTrigger.StatusValueTimestamp  
  default is `1`.
- **`nodeIDs`**  
  List the target node ID. The format is as follows.
  ```
  <namespaceIndex>,<identifier>,<depth>
  ```
  ```
  nodeIDs=2,Dynamic/RandomInt32,0 \
	  2,Dynamic/RandomInt64,0 \
	  2,Dynamic,-1 \
	  0,2295,-1
  ```
  In the above example, specify `2,Dynamic/RandomInt32` and `2,Dynamic/RandomInt64` uniquely, and search for the NodeID to be monitored from `2,Dynamic` and `0,2295`(VendorServerInfo) at an infinite depth. In this case, only `2,Dynamic,-1` and `0,2295,-1` should be specified, but I wrote it for explanation of the format.

`conf/opcua` also contains a `milo-example.properties` file.
This is an example server-properties file of connecting to the server ([milo-example-server](https://github.com/s5uishida/milo-example-server)) where the Milo sample server code is built almost as it is.
  
For reference, there is [toem impulse OPC/UA Extension (Eclipse pulug-in)](https://toem.de/index.php/projects/impulse-opcua) as a tool for easily checking the address space of OPC-UA server.

<a id="run_rainy"></a>

## Run rainy

- start  
  Start rainy as follows.
```
# cd /path/to/rainy-felix/bin
# sh rainy-start.sh
-> ps
START LEVEL 1
   ID   State         Level  Name
[   0] [Active     ] [    0] System Bundle (6.0.3)
[   1] [Active     ] [    1] bcpkix (1.62)
[   2] [Active     ] [    1] bcprov (1.62)
[   3] [Active     ] [    1] java driver for bh1750fvi - ambient light sensor (0.1.2)
[   4] [Active     ] [    1] bluetooth scanner (0.1.4)
[   5] [Active     ] [    1] bluez-dbus-osgi (0.1.2.201911022022)
[   6] [Active     ] [    1] java driver for bme280 - combined humidity, pressure and temperature sensor (0.1.3)
[   7] [Active     ] [    1] bsd-parser-core (0.3.4)
[   8] [Active     ] [    1] bsd-parser-gson (0.3.4)
[   9] [Active     ] [    1] java driver for ti sensortag cc2650 (0.1.2)
[  10] [Active     ] [    1] Apache Commons Lang (3.9.0)
[  11] [Active     ] [    1] cron4j-osgi (2.2.5)
[  12] [Active     ] [    1] dbus-java-osgi (3.2.1.SNAPSHOT)
[  13] [Active     ] [    1] Gson (2.8.5)
[  14] [Active     ] [    1] Guava: Google Core Libraries for Java (26.0.0.jre)
[  15] [Active     ] [    1] java driver for hc-sr501 - pir motion detector sensor module (0.1.1)
[  16] [Active     ] [    1] Java client for InfluxDB (2.15)
[  17] [Active     ] [    1] jSerialComm (2.5.1)
[  18] [Active     ] [    1] Jackson-annotations (2.9.9)
[  19] [Active     ] [    1] Jackson-core (2.9.9)
[  20] [Active     ] [    1] jackson-databind (2.9.9.1)
[  21] [Active     ] [    1] JavaBeans Activation Framework (1.2.0)
[  22] [Active     ] [    1] jaxb-api (2.3.1)
[  23] [Active     ] [    1] file:/home/pi/rainy-felix/bundle/jaxb-runtime-2.3.2.jar
[  24] [Active     ] [    1] java driver for mh-z19b - intelligent infrared co2 module (0.1.3)
[  25] [Active     ] [    1] A modern JSON library for Kotlin and Java (1.7.0)
[  26] [Active     ] [    1] MessagePack serializer implementation for Java (0.8.17)
[  27] [Active     ] [    1] Netty/Buffer (4.1.38.Final)
[  28] [Active     ] [    1] netty-channel-fsm-osgi (0.3.0)
[  29] [Active     ] [    1] Netty/Codec (4.1.38.Final)
[  30] [Active     ] [    1] Netty/Codec/HTTP (4.1.38.Final)
[  31] [Active     ] [    1] Netty/Common (4.1.38.Final)
[  32] [Active     ] [    1] Netty/Handler (4.1.38.Final)
[  33] [Active     ] [    1] Netty/Resolver (4.1.38.Final)
[  34] [Active     ] [    1] Netty/Transport (4.1.38.Final)
[  35] [Active     ] [    1] Apache Felix Shell Service (1.4.3)
[  36] [Active     ] [    1] Apache Felix Shell TUI (1.4.1)
[  37] [Active     ] [    1] Apache ServiceMix :: Bundles :: jsr305 (3.0.2.1)
[  38] [Active     ] [    1] Apache ServiceMix :: Bundles :: okhttp (3.14.1.1)
[  39] [Active     ] [    1] Apache ServiceMix :: Bundles :: okio (1.15.0.1)
[  40] [Active     ] [    1] Apache ServiceMix :: Bundles :: retrofit (2.5.0.2)
[  41] [Active     ] [    1] Paho MQTT Client (1.2.1)
[  42] [Active     ] [    1] OSGi LogService implemented over SLF4J (1.7.26)
[  43] [Active     ] [    1] Pi4J :: Java Library (Core) (1.2)
[  44] [Active     ] [    1] java driver for ppd42ns - dust sensor module (0.1.7)
[  45] [Active     ] [    1] osgi activator of rainy - a tiny tool for iot data collection and monitoring (0.1.8)
[  46] [Active     ] [    1] OPC-UA bundle of rainy - a tiny tool for iot data collection and monitoring (0.1.5)
[  47] [Active     ] [    1] rainy - a tiny tool for iot data collection and monitoring (0.1.24)
[  48] [Active     ] [    1] java driver for rcwl-0516 - microwave presence sensor module (0.1.1)
[  49] [Active     ] [    1] sdk-client (0.3.4)
[  50] [Active     ] [    1] sdk-core (0.3.4)
[  51] [Active     ] [    1] slf4j-api (1.7.26)
[  52] [Resolved   ] [    1] slf4j-jdk14 (1.7.26)
[  53] [Active     ] [    1] stack-client (0.3.4)
[  54] [Active     ] [    1] stack-core (0.3.4)
[  55] [Active     ] [    1] strict-machine-osgi (0.1.0)
-> 
```

- stop  
  Stop rainy as follows.
```
-> stop 0
```

<a id="output_sensor_value"></a>

### Output sensor values to the log file

If you change the following logging level of `conf/logging.properties` to `INFO --> FINE` and restart rainy, the sensor values will be output to the log file `logs/rainy.log.0`.
```
#io.github.s5uishida.level=INFO
-->
io.github.s5uishida.level=FINE
```
The sample of the output log is as follows.
```
[/dev/ttyAMA0] co2:1110 
[I2C_1_76] temperature:20.84 
[I2C_1_76] humidity:52.66211 
[I2C_1_76] pressure:1012.5222 
[I2C_1_23] optical:82.5 
[hci0] B0:B4:48:ED:B6:04 obj:15.8125 amb:21.75 
[hci0] B0:B4:48:ED:B6:04 humidity:56.78711 
[hci0] B0:B4:48:ED:B6:04 pressure:1012.96 
[hci0] B0:B4:48:ED:B6:04 optical:84.84 
[hci0] B0:B4:48:ED:B6:04 gyr[x]:-8.888245 
[hci0] B0:B4:48:ED:B6:04 gyr[y]:-7.5759883 
[hci0] B0:B4:48:ED:B6:04 gyr[z]:-0.06866455 
[hci0] B0:B4:48:ED:B6:04 acc[x]:0.010375977 
[hci0] B0:B4:48:ED:B6:04 acc[y]:-0.0040283203 
[hci0] B0:B4:48:ED:B6:04 acc[z]:0.24835205 
[hci0] B0:B4:48:ED:B6:04 mag[x]:183.0 
[hci0] B0:B4:48:ED:B6:04 mag[y]:403.0 
[hci0] B0:B4:48:ED:B6:04 mag[z]:-192.0 
[GPIO_10] pcs:4248.701 ugm3:6.6253257
[GPIO_18] detect:true
[GPIO_19] detect:true
```
In order to reduce writing to the SD card, it is usually recommended to set it to `INFO`.

<a id="check_database"></a>

### Check the database name for each device created in InfluxDB

Check from the log file `logs/rainy.log.0`. The following is an example. Note that InfluxDB will not do anything if the database already exists.
```
execute - CREATE DATABASE RP3B_01__dev_ttyAMA0               <-- MH-Z19B
...
execute - CREATE DATABASE RP3B_01_I2C_1_76                   <-- BME280
...
execute - CREATE DATABASE RP3B_01_I2C_1_23                   <-- BH1750FVI
...
execute - CREATE DATABASE B0_B4_48_B9_92_86                  <-- CC2650
...
execute - CREATE DATABASE B0_B4_48_ED_B6_04                  <-- CC2650
...
execute - CREATE DATABASE milo_digitalpetri_com_62541_milo   <-- Public Demo Server of Eclipse Milo
...
execute - CREATE DATABASE RP3B_01_GPIO_10                    <-- PPD42NS
...
execute - CREATE DATABASE RP3B_01_GPIO_18                    <-- RCWL-0516
...
execute - CREATE DATABASE RP3B_01_GPIO_19                    <-- HC-SR501
```
These database names are required for the visualization tools Grafana and Chronograf to connect to InfluxDB.

<a id="create_dashboards"></a>

## Create dashboards with visualization tools

Visualization tools can be connected to InfluxDB to monitor time-series sensor data.

<a id="case_grafana"></a>

### Case Grafana

Please refer to Getting started of [Grafana site](https://grafana.com/docs/) for how to use Grafana.  
The following figure is a sample image of a dashboard.

<img src="./images/rainy_grafana_1.png" title="./images/rainy_grafana_1.png" width=800px></img>

The following figure is a sample graph of Magnetometer using [Plotly](https://grafana.com/grafana/plugins/natel-plotly-panel) panel.

<img src="./images/rainy_grafana_1_1.png" title="./images/rainy_grafana_1_1.png" width=800px></img>

The following figure is a sample monitoring image with environmental information on the floor map using [ImageIt](https://grafana.com/grafana/plugins/pierosavi-imageit-panel) panel.

<img src="./images/rainy_floor_env_1.png" title="./images/rainy_floor_env_1.png" width=800px></img>

<a id="case_chronograf"></a>

### Case Chronograf

Please refer to Getting started of [Chronograf site](https://docs.influxdata.com/chronograf/v1.7/) for how to use Chronograf.  
The following figure is a sample image of a dashboard.

<img src="./images/rainy_chronograf_1.png" title="./images/rainy_chronograf_1.png" width=800px></img>

The following figure is a sample dashboard for the following NodeIDs on OPC-UA Public Demo Server of Eclipse Milo.  
- `1,VendorServerInfo/ProcessCpuLoad`
- `1,VendorServerInfo/SystemCpuLoad`
- `1,VendorServerInfo/UsedMemory`
- `2,Dynamic/RandomDouble`
- `2,Dynamic/RandomFloat`
- `2,Dynamic/RandomInt32`
- `2,Dynamic/RandomInt64`

<img src="./images/rainy_opcua_1.png" title="./images/rainy_opcua_1.png" width=800px></img>

The upper displays `1,VendorServerInfo/ProcessCpuLoad`, `1,VendorServerInfo/SystemCpuLoad` and `1, VendorServerInfo/UsedMemory`. The middle displays `2,Dynamic/RandomDouble` and `2,Dynamic/RandomFloat` superimposed and the lower displays `2,Dynamic/RandomInt32` and `2,Dynamic/RandomInt64` as time series graphs.

If you put data into InfluxDB which is a time series DB, you can easily create a dashboard using Grafana or Chronograf.

<a id="check_mqtt_data"></a>

## Check the data sent to MQTT broker

Check the data sent to the MQTT broker using the MQTT client command as follows:
```
# mosquitto_sub -d -t rainy/B0_B4_48_ED_B6_04
Client mosqsub|2095-u1804 sending CONNECT
Client mosqsub|2095-u1804 received CONNACK
Client mosqsub|2095-u1804 sending SUBSCRIBE (Mid: 1, Topic: rainy/B0_B4_48_ED_B6_04, QoS: 0)
Client mosqsub|2095-u1804 received SUBACK
Subscribed (mid: 1): 0
Client mosqsub|2095-u1804 received PUBLISH (d0, q0, r0, m0, 'rainy/B0_B4_48_ED_B6_04', ... (670 bytes))
{"deviceID":"B0:B4:48:ED:B6:04","clientID":"RP3B-01","samplingDate":"2019-08-09 12:56:00.009","samplingTimeMillis":1565351760009,"samplingTimeNanos":0,"firmwareVersion":"1.30 (May 23 2016)","batteryLevel":{"value":72,"unit":"%"},"objectTemperature":{"value":27.46875,"unit":"deg C"},"ambientTemperature":{"value":32.03125,"unit":"deg C"},"humidity":{"value":34.61914,"unit":"%"},"pressure":{"value":1009.16,"unit":"hPa"},"optical":{"value":203.84,"unit":"lux"},"gyroscope":{"x":-10.589599,"y":-7.8887935,"z":-2.281189,"unit":"deg/s"},"accelerometer":{"x":-0.029785156,"y":-0.06347656,"z":1.1887207,"unit":"G"},"magnetometer":{"x":138.0,"y":125.0,"z":-199.0,"unit":"uT"}}
```

<a id="limitations"></a>

## Limitations

- Only one Bluetooth adapter can be used.
- Only a few CC2650 (Bluetooth devices) can be used at the same time. (Restriction of Bluetooth chip)
- When the connection with CC2650 is lost, it may not recover automatically.
- This tool uses Pi4J for BME280/BH1750FVI/PPD42NS/RCWL-0516/HC-SR501, so these devices can only be used with Raspberry Pi series (arm). Therefore, the the features of these devices of this tool does not work on amd64 Linux machines.
- To use Pi4J 1.2's I2C functionality, sun.misc.SharedSecrets.class is required, but this class can only be used up to Java 8 and cannot be used since Java 9. Therefore, Java 8 is required to use BME280 and BH1750FVI.
- Depending on the combination of the number of monitored items of OPC-UA servers and the publishing interval, the load on InfluxDB may become too large.

<a id="bundle_list"></a>

## Bundle list

The following bundles I created follow the MIT license.
- [bluetooth-scanner 0.1.4](https://github.com/s5uishida/bluetooth-scanner)
- [cc2650-driver 0.1.2](https://github.com/s5uishida/cc2650-driver)
- [bme280-driver 0.1.3](https://github.com/s5uishida/bme280-driver)
- [bh1750fvi-driver 0.1.2](https://github.com/s5uishida/bh1750fvi-driver)
- [mh-z19b-driver 0.1.3](https://github.com/s5uishida/mh-z19b-driver)
- [ppd42ns-driver 0.1.7](https://github.com/s5uishida/ppd42ns-driver)
- [rcwl-0516-driver 0.1.1](https://github.com/s5uishida/rcwl-0516-driver)
- [hc-sr501-driver 0.1.1](https://github.com/s5uishida/hc-sr501-driver)
- [rainy-opcua 0.1.5](https://github.com/s5uishida/rainy-opcua)
- [rainy-activator 0.1.8](https://github.com/s5uishida/rainy-activator)
- [rainy 0.1.24](https://github.com/s5uishida/rainy)

Please check each license for the following bundles used in addition to these.
- [SLF4J 1.7.26](https://www.slf4j.org/)
- [Apache Commons Lang 3.9](https://commons.apache.org/proper/commons-lang/)
- [dbus-java-osgi 3.2.1-SNAPSHOT](https://github.com/hypfvieh/dbus-java)
- [bluez-dbus-osgi 0.1.2-SNAPSHOT](https://github.com/s5uishida/bluez-dbus-osgi)
- [cron4j-osgi 2.2.5](https://github.com/s5uishida/cron4j-osgi)
- [influxdb-java-osgi 2.15.0](https://github.com/s5uishida/influxdb-java-osgi)
- [msgpack-core-osgi 0.8.17](https://github.com/s5uishida/msgpack-core-osgi)
- [moshi-osgi 1.7.0](https://github.com/s5uishida/moshi-osgi)
- [Jackson 2.9.9](https://github.com/FasterXML/jackson) [annotations](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations/2.9.9), [core](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core/2.9.9), [databind](https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind/2.9.9.1)
- [Apache Felix 6.0.3](https://felix.apache.org/)
- [Apache Felix Shell 1.4.3](https://felix.apache.org/documentation/subprojects/apache-felix-shell.html)
- [Apache Felix Shell TUI 1.4.1](https://felix.apache.org/documentation/subprojects/apache-felix-shell-tui.html)
- [JSR 305 3.0.2](https://mvnrepository.com/artifact/org.apache.servicemix.bundles/org.apache.servicemix.bundles.jsr305/3.0.2_1)
- [Okio 1.15.0](https://mvnrepository.com/artifact/org.apache.servicemix.bundles/org.apache.servicemix.bundles.okio/1.15.0_1)
- [OkHttp 3.14.1](https://mvnrepository.com/artifact/org.apache.servicemix.bundles/org.apache.servicemix.bundles.okhttp/3.14.1_1)
- [Retrofit 2.5.0](https://mvnrepository.com/artifact/org.apache.servicemix.bundles/org.apache.servicemix.bundles.retrofit/2.5.0_2)
- [Eclipse Paho Client Mqttv3 1.2.1](https://mvnrepository.com/artifact/org.eclipse.paho/org.eclipse.paho.client.mqttv3/1.2.1)
- [jSerialComm 2.5.1](https://mvnrepository.com/artifact/com.fazecast/jSerialComm/2.5.1)
- [Netty 4.1.38](https://netty.io/index.html) netty-buffer-4.1.38.Final.jar, netty-codec-4.1.38.Final.jar, netty-codec-http-4.1.38.Final.jar, netty-common-4.1.38.Final.jar, netty-handler-4.1.38.Final.jar, netty-resolver-4.1.38.Final.jar, netty-transport-4.1.38.Final.jar
- [JAXB API 2.3.1](https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api/2.3.1)
- [JAXB Runtime 2.3.2](https://mvnrepository.com/artifact/org.glassfish.jaxb/jaxb-runtime/2.3.2)
- [JavaBeans Activation Framework (JAF) 1.2.0](https://mvnrepository.com/artifact/com.sun.activation/javax.activation/1.2.0)
- [strict-machine-osgi 0.1](https://github.com/s5uishida/strict-machine-osgi)
- [netty-channel-fsm-osgi 0.3](https://github.com/s5uishida/netty-channel-fsm-osgi)
- [bsd-parser-core 0.3.4](https://mvnrepository.com/artifact/org.eclipse.milo/bsd-parser-core/0.3.4)
- [bsd-parser-gson 0.3.4](https://mvnrepository.com/artifact/org.eclipse.milo/bsd-parser-gson/0.3.4)
- [stack-core 0.3.4](https://mvnrepository.com/artifact/org.eclipse.milo/stack-core/0.3.4)
- [stack-client 0.3.4](https://mvnrepository.com/artifact/org.eclipse.milo/stack-client/0.3.4)
- [sdk-core 0.3.4](https://mvnrepository.com/artifact/org.eclipse.milo/sdk-core/0.3.4)
- [sdk-client 0.3.4](https://mvnrepository.com/artifact/org.eclipse.milo/sdk-client/0.3.4)
- [Gson 2.8.5](https://mvnrepository.com/artifact/com.google.code.gson/gson/2.8.5)
- [Bouncy Castle PKIX, CMS, EAC, TSP, PKCS, OCSP, CMP, and CRMF APIs 1.62](https://www.bouncycastle.org/download/bcpkix-jdk15on-162.jar)
- [Bouncy Castle Provider 1.62](https://www.bouncycastle.org/download/bcprov-jdk15on-162.jar)
- [Guava: Google Core Libraries for Java 26.0](https://repo1.maven.org/maven2/com/google/guava/guava/26.0-jre/guava-26.0-jre.jar)
- [Pi4J 1.2 (pi4j-core.jar)](https://github.com/s5uishida/pi4j-core-osgi)

I would like to thank the authors of these very useful codes, and all the contributors.

<a id="ps"></a>

## P.S.

If Raspberry Pi 4B (4GB memory model), InfluxDB and Grafana may be able to run together with rainy in enough resources.
In this case, from sensor data collection to monitoring, it may be possible to run with one RP4.
