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

import com.google.gson.JsonParseException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

/**
 * Shell representation of class for student implementation.
 *
 */
public class CloudClientConnector extends MqttClientConnector implements ICloudClient,IPubSubClient
{
	// static
	private static final Logger _Logger =
		Logger.getLogger(CloudClientConnector.class.getName());
	// private var's
	private String topicPrefix = "";
	private int qosLevel;
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public CloudClientConnector()
	{
		super(true);
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
		if (isConnected()) {
			topicName = createTopicName(resource);
			topicName += "/lv";
			subscribeToTopic(topicName, qos);

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

		if (isConnected()) {
			topicName = createTopicName(resource);
			topicName += "/lv";
			unsubscribeFromTopic(topicName);
			success = true;
		} else {
			_Logger.warning("Unsubscribe method only available for MQTT. No MQTT connection to broker. Ignoring. Topic: " + topicName);
		}

		return success;
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
		// process raw topic
		topic = topic.substring(this.topicPrefix.length(), topic.length() - "/lv".length());
		_Logger.info(String.format("[Callback] Processed topic: '%s'", topic));
		// Get Resource Name from topic
		ResourceNameEnum resourceNameEnum = ResourceNameEnum.getEnumFromValue(topic);
		if (resourceNameEnum == null){
			_Logger.warning(String.format("[Callback] Invalid topic: '%s' !", topic));
			return;
		}
		Runnable handleMsgTask = new Runnable() {
			@Override
			public void run() {
				switch (resourceNameEnum) {
					case CLOUD_PRESSURE_LED_CMD_RESOURCE:
						dataMessageListener.handleIncomingMessage(resourceNameEnum,msgStr);
						break;
					default:
						_Logger.log(Level.WARNING, String.format("[Callback] Got a msg from invalid channel, channel: %s", resourceNameEnum.getResourceName()));
						return;
				}
			}
		};
		Thread thread = new Thread(handleMsgTask);
		thread.start();
	}

	@Override
	protected boolean publishMessage(String topic, byte[] payload, int qos) {
		return super.publishMessage(this.topicPrefix+topic, payload, qos);
	}

	// protected
	@Override
	protected void subscribeTopics(int qos) {
		if (isConnected()) {
			this.subscribeToTopic(this.topicPrefix+ResourceNameEnum.CLOUD_PRESSURE_LED_CMD_RESOURCE.getResourceName()+"/lv",qos);
		}
	}


	// private methods
	private String createTopicName(ResourceNameEnum resource)
	{
		return this.topicPrefix + resource.getResourceName();
	}

	private boolean publishMessageToCloud(ResourceNameEnum resource, String itemName, String payload)
	{
		String topicName = createTopicName(resource) + "-" + itemName;

		try {
			_Logger.finest("Publishing payload value(s) to Ubidots: " + topicName);

			publishMessage(topicName, payload.getBytes(), this.qosLevel);

			return true;
		} catch (Exception e) {
			_Logger.warning("Failed to publish message to Ubidots: " + topicName);
		}

		return false;
	}
}
