/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(MqttClientConnector.class.getName());
	
	// params
	private String host;
	private String protocol;
	private int port;
	private int brokerKeepAlive;
	private  int qos;
	private String clientID;
	private MemoryPersistence persistence;
	private  MqttConnectOptions connOpts;
	private String brokerAddr;
	// variables
	private MqttClient mqttClient;
	private IDataMessageListener dataMessageListener;
	private Map<ResourceNameEnum,Integer> subscribedTopics;
	// constructors

	/**
	 * MqttClientConnector Constructor
	 *
	 * Init MqttClientConnector, load configurations from config file
	 */
	public MqttClientConnector()
	{
		super();
		ConfigUtil configUtil = ConfigUtil.getInstance();

		this.host =
				configUtil.getProperty(
						ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);

		this.protocol = ConfigConst.DEFAULT_MQTT_PROTOCOL;

		this.port =
				configUtil.getInteger(
						ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT);

		this.qos =
				configUtil.getInteger(
						ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.DEFAULT_QOS_KEY, ConfigConst.DEFAULT_QOS);

		this.brokerKeepAlive =
				configUtil.getInteger(
						ConfigConst.MQTT_GATEWAY_SERVICE, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);

		// paho Java client requires a client ID
		this.clientID = MqttClient.generateClientId();

		// these are specific to the MQTT connection which will be used during connect
		this.persistence = new MemoryPersistence();
		this.connOpts = new MqttConnectOptions();

		this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
		this.connOpts.setCleanSession(false);
		this.connOpts.setAutomaticReconnect(true);

		// NOTE: URL does not have a protocol handler for "tcp",
		// so we need to construct the URL manually
		this.brokerAddr = this.protocol + "://" + this.host + ":" + this.port;

		// store topic subscribed for reconnection
		this.subscribedTopics = new HashMap<>();
	}
	
	
	// public methods

	// methods for IPubSubClient
	@Override
	public boolean connectClient() {
		try {
			if (this.mqttClient == null) {
				_Logger.info(String.format("Creating MQTT Client, broker: '%s', client id: '%s'...", this.brokerAddr, this.clientID));
				this.mqttClient = new MqttClient(this.brokerAddr, this.clientID, this.persistence);
				this.mqttClient.setCallback(this);
			}

			if (!this.mqttClient.isConnected()) {
				_Logger.info("Connecting to broker...");
				this.mqttClient.connect(this.connOpts);
				return true;
			}
		} catch (MqttException e) {
			_Logger.warning("Fail to connect to broker: " + e.toString());
			return false;
		}
		_Logger.warning("MQTT Client has already connected to broker! No need to reconnect.");
		return false;
	}

	@Override
	public boolean disconnectClient()
	{
		if (this.mqttClient != null) {
			if (this.mqttClient.isConnected()) {
				try {
					_Logger.info("Disconnecting from broker...");
					this.mqttClient.disconnect();
				} catch (MqttException e) {
					_Logger.warning("Fail to disconnect from broker: " + e.toString());
					return  false;
				}
				return true;
			}
		}
		_Logger.warning("MQTT Client has already disconnected from broker! No need to disconnect again.");
		return false;
	}

	public boolean isConnected()
	{
		if (this.mqttClient != null) {
			return this.mqttClient.isConnected();
		}
		return false;
	}
	
	@Override
	public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos)
	{
		if (topicName == null){
			_Logger.warning("Got a null topic to publish!");
			return false;
		}
		if (qos < 0 || qos > 2){
			_Logger.warning(String.format("Got an invalid QoS %d, change to use default QoS %d!", qos, ConfigConst.DEFAULT_QOS));
			qos = ConfigConst.DEFAULT_QOS;
		}
		MqttMessage mqttMsg = new MqttMessage();
		mqttMsg.setPayload(msg.getBytes());
		mqttMsg.setQos(qos);
		try {
			_Logger.info(String.format("Publishing msg '%s' to topic '%s' with QoS %d ...", msg, topicName.name(), qos));
			this.mqttClient.publish(topicName.name(),mqttMsg);
			return true;
		} catch (MqttException e) {
			_Logger.warning(String.format("Fail to publish msg '%s' to topic '%s' with QoS %d, exception:\n%s", msg, topicName.name(), qos, e.toString()));
		}
		return false;
	}

	@Override
	public boolean subscribeToTopic(ResourceNameEnum topicName, int qos)
	{
		if (qos < 0 || qos > 2){
			_Logger.warning(String.format("Got an invalid QoS %d, change to use default QoS %d!", qos, ConfigConst.DEFAULT_QOS));
			qos = ConfigConst.DEFAULT_QOS;
		}
		try {
			_Logger.info(String.format("Subscribing to topic '%s' with QoS %d ...", topicName.name(), qos));
			this.mqttClient.subscribe(topicName.name(), qos);
			this.subscribedTopics.put(topicName,qos);
			return true;
		} catch (MqttException e) {
			_Logger.warning(String.format("Fail to subscribe to topic '%s' with QoS %d, exception:\n%s", topicName.name(), qos, e.toString()));
		}
		return false;
	}

	@Override
	public boolean unsubscribeFromTopic(ResourceNameEnum topicName)
	{
		try {
			_Logger.info(String.format("Unsubscribing to topic '%s'...", topicName.name()));
			this.mqttClient.unsubscribe(topicName.name());
			this.subscribedTopics.remove(topicName);
			return true;
		} catch (MqttException e) {
			_Logger.warning(String.format("Fail to unsubscribe to topic '%s', exception:\n%s", topicName.name(), e.toString()));
		}
		return false;
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		this.dataMessageListener = listener;
		return true;
	}
	
	// callbacks for MqttCallback
	
	@Override
	public void connectComplete(boolean reconnect, String serverURI)
	{
		_Logger.info(String.format("Complete to %s to broker '%s'", reconnect ? "reconnect" : "connect", serverURI));
	}

	@Override
	public void connectionLost(Throwable t)
	{
		_Logger.warning("Lost connection from broker: " + t.getMessage() + ", try to reconnect...");
		if(!connectClient()){
			_Logger.warning(" Fail to reconnect to broker!");
		}
		else{
			if (!subscribedTopics.isEmpty()){
				subscribedTopics.forEach((k,v) -> this.subscribeToTopic(k, v));
			}
		}
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		_Logger.info(String.format("Complete to delivery to broker, response: %s", token.getResponse().toString()));
	}
	
	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception
	{
		String msgStr = new String(msg.getPayload());
		_Logger.info(String.format("Receive a message from topic '%s': %s", topic, msgStr));
//		ResourceNameEnum resource = ResourceNameEnum.getEnumFromValue(topic);
//		switch(resource){
//			// TODO: convert and handle incoming messages
//			case CDA_SENSOR_MSG_RESOURCE:
//				break;
//			case CDA_ACTUATOR_CMD_RESOURCE:
//				break;
//			case CDA_MGMT_STATUS_CMD_RESOURCE:
//				break;
//			case CDA_MGMT_STATUS_MSG_RESOURCE:
//				break;
//			case GDA_MGMT_STATUS_CMD_RESOURCE:
//				break;
//			case GDA_MGMT_STATUS_MSG_RESOURCE:
//				break;
//			default:
//		}
	}

	
	// private methods
	
	/**
	 * Called by the constructor to set the MQTT client parameters to be used for the connection.
	 * 
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initClientParameters(String configSectionName)
	{
		// TODO: implement this
	}
	
	/**
	 * Called by {@link #initClientParameters(String)} to load credentials.
	 * 
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initCredentialConnectionParameters(String configSectionName)
	{
		// TODO: implement this
	}
	
	/**
	 * Called by {@link #initClientParameters(String)} to enable encryption.
	 * 
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initSecureConnectionParameters(String configSectionName)
	{
		// TODO: implement this
	}
}
