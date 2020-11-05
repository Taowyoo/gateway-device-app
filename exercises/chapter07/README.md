# Gateway Device Application (Connected Devices)

## Lab Module 07

### Description

#### What does your implementation do? 

1. Create `MqttClientConnector` class as a client for GDA to communicate with broker.
2. Integrate `MqttClientConnector` into `DeviceDataManager`.

#### How does your implementation work?

1. Implement `MqttClientConnector` class:
   1. Implement all necessary callback functions for MQTT client.
   2. Implement methods for `IPubSubClient` interface.
2. Add `MqttClientConnector` instance and related logic code in `DeviceDataManager` class:
   1. Add instance initialization, config setup code for `MqttClientConnector`.
   2. Add related `SensorData`, `SystemPerformanceData`, `ActuationData` receiving or forwarding code for `MqttClientConnector`.


### Code Repository and Branch

URL: https://github.com/NU-CSYE6530-Fall2020/gateway-device-app-Taowyoo/tree/alpha001

### UML Design Diagram(s)

Here is latest class diagram of current code:
![Class Diagram](./../../doc/UML/Lab07.svg)

### Unit Tests Executed

- All unit tests in part01
- All unit tests in part02

### Integration Tests Executed

-  src\test\java\programmingtheiot\part03\integration\connection\MqttClientConnectorTest