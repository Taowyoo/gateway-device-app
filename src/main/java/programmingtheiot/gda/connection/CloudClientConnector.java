/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * Shell representation of class for student implementation.
 *
 */
public class CloudClientConnector implements ICloudClient,IPubSubClient
{
	// static
	private static final Logger _Logger =
		Logger.getLogger(CloudClientConnector.class.getName());
	
	// private var's
	private String topicPrefix = "";
	private MqttClientConnector mqttClient = null;
	private IDataMessageListener dataMsgListener = null;
	private int qosLevel;
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public CloudClientConnector()
	{

		ConfigUtil configUtil = ConfigUtil.getInstance();
		this.topicPrefix = configUtil.getProperty(ConfigConst.CLOUD_GATEWAY_SERVICE, ConfigConst.BASE_TOPIC_KEY);
		this.qosLevel = configUtil.getInteger(ConfigConst.CLOUD_GATEWAY_SERVICE, ConfigConst.DEFAULT_QOS_KEY);
		// Depending on the cloud service, the topic names may or may not begin with a "/", so this code
		// should be updated according to the cloud service provider's topic naming conventions
		if (topicPrefix == null) {
			topicPrefix = "/";
		} else {
			if (! topicPrefix.endsWith("/")) {
				topicPrefix += "/";
			}
		}
	}
	
	
	// public methods
	
	@Override
	public boolean connectClient()
	{
		if (this.mqttClient == null) {
			this.mqttClient = new MqttClientConnector(true);
		}
		if (this.mqttClient.isConnected()){
			return false;
		}
		return this.mqttClient.connectClient();
	}

	@Override
	public boolean disconnectClient()
	{
		if (this.mqttClient != null) {
			return this.mqttClient.disconnectClient();
		}

		return false;
	}

	@Override
	public boolean publishMessage(ResourceNameEnum topicName, String msg, int qos) {
		String topic = this.createTopicName(topicName);
		return this.mqttClient.publishMessage(topic,msg.getBytes(),qos);
	}

	@Override
	public boolean subscribeToTopic(ResourceNameEnum topicName, int qos) {
		return this.subscribeToEdgeEvents(topicName, qos);
	}

	@Override
	public boolean unsubscribeFromTopic(ResourceNameEnum topicName) {
		return this.unsubscribeFromEdgeEvents(topicName);
	}

	@Override
	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SensorData data) {
		_Logger.info("Sending an Edge SensorData to cloud topic: " + resource.getResourceName());
		if (resource != null && data != null) {
			String payload = DataUtil.getInstance().sensorDataToJson(data);

			return publishMessageToCloud(resource, data.getName(), payload);
		}

		return false;
	}

	@Override
	public boolean sendEdgeDataToCloud(ResourceNameEnum resource, SystemPerformanceData data) {
		_Logger.info("Send an Edge SystemPerformanceData to cloud topic: " + resource.getResourceName());
		if (resource != null && data != null) {
			SensorData cpuData = new SensorData();
			cpuData.setName(ConfigConst.CPU_UTIL_NAME);
			cpuData.setValue(data.getCpuUtilization());

			boolean cpuDataSuccess = sendEdgeDataToCloud(resource, cpuData);

			if (! cpuDataSuccess) {
				_Logger.warning("Failed to send CPU utilization data to cloud service.");
			}

			SensorData memData = new SensorData();
			memData.setName(ConfigConst.MEM_UTIL_NAME);
			memData.setValue(data.getMemoryUtilization());

			boolean memDataSuccess = sendEdgeDataToCloud(resource, memData);

			if (! memDataSuccess) {
				_Logger.warning("Failed to send memory utilization data to cloud service.");
			}

			return (cpuDataSuccess == memDataSuccess);
		}

		return false;
	}


	public boolean subscribeToEdgeEvents(ResourceNameEnum resource) {
		return this.subscribeToEdgeEvents(resource, this.qosLevel);
	}

	@Override
	public boolean subscribeToEdgeEvents(ResourceNameEnum resource, int qos) {
		_Logger.info("Subscribe to cloud topic: " + resource.getResourceName());
		boolean success = false;

		String topicName = null;

		if (isMqttClientConnected()) {
			topicName = createTopicName(resource);

			this.mqttClient.subscribeToTopic(topicName, qos);

			success = true;
		} else {
			_Logger.warning("Subscription methods only available for MQTT. No MQTT connection to broker. Ignoring. Topic: " + topicName);
		}

		return success;
	}


	@Override
	public boolean unsubscribeFromEdgeEvents(ResourceNameEnum resource) {
		_Logger.info("Unsubscribe to cloud topic: " + resource.getResourceName());
		boolean success = false;

		String topicName = null;

		if (isMqttClientConnected()) {
			topicName = createTopicName(resource);

			this.mqttClient.unsubscribeFromTopic(topicName);

			success = true;
		} else {
			_Logger.warning("Unsubscribe method only available for MQTT. No MQTT connection to broker. Ignoring. Topic: " + topicName);
		}

		return success;
	}

	public boolean isConnected()
	{
		return this.isMqttClientConnected();
	}

	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		if(listener == null){
			_Logger.warning("Got a null pointer IDataMessageListener!");
			return false;
		}
		this.dataMsgListener = listener;
		return true;
	}

	// private methods
	private String createTopicName(ResourceNameEnum resource)
	{
		return (this.topicPrefix + resource.getResourceName().replace('/','-')).toLowerCase();
	}

	private boolean publishMessageToCloud(ResourceNameEnum resource, String itemName, String payload)
	{
		String topicName = createTopicName(resource) + "-" + itemName;

		try {
			_Logger.finest("Publishing payload value(s) to Ubidots: " + topicName);

			this.mqttClient.publishMessage(topicName, payload.getBytes(), this.qosLevel);

			return true;
		} catch (Exception e) {
			_Logger.warning("Failed to publish message to Ubidots: " + topicName);
		}

		return false;
	}
	private boolean isMqttClientConnected() {
		if(this.mqttClient != null){
			return this.mqttClient.isConnected();
		}
		return false;
	}
}
