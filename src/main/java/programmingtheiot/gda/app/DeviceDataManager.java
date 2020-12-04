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

import com.google.gson.JsonParseException;
import org.eclipse.paho.client.mqttv3.MqttException;
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
    @Override
    public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data) {
        if (data.isResponseFlagEnabled() && data.isResponse()) {
            _Logger.log(Level.INFO, "Handling a ActuatorCommandResponse: " + data.toString());
            if (this.enablePersistenceClient) {
                this.redisClient.storeData(resourceName.getResourceName(), ConfigConst.DEFAULT_QOS, data);
            }
            if (data.getStatusCode() == ActuatorData.DEFAULT_STATUS) {
                return true;
            } else {
                _Logger.log(Level.WARNING, String.format("Got an error response! code: %d, statusData: %s", data.getStatusCode(), data.getStateData()));
                // TODO: handle error
                return false;
            }
        } else {
            _Logger.log(Level.INFO, "Bypass an non-responsive ActuatorCommand.");
            return true;
        }
    }

    @Override
    public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg) {
        _Logger.log(Level.INFO, String.format("Handling an IncomingMessage '%s' from '%s'", msg ,resourceName.getResourceName()));
        switch (resourceName) {
            case CDA_SENSOR_MSG_RESOURCE:
                try {
                    SensorData data = this.dataUtil.jsonToSensorData(msg);
                    handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, data);
                } catch (JsonParseException ex) {
                    _Logger.log(Level.WARNING, String.format("Fail to convert subscribed message to SensorData! JsonParseException:\n%s\nmsg:\n%s", ex.toString(), msg));
                    return false;
                }
                break;
            case CDA_ACTUATOR_CMD_RESOURCE:
                try {
                    ActuatorData data = this.dataUtil.jsonToActuatorData(msg);
                    handleActuatorCommandResponse(ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE, data);
                    this.handleIncomingDataAnalysis(resourceName, data);
                } catch (JsonParseException ex) {
                    _Logger.log(Level.WARNING, String.format("Fail to convert subscribed message to ActuatorData! JsonParseException:\n%s\nmsg:\n%s", ex.toString(), msg));
                    return false;
                }
                break;
            case CDA_MGMT_STATUS_MSG_RESOURCE:
                try {
                    SystemPerformanceData data = this.dataUtil.jsonToSystemPerformanceData(msg);
                    handleSystemPerformanceMessage(ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE, data);
                } catch (JsonParseException ex) {
                    _Logger.log(Level.WARNING,String.format("Fail to convert subscribed message to SystemPerformanceData! JsonParseException:\n%s\nmsg:\n%s", ex.toString(), msg));
                    return false;
                }
                break;
            case CDA_MGMT_STATUS_CMD_RESOURCE:
                break;
            case GDA_MGMT_STATUS_MSG_RESOURCE:
                try {
                    SystemStateData data = this.dataUtil.jsonToSystemStateData(msg);
                    this.handleIncomingDataAnalysis(resourceName, data);
                } catch (JsonParseException ex2) {
                    _Logger.log(Level.WARNING, "Fail to convert IncomingMessage to SystemStateData: " + ex2.toString());
                    return false;
                }
                break;
            case GDA_MGMT_STATUS_CMD_RESOURCE:
                break;
            default:
                _Logger.log(Level.WARNING, String.format("Got a msg from invalid channel, channel: %s msg: %s", resourceName.getResourceName(), msg));
                return false;
        }
        return true;
    }

    @Override
    public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data) {
        _Logger.log(Level.INFO, "Handling a SensorMessage: " + data.toString());


        if (this.enablePersistenceClient) {
            this.redisClient.storeData(resourceName.getResourceName(), ConfigConst.DEFAULT_QOS, data);
        }
        String jsonData = this.dataUtil.sensorDataToJson(data);
        if (jsonData != null) {
            this.handleIncomingDataAnalysis(resourceName, data);
            this.handleUpstreamTransmission(resourceName, jsonData);
            return true;
        } else {
            _Logger.log(Level.WARNING, "Fail to convert SensorData to Json string!");
            return false;
        }
    }

    @Override
    public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data) {
        _Logger.log(Level.INFO, "Handling a SystemPerformanceMessage: " + data.toString());
        if (this.enablePersistenceClient) {
            this.redisClient.storeData(resourceName.getResourceName(), ConfigConst.DEFAULT_QOS, data);
        }
        String jsonData = this.dataUtil.systemPerformanceDataToJson(data);
        if (jsonData != null) {
            this.handleIncomingDataAnalysis(resourceName, data);
            this.handleUpstreamTransmission(resourceName, jsonData);
            return true;
        } else {
            _Logger.log(Level.WARNING, "Fail to convert SensorData to Json string!");
            return false;
        }
    }
    // Implement methods for IDataMessageListener end

    public void startManager() {
        _Logger.log(Level.INFO, "DeviceDataManager starting...");
        this.sysPerfManager.startManager();
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
            String responseMsg = generateMeetThresholdResponse(data);
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

    private void handleUpstreamTransmission(ResourceNameEnum resourceName, String msg) {
        _Logger.log(Level.FINE, String.format("Upstream a msg: %s from %s.", msg, resourceName.getResourceName()));
        switch (resourceName){
            case CDA_SENSOR_MSG_RESOURCE:
                this.cloudClient.publishMessage(ResourceNameEnum.CLOUD_CDA,msg,cloudQos);
                break;
            case CDA_ACTUATOR_CMD_RESOURCE:
                this.cloudClient.publishMessage(ResourceNameEnum.CLOUD_CDA,msg,cloudQos);
                break;
            case CDA_ACTUATOR_RESPONSE_RESOURCE:
                break;
            case CDA_MGMT_STATUS_MSG_RESOURCE:
                this.cloudClient.publishMessage(ResourceNameEnum.CLOUD_CDA,msg,cloudQos);
                break;
            case CDA_SYSTEM_PERF_MSG_RESOURCE:
                this.cloudClient.publishMessage(ResourceNameEnum.CLOUD_CDA,msg,cloudQos);
                break;
            case CDA_MGMT_STATUS_CMD_RESOURCE:
                this.cloudClient.publishMessage(ResourceNameEnum.CLOUD_CDA,msg,cloudQos);
                break;
            case GDA_MGMT_STATUS_MSG_RESOURCE:
                this.cloudClient.publishMessage(ResourceNameEnum.CLOUD_GDA,msg,cloudQos);
                break;
            case GDA_MGMT_STATUS_CMD_RESOURCE:
                this.cloudClient.publishMessage(ResourceNameEnum.CLOUD_GDA,msg,cloudQos);
                break;
            case GDA_SYSTEM_PERF_MSG_RESOURCE:
                this.cloudClient.publishMessage(ResourceNameEnum.CLOUD_GDA,msg,cloudQos);
                break;
            case CLOUD_GDA:
                break;
            case CLOUD_CDA:
                break;
        }
    }

    private String generateMeetThresholdResponse(SensorData data) {
        String ret = null;
        ActuatorData cmd = new ActuatorData();
        if (data.getSensorType() == SensorData.HUMIDITY_SENSOR_TYPE) {
            if (data.getValue() < this.humiditySimFloor) {
                cmd.setCommand(ActuatorData.COMMAND_ON);
                cmd.setActuatorType(ActuatorData.HUMIDIFIER_ACTUATOR_TYPE);
                cmd.setStateData("Humidity too low");
            }
            if (data.getValue() > this.humiditySimCeiling) {
                cmd.setCommand(ActuatorData.COMMAND_OFF);
                cmd.setActuatorType(ActuatorData.HUMIDIFIER_ACTUATOR_TYPE);
                cmd.setStateData("Humidity too high");
            }
        }
        ret = this.dataUtil.actuatorDataToJson(cmd);
        return ret;
    }

    private String generateMeetThresholdResponse(SystemPerformanceData data) {
        String ret = null;
        // TODO: check meet threshold crossing
        // TODO: if meet, generate response cmd msg
        return ret;
    }
}
