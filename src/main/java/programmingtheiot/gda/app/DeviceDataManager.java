/**
 * This class is part of the Programming the Internet of Things project.
 * <p>
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.*;

import programmingtheiot.gda.connection.*;
import programmingtheiot.gda.system.SystemPerformanceManager;
import redis.clients.jedis.JedisPubSub;

/**
 * Shell representation of class for student implementation.
 *
 */
public class DeviceDataManager extends JedisPubSub implements IDataMessageListener {
    // static

    private static final Logger _Logger =
            Logger.getLogger(DeviceDataManager.class.getName());

    // private var's

    private boolean enableMqttClient = true;
    private boolean enableCoapServer = false;
    private boolean enableCloudClient = false;
    private boolean enableSmtpClient = false;
    private boolean enablePersistenceClient = false;

    private IPubSubClient mqttClient = null;
    private IPubSubClient cloudClient = null;
    private IPersistenceClient persistenceClient = null;
    private IRequestResponseClient smtpClient = null;
    private CoapServerGateway coapServer = null;

    private ConfigUtil configUtil = ConfigUtil.getInstance();
    private DataUtil dataUtil = DataUtil.getInstance();

    private float humiditySimFloor;
    private float humiditySimCeiling;

    private RedisPersistenceAdapter redisClient = null;
    //	private MqttClientConnector mqttClient;
    private SystemPerformanceManager sysPerfManager;

    private Thread subCDAThread;
    private int cloudQos = configUtil.getInteger(ConfigConst.CLOUD_GATEWAY_SERVICE,ConfigConst.DEFAULT_QOS_KEY);
    private int mqttQos = configUtil.getInteger(ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.DEFAULT_QOS_KEY);

    // constructors
    public DeviceDataManager() {
        super();
        this.enableMqttClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);
        this.enableCoapServer = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);
        this.enableCloudClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
        this.enableSmtpClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_SMTP_CLIENT_KEY);
        this.enablePersistenceClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);

        this.humiditySimFloor = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE, ConfigConst.HUMIDITY_SIM_FLOOR_KEY);
        this.humiditySimCeiling = configUtil.getFloat(ConfigConst.GATEWAY_DEVICE, ConfigConst.HUMIDITY_SIM_CEILING_KEY);

        _Logger.log(Level.INFO, "Get config enablePersistenceClient: " + this.enablePersistenceClient);
        this.sysPerfManager = new SystemPerformanceManager();
        this.sysPerfManager.setDataMessageListener(this);
        initConnections();
    }

    public DeviceDataManager(
            boolean enableMqttClient,
            boolean enableCoapServer,
            boolean enableCloudClient,
            boolean enableSmtpClient,
            boolean enablePersistenceClient) {
        super();
        this.enableMqttClient = enableMqttClient;
        this.enableCoapServer = enableCoapServer;
        this.enableCloudClient = enableCloudClient;
        this.enableSmtpClient = enableSmtpClient;
        this.enablePersistenceClient = enablePersistenceClient;

        this.humiditySimFloor = configUtil.getInteger(ConfigConst.GATEWAY_DEVICE, ConfigConst.HUMIDITY_SIM_FLOOR_KEY);
        this.humiditySimCeiling = configUtil.getInteger(ConfigConst.GATEWAY_DEVICE, ConfigConst.HUMIDITY_SIM_CEILING_KEY);

        _Logger.log(Level.INFO, "Get config enablePersistenceClient: " + this.enablePersistenceClient);
        this.sysPerfManager = new SystemPerformanceManager();
        this.sysPerfManager.setDataMessageListener(this);
        initConnections();
    }


    // public methods

    // implement methods for JedisPubSub begin
    public void onMessage(String channel, String message) {
        _Logger.log(Level.INFO, String.format("onMessage callback on channel: %s", channel));
        this.handleIncomingMessage(ResourceNameEnum.getEnumFromValue(channel),message);
    }

    public void onUnsubscribe(String channel, int subscribedChannels) {
        _Logger.log(Level.INFO, String.format("Unsubscribe channel: %s %s", subscribedChannels, channel));
    }
    // implement methods for JedisPubSub end

    // implement methods for IDataMessageListener begin

    /**
     * Handle all SensorData mgs arrived from CDA/GDA/Cloud
     * @param resourceName The enum representing the String resource name.
     * @param data The SensorData data - this will usually be the decoded payload
     * from a connection using either MQTT or CoAP.
     * @return if success to handle the message
     */
    @Override
    public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data) {
        _Logger.log(Level.INFO, "Handling a SensorMessage: " + data.toString());
        // Redis
        if (this.enablePersistenceClient) {
            this.redisClient.storeData(resourceName.getResourceName(), ConfigConst.DEFAULT_QOS, data);
        }
        // process data and make reaction if necessary
        this.handleIncomingDataAnalysis(resourceName, data);
        // upstream SensorData to cloud
        this.handleUpstreamTransmission(resourceName, data);
        return true;
    }

    /**
     * Handle all ActuatorData msg arrived from CDA/GDA/Cloud
     * @param resourceName The enum representing the String resource name.
     * @param data The ActuatorData data - this will usually be the decoded payload
     * from a connection using either MQTT or CoAP.
     * @return if success to handle the message
     */
    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data) {
        if (data.isResponseFlagEnabled() && data.isResponse()) {
            _Logger.log(Level.INFO, "Handling a ActuatorCommandResponse: " + data.toString());
            if (this.enablePersistenceClient) {
                this.redisClient.storeData(resourceName.getResourceName(), ConfigConst.DEFAULT_QOS, data);
            }
            this.handleIncomingDataAnalysis(resourceName, data);
            if (data.hasErrorFlag()) {
                _Logger.log(Level.WARNING, String.format("Got an error response! code: %d, statusData: %s", data.getStatusCode(), data.getStateData()));
                this.handleUpstreamTransmission(resourceName, data);
            }
        } else {
            _Logger.log(Level.INFO, "Bypass an non-responsive ActuatorCommand.");
        }
        return true;
    }

    /**
     * Handle all SystemPerformanceData msg arrived from CDA/GDA
     * @param resourceName The enum representing the String resource name.
     * @param data The SystemPerformanceData data - this will usually be the decoded payload
     * from a connection using either MQTT or CoAP.
     * @return if success to handle the message
     */
    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data) {
        _Logger.log(Level.INFO, "Handling a SystemPerformanceMessage: " + data.toString());
        if (this.enablePersistenceClient) {
            this.redisClient.storeData(resourceName.getResourceName(), ConfigConst.DEFAULT_QOS, data);
        }
        this.handleIncomingDataAnalysis(resourceName, data);
        this.handleUpstreamTransmission(resourceName, data);
        return true;
    }

    /**
     * Handle all other msg except SensorData/ActuatorData/SystemPerformanceData msg
     * @param resourceName The enum representing the String resource name.
     * @param msg The String message - this will usually be the decoded payload
     * from a connection using either MQTT or CoAP.
     * @return if success to handle the message
     */
    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg) {
        _Logger.log(Level.INFO, String.format("Handling an IncomingMessage '%s' from '%s'", msg ,resourceName.getResourceName()));
        // only cloud connector will subscribe to CDA_ACTUATOR_CMD_RESOURCE
        if(resourceName == ResourceNameEnum.CLOUD_PRESSURE_LED_CMD_RESOURCE) {
            int val =(int) Double.parseDouble(msg);
            ActuatorData data = new ActuatorData();
            data.setActuatorType(ActuatorData.LED_DISPLAY_ACTUATOR_TYPE);
            if (val == 1) {
                data.setCommand(ActuatorData.COMMAND_ON);
                data.setStateData("Pressure too high");
            } else {
                data.setCommand(ActuatorData.COMMAND_OFF);
            }
            String cmdMsg = dataUtil.actuatorDataToJson(data);
            this.mqttClient.publishMessage(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, cmdMsg, mqttQos);
            return true;
        }
        // TODO: Handle msg from other channels
        return true;
    }
    // Implement methods for IDataMessageListener end

    public void startManager() {
        _Logger.log(Level.INFO, "DeviceDataManager starting...");

        if (this.enableMqttClient) {
            this.mqttClient.connectClient();
        }
        if (this.enableCoapServer) {
            this.coapServer.startServer();
        }
        if (this.enablePersistenceClient) {
            this.redisClient.connectClient();
            Runnable toRun = this.redisClient.subscribeToChannel(this,
                    new ResourceNameEnum[]{ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE,
                    ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE});
            this.subCDAThread = new Thread(toRun);
            this.subCDAThread.start();
        }
        if (this.enableCloudClient){
            this.cloudClient.connectClient();
        }
        this.sysPerfManager.startManager();
        _Logger.log(Level.INFO, "DeviceDataManager started.");
    }

    public void stopManager() {
        _Logger.log(Level.INFO, "DeviceDataManager stopping...");
        this.sysPerfManager.stopManager();
        if (this.enableMqttClient) {
            this.mqttClient.disconnectClient();
        }
        if (this.enableCoapServer) {
            this.coapServer.stopServer();
        }
        if (this.enablePersistenceClient) {
            this.redisClient.disconnectClient();
        }
        if (this.enableCloudClient){
            this.cloudClient.disconnectClient();
        }
        _Logger.log(Level.INFO, "DeviceDataManager stopped.");
    }


    // private methods

    /**
     * Initializes the enabled connections. This will NOT start them, but only create the
     * instances that will be used in the {@link #startManager() and #stopManager()) methods.
     *
     */
    private void initConnections() {
        if (this.enablePersistenceClient) {
            this.redisClient = new RedisPersistenceAdapter();
        }
        if (this.enableMqttClient) {
            this.mqttClient = new MqttClientConnector();
            this.mqttClient.setDataMessageListener(this);
        }
        if (this.enableCoapServer) {
            this.coapServer = new CoapServerGateway();
            this.coapServer.setDataMessageListener(this);
        }
        if (this.enableCloudClient){
            this.cloudClient = new CloudClientConnector();
            this.cloudClient.setDataMessageListener(this);
        }
    }

    private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, SensorData data) {
        _Logger.log(Level.FINE, String.format("Analyze an Incoming SensorData from %s.", resourceName.getResourceName()));
        if (resourceName == ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE) {
            String responseMsg = generateMeetHumidityThresholdResponse(data);
            if (responseMsg != null) {
                this.mqttClient.publishMessage(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, responseMsg, mqttQos);
            }
        }
    }

    private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, ActuatorData data) {
        _Logger.log(Level.FINE, String.format("Analyze an Incoming ActuatorData from %s.", resourceName.getResourceName()));
    }

    private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, SystemPerformanceData data) {
        _Logger.log(Level.FINE, String.format("Analyze an Incoming SystemPerformanceData from %s.", resourceName.getResourceName()));
        if (resourceName == ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE) {
            String responseMsg = generateMeetThresholdResponse(data);
            if (responseMsg != null) {
                this.mqttClient.publishMessage(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, responseMsg, mqttQos);
            }
        }
    }

    private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, SystemStateData data) {
        _Logger.log(Level.FINE, String.format("Analyze an Incoming SystemStateData from %s.", resourceName.getResourceName()));
    }

    private void handleUpstreamTransmission(ResourceNameEnum resourceName, SensorData data){
        if (enableCloudClient){
            // convert data
            String msg = this.dataUtil.sensorDataToJsonCloud(data);
            // upload data
            this.cloudClient.publishMessage(resourceName.getCloudEnum(), msg, cloudQos);
        }
    }
    private void handleUpstreamTransmission(ResourceNameEnum resourceName, ActuatorData data){
        _Logger.log(Level.FINE, String.format("Upstreaming an ActuatorData to %s", resourceName.getResourceName()));
        // convert data
        String msg = this.dataUtil.actuatorDataToJsonCloud(data);
        // upload data
        this.cloudClient.publishMessage(resourceName.getCloudEnum(), msg, cloudQos);
    }
    private void handleUpstreamTransmission(ResourceNameEnum resourceName, SystemPerformanceData data){
        if (enableCloudClient){
            // convert data
            String msg = this.dataUtil.systemPerformanceDataToJsonCloud(data);
            // upload data
            this.cloudClient.publishMessage(resourceName.getCloudEnum(), msg, cloudQos);
        }
    }
    private void handleUpstreamTransmission(ResourceNameEnum resourceName, SystemStateData data){
        _Logger.log(Level.FINE, String.format("Upstreaming a SystemStateData to %s", resourceName.getResourceName()));
    }

    private String generateMeetHumidityThresholdResponse(SensorData data) {
        String ret = null;
        ActuatorData cmd = new ActuatorData();
        if (data.getSensorType() == SensorData.HUMIDITY_SENSOR_TYPE) {
            if (data.getValue() < this.humiditySimFloor) {
                cmd.setCommand(ActuatorData.COMMAND_ON);
                cmd.setActuatorType(ActuatorData.HUMIDIFIER_ACTUATOR_TYPE);
                cmd.setStateData("Humidity too low");
                ret = this.dataUtil.actuatorDataToJson(cmd);
                return ret;
            }
            if (data.getValue() > this.humiditySimCeiling) {
                cmd.setCommand(ActuatorData.COMMAND_OFF);
                cmd.setActuatorType(ActuatorData.HUMIDIFIER_ACTUATOR_TYPE);
                cmd.setStateData("Humidity too high");
                ret = this.dataUtil.actuatorDataToJson(cmd);
                return ret;
            }
        }
        return null;
    }

    private String generateMeetThresholdResponse(SystemPerformanceData data) {
        String ret = null;
        // TODO: check meet threshold crossing
        // TODO: if meet, generate response cmd msg
        return ret;
    }
}
