/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonParseException;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.*;

import programmingtheiot.gda.connection.CoapServerGateway;
import programmingtheiot.gda.connection.IPersistenceClient;
import programmingtheiot.gda.connection.IPubSubClient;
import programmingtheiot.gda.connection.IRequestResponseClient;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;
import programmingtheiot.gda.system.SystemPerformanceManager;
import redis.clients.jedis.JedisPubSub;

/**
 * Shell representation of class for student implementation.
 *
 */
public class DeviceDataManager extends JedisPubSub implements IDataMessageListener
{
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

	private RedisPersistenceAdapter redisClient = null;
	private SystemPerformanceManager sysPerfManager;
//	private int cloudQos = configUtil.getInteger(ConfigConst.CLOUD_GATEWAY_SERVICE,ConfigConst.DEFAULT_QOS_KEY);
//	private int mqttQos = configUtil.getInteger(ConfigConst.MQTT_GATEWAY_SERVICE,ConfigConst.DEFAULT_QOS_KEY);

	// constructors
	public DeviceDataManager()
	{
		super();
		this.enableMqttClient  = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_MQTT_CLIENT_KEY);
		this.enableCoapServer  = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_COAP_SERVER_KEY);
		this.enableCloudClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_CLOUD_CLIENT_KEY);
		this.enableSmtpClient  = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_SMTP_CLIENT_KEY);
		this.enablePersistenceClient = configUtil.getBoolean(ConfigConst.GATEWAY_DEVICE, ConfigConst.ENABLE_PERSISTENCE_CLIENT_KEY);
		this.sysPerfManager = new SystemPerformanceManager(10);
		initConnections();
	}
	
	public DeviceDataManager(
		boolean enableMqttClient,
		boolean enableCoapServer,
		boolean enableCloudClient,
		boolean enableSmtpClient,
		boolean enablePersistenceClient)
	{
		super();
		this.enableMqttClient = enableMqttClient;
		this.enableCoapServer = enableCoapServer;
		this.enableCloudClient = enableCloudClient;
		this.enableSmtpClient = enableSmtpClient;
		this.enablePersistenceClient = enablePersistenceClient;
		this.sysPerfManager = new SystemPerformanceManager();
		initConnections();
	}
	
	
	// public methods

	// implement methods for JedisPubSub begin
	public void onMessage(String channel, String message) {
		if(channel == ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE.getResourceName()){
			try {
				SensorData data = this.dataUtil.jsonToSensorData(message);
				handleSensorMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, data);
			}
			catch (JsonParseException ex) {
				_Logger.log(Level.WARNING,"Fail to convert subscribed message to SensorData!");
			}
		}
		else if(channel == ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE.getResourceName()){
			try {
				ActuatorData data = this.dataUtil.jsonToActuatorData(message);
				handleActuatorCommandResponse(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, data);
			}
			catch (JsonParseException ex) {
				_Logger.log(Level.WARNING,"Fail to convert subscribed message to SensorData!");
			}
		}
		else if(channel == ResourceNameEnum.CDA_MGMT_STATUS_MSG_RESOURCE.getResourceName()){
			try {
				SystemPerformanceData data = this.dataUtil.jsonToSystemPerformanceData(message);
				handleSystemPerformanceMessage(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, data);
			}
			catch (JsonParseException ex) {
				_Logger.log(Level.WARNING,"Fail to convert subscribed message to SensorData!");
			}
		}
		else if(channel == ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE.getResourceName()){
			handleIncomingMessage(ResourceNameEnum.GDA_MGMT_STATUS_MSG_RESOURCE, message);
		}
		else{
			_Logger.log(Level.WARNING, String.format("Got a msg from invalid channel, channel: %s msg: %s", channel, message));
		}
	}

	// implement methods for JedisPubSub end

	// implement methods for IDataMessageListener begin
	@Override
	public boolean handleActuatorCommandResponse(ResourceNameEnum resourceName, ActuatorData data)
	{
		if(data.isResponseFlagEnabled() && data.isResponse()){
			_Logger.log(Level.INFO,"Handling a ActuatorCommandResponse: " + data.toString());
			if (this.enablePersistenceClient){
				this.redisClient.storeData(resourceName.getResourceName(),ConfigConst.DEFAULT_QOS, data);
			}
			if (data.getStatusCode() == ActuatorData.DEFAULT_STATUS){
				return true;
			}
			else{
				_Logger.log(Level.WARNING, String.format("Got an error response! code: %d, statusData: %s", data.getStatusCode(), data.getStateData()));
				// TODO: handle error
				return false;
			}
		}
		else{
			_Logger.log(Level.INFO,"Bypass an non-responsive ActuatorCommand.");
			return true;
		}
	}

	@Override
	public boolean handleIncomingMessage(ResourceNameEnum resourceName, String msg)
	{
		_Logger.log(Level.INFO,"Handling an IncomingMessage: " + msg);
		try {
			ActuatorData data = this.dataUtil.jsonToActuatorData(msg);
			this.handleIncomingDataAnalysis(resourceName, data);
		}
		catch (JsonParseException ex){
			_Logger.log(Level.WARNING,"Fail to convert IncomingMessage to ActuatorData!");
			_Logger.log(Level.INFO,"Try to convert IncomingMessage to SystemStateData!");
			try {
				SystemStateData data = this.dataUtil.jsonToSystemStateData(msg);
				this.handleIncomingDataAnalysis(resourceName, data);
			}
			catch (JsonParseException ex2){
				_Logger.log(Level.WARNING,"Fail to convert IncomingMessage to SystemStateData: " + ex2.toString());
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean handleSensorMessage(ResourceNameEnum resourceName, SensorData data)
	{
		_Logger.log(Level.INFO,"Handling a SensorMessage: " + data.toString());
		if (this.enablePersistenceClient){
			if (this.enablePersistenceClient){
				this.redisClient.storeData(resourceName.getResourceName(),ConfigConst.DEFAULT_QOS, data);
			}
			String jsonData = this.dataUtil.sensorDataToJson(data);
			if (jsonData != null){
				this.handleUpstreamTransmission(resourceName, jsonData);
				return true;
			}
			else{
				_Logger.log(Level.WARNING,"Fail to convert SensorData to Json string!");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean handleSystemPerformanceMessage(ResourceNameEnum resourceName, SystemPerformanceData data)
	{
		_Logger.log(Level.INFO,"Handling a SystemPerformanceMessage: " + data.toString());
		if (this.enablePersistenceClient){
			if (this.enablePersistenceClient){
				this.redisClient.storeData(resourceName.getResourceName(),ConfigConst.DEFAULT_QOS, data);
			}
			String jsonData = this.dataUtil.systemPerformanceDataToJson(data);
			if (jsonData != null){
				this.handleUpstreamTransmission(resourceName, jsonData);
				return true;
			}
			else{
				_Logger.log(Level.WARNING,"Fail to convert SensorData to Json string!");
				return false;
			}
		}
		return true;
	}
	// Implement methods for IDataMessageListener end
	
	public void startManager()
	{
		_Logger.log(Level.INFO, "DeviceDataManager starting...");
		this.sysPerfManager.startManager();
		if (this.enablePersistenceClient){
			this.redisClient.connectClient();
			this.redisClient.subscribeToChannel(this,ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE);
			this.redisClient.subscribeToChannel(this,ResourceNameEnum.CDA_ACTUATOR_CMD_RESOURCE);
		}
		_Logger.log(Level.INFO, "DeviceDataManager started.");
	}

	public void stopManager()
	{
		_Logger.log(Level.INFO, "DeviceDataManager stopping...");
		this.sysPerfManager.stopManager();
		if (this.enablePersistenceClient){
			this.redisClient.disconnectClient();
		}
		_Logger.log(Level.INFO, "DeviceDataManager stopped.");
	}

	
	// private methods
	
	/**
	 * Initializes the enabled connections. This will NOT start them, but only create the
	 * instances that will be used in the {@link #startManager() and #stopManager()) methods.
	 * 
	 */
	private void initConnections()
	{
		if (this.enablePersistenceClient){
			this.redisClient = new RedisPersistenceAdapter();
		}
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, ActuatorData data){
		_Logger.log(Level.FINE, String.format("Analyze an Incoming ActuatorData from %s.", resourceName.getResourceName()));
	}

	private void handleIncomingDataAnalysis(ResourceNameEnum resourceName, SystemStateData data){
		_Logger.log(Level.FINE, String.format("Analyze an Incoming SystemStateData from %s.", resourceName.getResourceName()));
	}

	private void handleUpstreamTransmission(ResourceNameEnum resourceName, String msg){
		_Logger.log(Level.FINE, String.format("Upstream a msg: %s from %s.", msg, resourceName.getResourceName()));
		// TODO: publish to the cloud service. We'll revisit this in Part 03.
	}
}
