package programmingtheiot.part03.integration.connection;

import org.junit.Before;
import org.junit.Test;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.gda.connection.MqttClientConnector;

import java.util.logging.Logger;

import static org.junit.Assert.*;

public class MqttClientPerformanceTest {
    // static
    private static final Logger _Logger =
            Logger.getLogger(MqttClientConnectorTest.class.getName());
    public static final int MAX_TEST_RUNS = 10000;


    // member var's
    private MqttClientConnector mqttClient = null;


    // test setup methods
    @Before
    public void setUp() throws Exception
    {
        this.mqttClient = new MqttClientConnector();
    }

    @Test
    public void testConnectAndDisconnect()
    {
        long startMillis = System.currentTimeMillis();

        assertTrue(this.mqttClient.connectClient());
        assertTrue(this.mqttClient.disconnectClient());

        long endMillis = System.currentTimeMillis();
        long elapsedMillis = endMillis - startMillis;

        _Logger.info("Connect and Disconnect: " + elapsedMillis + " ms");
    }

    @Test
    public void testPublishQoS0()
    {
        execTestPublish(MAX_TEST_RUNS, 0);
    }

    @Test
    public void testPublishQoS1()
    {
        execTestPublish(MAX_TEST_RUNS, 1);
    }

    @Test
    public void testPublishQoS2()
    {
        execTestPublish(MAX_TEST_RUNS, 2);
    }

    private void execTestPublish(int maxTestRuns, int qos)
    {
        assertTrue(this.mqttClient.connectClient());

        SensorData sensorData = new SensorData();

        String payload = DataUtil.getInstance().sensorDataToJson(sensorData);

        long startMillis = System.currentTimeMillis();

        for (int sequenceNo = 0; sequenceNo < maxTestRuns; sequenceNo++) {
            this.mqttClient.publishMessage(ResourceNameEnum.CDA_MGMT_STATUS_CMD_RESOURCE, payload, qos);
        }

        long endMillis = System.currentTimeMillis();
        long elapsedMillis = endMillis - startMillis;

        assertTrue(this.mqttClient.disconnectClient());

        _Logger.info("Publish message - QoS " + qos + " [" + maxTestRuns + "]: " + elapsedMillis + " ms");
    }
}
