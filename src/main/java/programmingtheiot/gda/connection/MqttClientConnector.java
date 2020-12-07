/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;



import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonParseException;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import javax.net.ssl.SSLSocketFactory;
import programmingtheiot.common.SimpleCertManagementUtil;
import programmingtheiot.data.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Simple Mqtt Client implemented by using paho library
 * 
 */
public class MqttClientConnector implements IPubSubClient, MqttCallbackExtended
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(MqttClientConnector.class.getName());
	// Maximum number of threads in thread pool
	static final int MAX_T = 1;

	// params
	private String host;
	private String protocol;
	private int port;
	private int brokerKeepAlive;
	private int qos;
	private String clientID;
	private MemoryPersistence persistence;
	private MqttConnectOptions connOpts;
	private String brokerAddr;
	private boolean enableEncryption;
	private String pemFileName;
	private boolean useCloudGatewayConfig = false;
	// variables
	private MqttClient mqttClient;
	protected IDataMessageListener dataMessageListener;
	private Map<String,Integer> subscribedTopics;
	private final ConfigUtil configUtil = ConfigUtil.getInstance();
	// constructors

	/**
	 * MqttClientConnector Constructor
	 *
	 * Init MqttClientConnector, load configurations from config file
	 */
	public MqttClientConnector()
	{
		this(false);
	}

	/**
	 * MqttClientConnector Constructor with choice of whether use cloud config
	 * @param useCloudGatewayConfig Whether use Cloud config
	 */
	public MqttClientConnector(boolean useCloudGatewayConfig)
	{
		super();

		this.useCloudGatewayConfig = useCloudGatewayConfig;

		if (useCloudGatewayConfig) {
			initClientParameters(ConfigConst.CLOUD_GATEWAY_SERVICE);
		} else {
			initClientParameters(ConfigConst.MQTT_GATEWAY_SERVICE);
		}
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
		return this.publishMessage(topicName.getResourceName(),msg.getBytes(),qos);
	}

	@Override
	public boolean subscribeToTopic(ResourceNameEnum topicName, int qos)
	{
		return this.subscribeToTopic(topicName.getResourceName(),qos);
	}

	@Override
	public boolean unsubscribeFromTopic(ResourceNameEnum topicName)
	{
		return this.unsubscribeFromTopic(topicName.getResourceName());
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		if (listener != null){
			this.dataMessageListener = listener;
			return true;
		}
		return false;
	}
	
	// callbacks for MqttCallback
	
	@Override
	public void connectComplete(boolean reconnect, String serverURI)
	{
		_Logger.info(String.format("[Callback] Complete to %s to broker '%s'", reconnect ? "reconnect" : "connect", serverURI));
		subscribeTopics(this.qos);
	}

	@Override
	public void connectionLost(Throwable t)
	{
		_Logger.warning("[Callback] Lost connection from broker: " + t.getMessage() + ", try to reconnect...");
		this.subscribedTopics.clear();
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken token)
	{
		_Logger.info(String.format("[Callback] Complete to delivery to broker, response: %s", token.getResponse().toString()));
	}

	/**
	 * Callback for processing arrived message
	 * Convert topic to ResourceNameEnum object, then use dataMessageListener to call the corresponding function
	 * @param topic Topic where mqtt message comes from
	 * @param msg MqttMessage instance of message
	 * @throws Exception Any exception meets when processing the msg
	 */
	@Override
	public void messageArrived(String topic, MqttMessage msg) throws Exception
	{
		// Get msg string
		String msgStr = new String(msg.getPayload());
		_Logger.info(String.format("[Callback] Receive a message from topic '%s':\n%s", topic, msgStr));
		// Get Resource Name from topic
		ResourceNameEnum resourceNameEnum = ResourceNameEnum.getEnumFromValue(topic);
		if (resourceNameEnum == null){
			_Logger.warning(String.format("[Callback] Invalid topic: '%s' !", topic));
			return;
		}
		DataUtil dataUtil = DataUtil.getInstance();
		Runnable handleMsgTask = new Runnable() {
			@Override
			public void run() {
				switch (resourceNameEnum) {
					case CDA_SENSOR_MSG_RESOURCE:
						try {
							SensorData data = dataUtil.jsonToSensorData(msgStr);
							dataMessageListener.handleSensorMessage(resourceNameEnum, data);
						} catch (JsonParseException ex) {
							_Logger.log(Level.WARNING,String.format("Fail to convert message to SensorData: %s", ex.toString()));
							return;
						}
						break;
					case CDA_ACTUATOR_RESPONSE_RESOURCE:
						try {
							ActuatorData data = dataUtil.jsonToActuatorData(msgStr);
							dataMessageListener.handleActuatorCommandResponse(resourceNameEnum, data);
						} catch (JsonParseException ex) {
							_Logger.log(Level.WARNING,String.format("Fail to convert message to ActuatorData: %s", ex.toString()));
							return;
						}
						break;
					case CDA_SYSTEM_PERF_MSG_RESOURCE:
					case GDA_SYSTEM_PERF_MSG_RESOURCE:
						try {
							SystemPerformanceData data = dataUtil.jsonToSystemPerformanceData(msgStr);
							dataMessageListener.handleSystemPerformanceMessage(resourceNameEnum, data);
						} catch (JsonParseException ex) {
							_Logger.log(Level.WARNING,String.format("Fail to convert message to SystemPerformanceData: %s", ex.toString()));
							return;
						}
						break;
					case CDA_MGMT_STATUS_CMD_RESOURCE:
					case GDA_MGMT_STATUS_CMD_RESOURCE:
					case CLOUD_GDA_DEVICE:
					case CLOUD_CDA_DEVICE:
					case CDA_MGMT_STATUS_MSG_RESOURCE:
					case GDA_MGMT_STATUS_MSG_RESOURCE:
					case CDA_ACTUATOR_CMD_RESOURCE:
					case CLOUD_PRESSURE_LED_CMD_RESOURCE:
						dataMessageListener.handleIncomingMessage(resourceNameEnum,msgStr);
						break;
					default:
						_Logger.log(Level.WARNING, String.format("Got a msg from invalid channel, channel: %s", resourceNameEnum.getResourceName()));
						return;
				}
			}
		};
//		this.incomingMsgHandlerThreadPool.execute(handleMsgTask);
		Thread thread = new Thread(handleMsgTask);
		thread.start();
	}

	// protected methods
	protected boolean publishMessage(String topic, byte[] payload, int qos)
	{
		if (topic == null){
			_Logger.warning("Got a null topic to publish!");
			return false;
		}
		if (qos < 0 || qos > 2){
			_Logger.warning(String.format("Got an invalid QoS %d, change to use default QoS %d!", qos, ConfigConst.DEFAULT_QOS));
			qos = ConfigConst.DEFAULT_QOS;
		}
		MqttMessage message = new MqttMessage(payload);
		message.setQos(qos);
		try {
			_Logger.info(String.format("Publishing to topic '%s' with QoS %d, msg:\n%s", topic, qos, new String((payload))));
			this.mqttClient.publish(topic,message);
			return true;
		} catch (MqttPersistenceException e) {
			_Logger.warning("Persistence exception thrown when publishing to topic: " + topic );
		} catch (MqttException e) {
			_Logger.warning("MqttException exception thrown when publishing to topic : " + e.toString());
		}
		return false;
	}

	protected boolean subscribeToTopic(String topic, int qos)
	{
		if (qos < 0 || qos > 2){
			_Logger.warning(String.format("Got an invalid QoS %d, change to use default QoS %d!", qos, ConfigConst.DEFAULT_QOS));
			qos = ConfigConst.DEFAULT_QOS;
		}
		try {
			_Logger.info(String.format("Subscribing to topic '%s' with QoS %d ...", topic, qos));
			this.mqttClient.subscribe(topic, qos);
			this.subscribedTopics.put(topic,qos);
			return true;
		} catch (MqttException e) {
			_Logger.warning("Failed to subscribe to topic: " + topic);
		}
		return false;
	}

	protected boolean unsubscribeFromTopic(String topic)
	{
		try {
			_Logger.info(String.format("Unsubscribing to topic '%s'...", topic));
			this.mqttClient.unsubscribe(topic);
			this.subscribedTopics.remove(topic);
			return true;
		} catch (MqttException e) {
			_Logger.warning(String.format("Fail to unsubscribe to topic '%s', exception:\n%s", topic, e.toString()));
		}
		return false;
	}

	/**
	 * Helper methods to subscribe to topics
	 * @param qos QoS value
	 */
	protected void subscribeTopics(int qos) {
		this.subscribeToTopic(ResourceNameEnum.CDA_ACTUATOR_RESPONSE_RESOURCE, qos);
		this.subscribeToTopic(ResourceNameEnum.CDA_SENSOR_MSG_RESOURCE, qos);
		this.subscribeToTopic(ResourceNameEnum.CDA_SYSTEM_PERF_MSG_RESOURCE, qos);
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
		this.protocol = ConfigConst.DEFAULT_MQTT_PROTOCOL;
		this.host = configUtil.getProperty(configSectionName, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);
		this.port =	configUtil.getInteger(configSectionName, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_MQTT_PORT);
		this.enableEncryption = configUtil.getBoolean(configSectionName, ConfigConst.ENABLE_CRYPT_KEY);
		this.pemFileName = configUtil.getProperty(configSectionName, ConfigConst.CERT_FILE_KEY);
		this.brokerKeepAlive =
				configUtil.getInteger(configSectionName, ConfigConst.KEEP_ALIVE_KEY, ConfigConst.DEFAULT_KEEP_ALIVE);
		this.qos = configUtil.getInteger(configSectionName, ConfigConst.DEFAULT_QOS_KEY, ConfigConst.DEFAULT_QOS);

		// paho Java client requires a client ID
		this.clientID = MqttClient.generateClientId();

		// these are specific to the MQTT connection which will be used during connect
		this.persistence = new MemoryPersistence();
		this.connOpts = new MqttConnectOptions();

		this.connOpts.setKeepAliveInterval(this.brokerKeepAlive);
		this.connOpts.setCleanSession(false);
		this.connOpts.setAutomaticReconnect(true);

		// if encryption is enabled, try to load and apply the cert(s)
		if (this.enableEncryption) {
			initSecureConnectionParameters(configSectionName);
		}

		// if there's a credential file, try to load and apply them
		if (configUtil.hasProperty(configSectionName, ConfigConst.CRED_FILE_KEY)) {
			initCredentialConnectionParameters(configSectionName);
		}

		// NOTE: URL does not have a protocol handler for "tcp",
		// so we need to construct the URL manually
		this.brokerAddr = this.protocol + "://" + this.host + ":" + this.port;

		// store topic subscribed for reconnection
		this.subscribedTopics = new HashMap<>();
	}

	/**
	 * Called by {@link #initClientParameters(String)} to load credentials.
	 *
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initCredentialConnectionParameters(String configSectionName)
	{
		String userToken = configUtil.getCredentials(configSectionName).getProperty("userToken");
		_Logger.info("Load userToken from credFile: " + userToken);
		this.connOpts.setUserName(userToken);
	}

	/**
	 * Called by {@link #initClientParameters(String)} to enable encryption.
	 *
	 * @param configSectionName The name of the configuration section to use for
	 * the MQTT client configuration parameters.
	 */
	private void initSecureConnectionParameters(String configSectionName)
	{
		try {
			_Logger.info("Configuring TLS...");

			if (this.pemFileName != null) {
				File file = new File(this.pemFileName);

				if (file.exists()) {
					_Logger.info("PEM file valid. Using secure connection: " + this.pemFileName);
				} else {
					this.enableEncryption = false;

					_Logger.log(Level.WARNING, "PEM file invalid. Using insecure connection: " + pemFileName, new Exception());

					return;
				}
			}

			SSLSocketFactory sslFactory =
					SimpleCertManagementUtil.getInstance().loadCertificate(this.pemFileName);
			this.connOpts.setSocketFactory(sslFactory);

			// override current config parameters
			this.port =
					configUtil.getInteger(
							configSectionName, ConfigConst.SECURE_PORT_KEY, ConfigConst.DEFAULT_MQTT_SECURE_PORT);

			this.protocol = ConfigConst.DEFAULT_MQTT_SECURE_PROTOCOL;

			_Logger.info("TLS enabled.");
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to initialize secure MQTT connection. Using insecure connection.", e);

			this.enableEncryption = false;
		}
	}

}
