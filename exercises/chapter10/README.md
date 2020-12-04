# Gateway Device Application (Connected Devices)

## Lab Module 10

### Description

What does your implementation do? 

1. Update `MqttClientConnector` to support encrypted connections to the broker
2. Update `MqttClientConnector` to send received messages to an `IDataMessageListener` instance which is `DeviceDataManager`
3. Update `DeviceDataManager` to handle analyze messages from the CDA and take an appropriate action
4. Update sensor and actuator data containers to set the appropriate device name
5. Complete tests for testing the performance of MQTT and CoAP clients
6. Run Integrate test to test GDA trigger of floor and ceiling of humidity


How does your implementation work?

1. Add `initSecureConnectionParameters` and `initClientParameters` method to `MqttClientConnector` and update the defualt constructor
2. Add three `subscribeToTopic` call to `connectComplete` to subscribe CDA topics as soon as connection established
3. Update `handleIncomingMessage`, `handleSensorMessage` and `handleIncomingDataAnalysis` methods in `DeviceDataManager`
4. Add `CoapClientPerformanceTest` and `MqttClientPerformanceTest` to test the performance of MQTT and CoAP clients
5. Add `DeviceDataManagerIntegrationTest` to test with CDA of triggerring an actuator cmd to CDA

### Code Repository and Branch

URL: https://github.com/NU-CSYE6530-Fall2020/gateway-device-app-Taowyoo/tree/alpha001

### UML Design Diagram(s)

Here is latest class diagram of current code:
![Class Diagram](./../../doc/uml/Lab10.svg)

### Unit Tests Executed

- ActuatorDataTest 
- SensorDataTest
- SystemPerformanceDataTest
- SystemStateDataTest
- 

### Integration Tests Executed

- MqttClientConnectorTest
- DataIntegrationTest
- DeviceDataManagerIntegrationTest
- CoapClientPerformanceTest
- MqttClientPerformanceTest

### GDA MQTT Client Performance Test Results

#### Summary

1. Speed
   1. QoS 0 > QoS 1 > QoS 2
   2. QoS 1 is slower than QoS 0 by 1.91%
   3. QoS 2 is slower than QoS 0 by 5.11%

2. The connect / disconnect cost about 305 ms

#### Log 

##### QoS 0

```log
Dec 03, 2020 11:30:31 PM programmingtheiot.gda.connection.MqttClientConnector connectClient
INFO: Creating MQTT Client, broker: 'tcp://localhost:1883', client id: 'paho87583448753449'...
Dec 03, 2020 11:30:31 PM programmingtheiot.gda.connection.MqttClientConnector connectClient
INFO: Connecting to broker...
Dec 03, 2020 11:30:32 PM programmingtheiot.gda.connection.MqttClientConnector connectComplete
INFO: [Callback] Complete to connect to broker 'tcp://localhost:1883'
Dec 03, 2020 11:30:33 PM programmingtheiot.gda.connection.MqttClientConnector disconnectClient
INFO: Disconnecting from broker...
Dec 03, 2020 11:30:33 PM programmingtheiot.part03.integration.connection.MqttClientPerformanceTest execTestPublish
INFO: Publish message - QoS 0 [10000]: 1096 ms
```

##### QoS 1

```log
Dec 03, 2020 11:30:33 PM programmingtheiot.gda.connection.MqttClientConnector connectClient
INFO: Creating MQTT Client, broker: 'tcp://localhost:1883', client id: 'paho87585290395167'...
Dec 03, 2020 11:30:33 PM programmingtheiot.gda.connection.MqttClientConnector connectClient
INFO: Connecting to broker...
Dec 03, 2020 11:30:33 PM programmingtheiot.gda.connection.MqttClientConnector connectComplete
INFO: [Callback] Complete to connect to broker 'tcp://localhost:1883'
Dec 03, 2020 11:30:34 PM programmingtheiot.gda.connection.MqttClientConnector disconnectClient
INFO: Disconnecting from broker...
Dec 03, 2020 11:30:34 PM programmingtheiot.part03.integration.connection.MqttClientPerformanceTest execTestPublish
INFO: Publish message - QoS 1 [10000]: 1117 ms
```

##### QoS 2

```log
Dec 03, 2020 11:30:34 PM programmingtheiot.gda.connection.MqttClientConnector connectClient
INFO: Creating MQTT Client, broker: 'tcp://localhost:1883', client id: 'paho87586720951958'...
Dec 03, 2020 11:30:34 PM programmingtheiot.gda.connection.MqttClientConnector connectClient
INFO: Connecting to broker...
Dec 03, 2020 11:30:35 PM programmingtheiot.gda.connection.MqttClientConnector connectComplete
INFO: [Callback] Complete to connect to broker 'tcp://localhost:1883'
Dec 03, 2020 11:30:36 PM programmingtheiot.gda.connection.MqttClientConnector disconnectClient
INFO: Disconnecting from broker...
Dec 03, 2020 11:30:36 PM programmingtheiot.part03.integration.connection.MqttClientPerformanceTest execTestPublish
INFO: Publish message - QoS 2 [10000]: 1152 ms
```

##### Connect and disconnect

```log
Dec 03, 2020 11:30:36 PM programmingtheiot.gda.connection.MqttClientConnector connectClient
INFO: Creating MQTT Client, broker: 'tcp://localhost:1883', client id: 'paho87588182960918'...
Dec 03, 2020 11:30:36 PM programmingtheiot.gda.connection.MqttClientConnector connectClient
INFO: Connecting to broker...
Dec 03, 2020 11:30:36 PM programmingtheiot.gda.connection.MqttClientConnector disconnectClient
INFO: Disconnecting from broker...
Dec 03, 2020 11:30:36 PM programmingtheiot.gda.connection.MqttClientConnector connectComplete
INFO: [Callback] Complete to connect to broker 'tcp://localhost:1883'
Dec 03, 2020 11:30:36 PM programmingtheiot.part03.integration.connection.MqttClientPerformanceTest testConnectAndDisconnect
INFO: Connect and Disconnect: 305 ms
```

Collect the elapsed time value for each request type.
Copy / paste the logged results from the test execution for each test in your README.md under the "GDA CoAP Client Performance Test Results" section.
For the CON and NON tests, include the percentage difference between them, with the NON test as the baseline.
Which ran fastest?
Which ran slowest?

### CDA CoAP Client Performance Test Results

#### Summary

1. Speed
   1. NON is faster then CON
   2. NON is faster then CON by about 152.20%

#### Log 

```log
Dec 03, 2020 11:47:39 PM org.eclipse.californium.core.network.config.NetworkConfig createStandardWithFile
INFO: Loading standard properties from file Californium.properties
Dec 03, 2020 11:47:39 PM programmingtheiot.gda.connection.CoapClientConnector initClient
INFO: Created client connection to server / resource: coap://localhost:5683
Dec 03, 2020 11:47:39 PM programmingtheiot.gda.connection.CoapClientConnector <init>
INFO: Using URL for server connection: coap://localhost:5683
Dec 03, 2020 11:47:39 PM org.eclipse.californium.core.network.CoapEndpoint start
INFO: Starting endpoint at 0.0.0.0/0.0.0.0:0
Dec 03, 2020 11:47:39 PM org.eclipse.californium.core.network.EndpointManager createDefaultEndpoint
INFO: Created implicit default endpoint 0.0.0.0/0.0.0.0:35770
Dec 03, 2020 11:47:44 PM programmingtheiot.part03.integration.connection.CoapClientPerformanceTest execTestPost
INFO: POST message - useCON true [10000]: 4917 ms
Dec 03, 2020 11:47:44 PM programmingtheiot.gda.connection.CoapClientConnector initClient
INFO: Created client connection to server / resource: coap://localhost:5683
Dec 03, 2020 11:47:44 PM programmingtheiot.gda.connection.CoapClientConnector <init>
INFO: Using URL for server connection: coap://localhost:5683
Dec 03, 2020 11:47:46 PM programmingtheiot.part03.integration.connection.CoapClientPerformanceTest execTestPost
INFO: POST message - useCON false [10000]: 1971 ms
```