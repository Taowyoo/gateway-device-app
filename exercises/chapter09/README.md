# Constrained Device Application (Connected Devices)

## Lab Module 09

### Description

#### What does your implementation do? 

1. Update CoAP Client class for GDA.
2. Test and use Wireshark to capture CoAP packets.

#### How does your implementation work?

1. Implement the [`CoapClientConnector`](../../src/main/java/programmingtheiot/gda/connection/CoapClientConnector.java) class and [`GenericCoapResponseHandler`](../../src/main/java/programmingtheiot/gda/connection/handlers/GenericCoapResponseHandler.java) class as a CoAP client to send GET, PUT, POST, DELETE and DISCOVERY to CoAP server.
2. Update [`CoapClientConnectorTest`](../../src/test/java/programmingtheiot/part03/integration/connection/CoapClientConnectorTest.java) and validate code with these tests.
   Before run tests, run wireshark to capture CoAP packets.

### Code Repository and Branch

URL: https://github.com/NU-CSYE6530-Fall2020/gateway-device-app-Taowyoo/tree/alpha001

### UML Design Diagram(s)

Here is latest class diagram of current code:
![Class Diagram](./../../doc/uml/Lab09.svg)

### Unit Tests Executed

- All unit tests in part01
- All unit tests in part02

### Integration Tests Executed

- **/part02/integration/connection/PersistenceClientAdapterTest.java
- **/part03/integration/connection/MqttClientConnectorTest.java
- **/part03/integration/connection/CoapServerGatewayTest.java
- **/part03/integration/connection/CoapClientConnectorTest.java
- **/part02/integration/app/DeviceDataManagerNoCommsTest.java
- **/part01/integration/app/GatewayDeviceAppTest.java

### Captured CoAP Packets

#### DISCOVERY

```
Constrained Application Protocol, Confirmable, GET, MID:23112
    01.. .... = Version: 1
    ..00 .... = Type: Confirmable (0)
    .... 1000 = Token Length: 8
    Code: GET (1)
    Message ID: 23112
    Token: 6f2fbe84267ebca6
    Opt Name: #1: Uri-Host: localhost
    Opt Name: #2: Uri-Path: .well-known
    Opt Name: #3: Uri-Path: core
    [Uri-Path: coap://localhost/.well-known/core]
    [Response In: 238]
```
```
Constrained Application Protocol, Acknowledgement, 2.05 Content, MID:23112
    01.. .... = Version: 1
    ..10 .... = Type: Acknowledgement (2)
    .... 1000 = Token Length: 8
    Code: 2.05 Content (69)
    Message ID: 23112
    Token: 6f2fbe84267ebca6
    Opt Name: #1: Content-Format: application/link-format
    End of options marker: 255
    [Uri-Path: coap://localhost/.well-known/core]
    [Request In: 237]
    [Response Time: 0.014102282 seconds]
    Payload: Payload Content-Format: application/link-format, Length: 420
```

#### PUT

1. CON

```
Constrained Application Protocol, Confirmable, PUT, MID:23113
    01.. .... = Version: 1
    ..00 .... = Type: Confirmable (0)
    .... 1000 = Token Length: 8
    Code: PUT (3)
    Message ID: 23113
    Token: 662ef55d4bc98651
    Opt Name: #1: Uri-Host: localhost
    Opt Name: #2: Uri-Path: PIOT
    Opt Name: #3: Uri-Path: GatewayDevice
    Opt Name: #4: Uri-Path: MgmtStatusMsg
    Opt Name: #5: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 200
```
```
Constrained Application Protocol, Confirmable, 2.04 Changed, MID:57437
    01.. .... = Version: 1
    ..00 .... = Type: Confirmable (0)
    .... 1000 = Token Length: 8
    Code: 2.04 Changed (68)
    Message ID: 57437
    Token: 662ef55d4bc98651
    Opt Name: #1: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 23
```

2. NON

```
Constrained Application Protocol, Non-Confirmable, PUT, MID:23114
    01.. .... = Version: 1
    ..01 .... = Type: Non-Confirmable (1)
    .... 0010 = Token Length: 2
    Code: PUT (3)
    Message ID: 23114
    Token: e325
    Opt Name: #1: Uri-Host: localhost
    Opt Name: #2: Uri-Path: PIOT
    Opt Name: #3: Uri-Path: GatewayDevice
    Opt Name: #4: Uri-Path: MgmtStatusMsg
    Opt Name: #5: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 200
```
```
Constrained Application Protocol, Non-Confirmable, 2.04 Changed, MID:57438
    01.. .... = Version: 1
    ..01 .... = Type: Non-Confirmable (1)
    .... 0010 = Token Length: 2
    Code: 2.04 Changed (68)
    Message ID: 57438
    Token: e325
    Opt Name: #1: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 23
```

#### POST

1. CON

```
Constrained Application Protocol, Confirmable, POST, MID:23115
    01.. .... = Version: 1
    ..00 .... = Type: Confirmable (0)
    .... 0100 = Token Length: 4
    Code: POST (2)
    Message ID: 23115
    Token: 56abb114
    Opt Name: #1: Uri-Host: localhost
    Opt Name: #2: Uri-Path: PIOT
    Opt Name: #3: Uri-Path: GatewayDevice
    Opt Name: #4: Uri-Path: MgmtStatusMsg
    Opt Name: #5: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 200
```
```
Constrained Application Protocol, Confirmable, 2.01 Created, MID:57439
    01.. .... = Version: 1
    ..00 .... = Type: Confirmable (0)
    .... 0100 = Token Length: 4
    Code: 2.01 Created (65)
    Message ID: 57439
    Token: 56abb114
    Opt Name: #1: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 24
```

2. NON

```
Constrained Application Protocol, Non-Confirmable, POST, MID:23116
    01.. .... = Version: 1
    ..01 .... = Type: Non-Confirmable (1)
    .... 0101 = Token Length: 5
    Code: POST (2)
    Message ID: 23116
    Token: 6598593eb6
    Opt Name: #1: Uri-Host: localhost
    Opt Name: #2: Uri-Path: PIOT
    Opt Name: #3: Uri-Path: GatewayDevice
    Opt Name: #4: Uri-Path: MgmtStatusMsg
    Opt Name: #5: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 200

```
```
Constrained Application Protocol, Non-Confirmable, 2.01 Created, MID:57440
    01.. .... = Version: 1
    ..01 .... = Type: Non-Confirmable (1)
    .... 0101 = Token Length: 5
    Code: 2.01 Created (65)
    Message ID: 57440
    Token: 6598593eb6
    Opt Name: #1: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 24
```

#### GET

1. CON

```
Constrained Application Protocol, Confirmable, GET, MID:23117
    01.. .... = Version: 1
    ..00 .... = Type: Confirmable (0)
    .... 0111 = Token Length: 7
    Code: GET (1)
    Message ID: 23117
    Token: 3de5d42e0065db
    Opt Name: #1: Uri-Host: localhost
    Opt Name: #2: Uri-Path: PIOT
    Opt Name: #3: Uri-Path: GatewayDevice
    Opt Name: #4: Uri-Path: MgmtStatusMsg
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
```
```
Constrained Application Protocol, Confirmable, 2.03 Valid, MID:57441
    01.. .... = Version: 1
    ..00 .... = Type: Confirmable (0)
    .... 0111 = Token Length: 7
    Code: 2.03 Valid (67)
    Message ID: 57441
    Token: 3de5d42e0065db
    Opt Name: #1: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 23

```

2. NON

```
Constrained Application Protocol, Non-Confirmable, GET, MID:23118
    01.. .... = Version: 1
    ..01 .... = Type: Non-Confirmable (1)
    .... 0011 = Token Length: 3
    Code: GET (1)
    Message ID: 23118
    Token: 2b491f
    Opt Name: #1: Uri-Host: localhost
    Opt Name: #2: Uri-Path: PIOT
    Opt Name: #3: Uri-Path: GatewayDevice
    Opt Name: #4: Uri-Path: MgmtStatusMsg
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]

```
```
Constrained Application Protocol, Non-Confirmable, 2.03 Valid, MID:57442
    01.. .... = Version: 1
    ..01 .... = Type: Non-Confirmable (1)
    .... 0011 = Token Length: 3
    Code: 2.03 Valid (67)
    Message ID: 57442
    Token: 2b491f
    Opt Name: #1: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusMsg]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 23
```

#### DELETE

1. CON

```
Constrained Application Protocol, Confirmable, DELETE, MID:23119
    01.. .... = Version: 1
    ..00 .... = Type: Confirmable (0)
    .... 0110 = Token Length: 6
    Code: DELETE (4)
    Message ID: 23119
    Token: 9d52946a5385
    Opt Name: #1: Uri-Host: localhost
    Opt Name: #2: Uri-Path: PIOT
    Opt Name: #3: Uri-Path: GatewayDevice
    Opt Name: #4: Uri-Path: MgmtStatusCmd
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusCmd]
```
```
Constrained Application Protocol, Confirmable, 2.02 Deleted, MID:57443
    01.. .... = Version: 1
    ..00 .... = Type: Confirmable (0)
    .... 0110 = Token Length: 6
    Code: 2.02 Deleted (66)
    Message ID: 57443
    Token: 9d52946a5385
    Opt Name: #1: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusCmd]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 26
```

2. NON

```
Constrained Application Protocol, Non-Confirmable, DELETE, MID:23120
    01.. .... = Version: 1
    ..01 .... = Type: Non-Confirmable (1)
    .... 0111 = Token Length: 7
    Code: DELETE (4)
    Message ID: 23120
    Token: bc353ecd93894b
    Opt Name: #1: Uri-Host: localhost
    Opt Name: #2: Uri-Path: PIOT
    Opt Name: #3: Uri-Path: GatewayDevice
    Opt Name: #4: Uri-Path: MgmtStatusCmd
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusCmd]
```
```
Constrained Application Protocol, Non-Confirmable, 2.02 Deleted, MID:57444
    01.. .... = Version: 1
    ..01 .... = Type: Non-Confirmable (1)
    .... 0111 = Token Length: 7
    Code: 2.02 Deleted (66)
    Message ID: 57444
    Token: bc353ecd93894b
    Opt Name: #1: Content-Format: text/plain; charset=utf-8
    End of options marker: 255
    [Uri-Path: coap://localhost/PIOT/GatewayDevice/MgmtStatusCmd]
    Payload: Payload Content-Format: text/plain; charset=utf-8, Length: 26
```
