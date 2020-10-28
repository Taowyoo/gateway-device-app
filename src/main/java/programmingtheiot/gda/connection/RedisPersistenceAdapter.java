/**
 * This class is part of the Programming the Internet of Things project.
 *
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.connection;

import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonSyntaxException;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.common.ResourceNameEnum;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class RedisPersistenceAdapter implements IPersistenceClient
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(RedisPersistenceAdapter.class.getName());
	
	// private var's
	private String host = null;
	private int port = 0;
	private boolean enableCrypt = false;
	private Jedis jedis = null;
	private Jedis jedisSub = null;
	private DataUtil dataUtil;

	private JedisPubSub subscriber = null;

	// constructor
	/**
	 * Default.
	 * 
	 */
	public RedisPersistenceAdapter()
	{
		super();
		ConfigUtil configUtil = ConfigUtil.getInstance();
		this.host = configUtil.getProperty(ConfigConst.DATA_GATEWAY_SERVICE, ConfigConst.HOST_KEY);
		this.port = configUtil.getInteger(ConfigConst.DATA_GATEWAY_SERVICE, ConfigConst.PORT_KEY);
		this.enableCrypt = configUtil.getBoolean(ConfigConst.DATA_GATEWAY_SERVICE, ConfigConst.ENABLE_CRYPT_KEY);
		this.dataUtil = DataUtil.getInstance();
		this.stringSensorDataMap = new HashMap<>();
		this.stringActuatorDataMap = new HashMap<>();
		this.jedis = this.initClient();
		this.jedisSub = this.initClient();
	}

	// public methods

	public Runnable subscribeToChannel(JedisPubSub sub, ResourceNameEnum... resource){
		this.subscriber = sub;
		String[] channels = Arrays.stream(resource).map(x -> x.getResourceName()).toArray(String[]::new);
		Runnable runnable =	() -> {
			_Logger.log(Level.INFO, "Redis client is subscribing to channel: {0}", Arrays.toString(channels));
			this.jedisSub.subscribe(sub, channels);
			_Logger.log(Level.INFO, String.format("Redis client succeeded to subscribe to channels."));
		};
		Thread thread = new Thread(runnable);
		thread.start();
		return runnable;
	}

	// implement methods for IPersistenceClient begin
	@Override
	public boolean connectClient()
	{
		if (this.jedis.isConnected()){
			_Logger.log(Level.WARNING,"Redis client has already connected!");
			return true;
		}
		else{
			try {
				_Logger.log(Level.INFO,"Redis client is connecting to server...");
				this.jedis.connect();
				this.jedisSub.connect();
				_Logger.log(Level.INFO,"Redis client succeeded to connect to server!");
				return true;
			}
			catch (JedisConnectionException jedisConnectionException){
				_Logger.log(Level.WARNING,"Error occurred when Redis client is connecting to server: " + jedisConnectionException.toString());
				return false;
			}
		}
	}

	@Override
	public boolean disconnectClient()
	{
		if (!this.jedis.isConnected()){
			_Logger.log(Level.WARNING,"Redis client has already disconnected!");
			return true;
		}
		else{
			try {
				_Logger.log(Level.INFO,"Redis client is disconnecting to server...");
				String quitRet = this.jedis.quit();
				if(this.subscriber != null){
					this.subscriber.unsubscribe();
					this.subscriber = null;
				}
				this.jedis.disconnect();
				this.jedisSub.disconnect();
				_Logger.log(Level.INFO,"Redis client succeeded to disconnect to server" );
				return true;
			}
			catch (JedisConnectionException jedisConnectionException){
				_Logger.log(Level.WARNING,"Error occurred when Redis client is disconnecting to server: " + jedisConnectionException.toString());
				return false;
			}
		}
	}

	@Override
	public ActuatorData[] getActuatorData(String topic, Date startDate, Date endDate)
	{
		List<ActuatorData> ans = new ArrayList<>();
		long startTime = startDate.getTime();
		long endTime = endDate.getTime();
		Set<String>  dataSet = this.jedis.zrangeByScore(topic,startTime,endTime);
		for (String data : dataSet){
			try {
				ans.add(dataUtil.jsonToActuatorData(data));
			}
			catch (JsonSyntaxException ex){
				_Logger.log(Level.WARNING, "Bypass a broken ActuatorData: " + data);
			}
		}
		return (ActuatorData[])ans.toArray();
	}

	@Override
	public SensorData[] getSensorData(String topic, Date startDate, Date endDate)
	{
		List<SensorData> ans = new ArrayList<>();
		long startTime = startDate.getTime();
		long endTime = endDate.getTime();
		Set<String>  dataSet = this.jedis.zrangeByScore(topic,startTime,endTime);
		for (String data : dataSet){
			try {
				ans.add(dataUtil.jsonToSensorData(data));
			}
			catch (JsonSyntaxException ex){
				_Logger.log(Level.WARNING, "Bypass a broken SensorData: " + data);
			}
		}
		return (SensorData[])ans.toArray();
	}

	@Override
	public void registerDataStorageListener(Class cType, IPersistenceListener listener, String... topics)
	{
		// TODO: Update in the future, to replace subscribe callback in DeviceDataManager
	}

	@Override
	public boolean storeData(String topic, int qos, ActuatorData... data)
	{
		for (ActuatorData oneData : data){
			Transaction t = jedis.multi();
			t.zadd(topic,oneData.getTimeStampMillis(),dataUtil.actuatorDataToJson(oneData));
			t.exec();
		}
		return true;
	}

	@Override
	public boolean storeData(String topic, int qos, SensorData... data)
	{
		for (SensorData oneData : data){
			Transaction t = jedis.multi();
			t.zadd(topic,oneData.getTimeStampMillis(),dataUtil.sensorDataToJson(oneData));
			t.exec();
			_Logger.log(Level.INFO, String.format("ZADD score: %s data: %s", oneData.getTimeStampMillis(),dataUtil.sensorDataToJson(oneData)));
		}
		return true;
	}

	@Override
	public boolean storeData(String topic, int qos, SystemPerformanceData... data)
	{
		for (SystemPerformanceData oneData : data){
			Transaction t = jedis.multi();
			t.zadd(topic,oneData.getTimeStampMillis(),dataUtil.systemPerformanceDataToJson(oneData));
			t.exec();
		}
		return true;
	}
	// implement methods for IPersistenceClient end
	
	// private methods
	
	/**
	 * Generates a listener key map from the class type and topic.
	 * The format will be as follows:
	 * <br>'simple class name' + "_" + 'topic name'
	 * <br>e.g. ActuatorData_localhost/fan
	 * <br>e.g. SensorData_localhost/temperature
	 * <p>
	 * If the class type is null, it will simply be dropped and
	 * only the topic name will be used in the key. If the topic
	 * name is also null or invalid (e.g. empty), the 'all' keyword
	 * will be used instead.
	 * 
	 * @param cType The class type to use in the key.
	 * @param topic The topic name to use in the key.
	 * @return String The key derived from cType and topic, as per above.
	 */
	private String getListenerMapKey(Class cType, String topic)
	{
		StringBuilder buf = new StringBuilder();
		
		if (cType != null) {
			buf.append(cType.getSimpleName()).append("_");
		}
		
		if (topic != null && topic.trim().length() > 0) {
			buf.append(topic.trim());
		} else {
			buf.append("all");
		}
		
		String key = buf.toString();
		
		_Logger.info("Generated listener map lookup key from '" + cType + "' and '" + topic + "': " + key);
		
		return key;
	}
	
	private Jedis initClient()
	{
		Jedis newJedis = new Jedis(this.host, this.port);
		if (this.enableCrypt){
			// TODO: Encryption related implementation
		}
		return newJedis;
	}
	
	private Long updateRedisDataElement(String topic, double score, String payload)
	{
		return 0L;
	}


}
