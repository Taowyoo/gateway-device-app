/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.data;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import programmingtheiot.common.ConfigConst;

/**
 * Shell representation of class for student implementation.
 *
 */
public class DataUtil
{
	// static

	private static final DataUtil _Instance = new DataUtil();

	/**
	 * Returns the Singleton instance of this class.
	 * 
	 * @return ConfigUtil
	 */
	public static final DataUtil getInstance()
	{
		return _Instance;
	}
	
	
	// private var's
	Gson gson = null;
	
	// constructors
	
	/**
	 * Default (private).
	 * 
	 */
	private DataUtil()
	{
		super();
		gson = new GsonBuilder().setPrettyPrinting().create();
	}

	
	// public methods
	
	public String actuatorDataToJson(ActuatorData actuatorData)
	{
		String jsonData = gson.toJson(actuatorData);
		return jsonData;
	}
	public String actuatorDataToJsonCloud(ActuatorData actuatorData)
	{
		Map<String, Map> cloudNode = new HashMap<>();
		Map<String,Object> node = new HashMap<>();
		node.put("value", (double) actuatorData.getStatusCode());
		node.put("timestamp", (double) actuatorData.getTimeStampMillis());
		Map<String,Object> context = new HashMap<>();
		context.put("state-data",actuatorData.getStateData());
		node.put("context",context);
		cloudNode.put(actuatorData.getName(),node);
		String jsonData = gson.toJson(cloudNode);
		return jsonData;
	}
	public String sensorDataToJson(SensorData sensorData)
	{
		String jsonData = gson.toJson(sensorData);
		return jsonData;
	}

	public String sensorDataToJsonCloud(SensorData sensorData)
	{
		Map<String, Map<String,Double>> cloudNodes = new HashMap<>();
		Map<String,Double> node = new HashMap<>();
		node.put("value", (double) sensorData.getValue());
		node.put("timestamp", (double) sensorData.getTimeStampMillis());
		cloudNodes.put(sensorData.getName(),node);
		String jsonData = gson.toJson(cloudNodes);
		return jsonData;
	}

	public String systemPerformanceDataToJson(SystemPerformanceData sysPerfData)
	{
		String jsonData = gson.toJson(sysPerfData);
		return jsonData;
	}

	public String systemPerformanceDataToJsonCloud(SystemPerformanceData sysPerfData)
	{
		Map<String, Map<String,Double>> cloudNodes = new HashMap<>();
		Map<String,Double> cpuNode = new HashMap<>();
		cpuNode.put("value", (double) sysPerfData.getCpuUtilization());
		cpuNode.put("timestamp", (double) sysPerfData.getTimeStampMillis());
		Map<String,Double> memNode = new HashMap<>();
		memNode.put("value", (double) sysPerfData.getMemoryUtilization());
		memNode.put("timestamp", (double) sysPerfData.getTimeStampMillis());
		Map<String,Double> diskNode = new HashMap<>();
		diskNode.put("value", (double) sysPerfData.getDiskUtilization());
		diskNode.put("timestamp", (double) sysPerfData.getTimeStampMillis());
		cloudNodes.put(ConfigConst.CPU_UTIL_NAME,cpuNode);
		cloudNodes.put(ConfigConst.MEM_UTIL_NAME,memNode);
		cloudNodes.put(ConfigConst.DISK_UTIL_NAME,diskNode);
		String jsonData = gson.toJson(cloudNodes);
		return jsonData;
	}

	public String systemStateDataToJson(SystemStateData sysStateData)
	{
		String jsonData = gson.toJson(sysStateData);
		return jsonData;
	}
	
	public ActuatorData jsonToActuatorData(String jsonData)
	{
		jsonData = this.preProcess(jsonData);
		ActuatorData actuatorData = gson.fromJson(jsonData, ActuatorData.class);
		return actuatorData;
	}
	
	public SensorData jsonToSensorData(String jsonData)
	{
		jsonData = this.preProcess(jsonData);
		SensorData sensorData = gson.fromJson(jsonData, SensorData.class);
		return sensorData;
	}
	
	public SystemPerformanceData jsonToSystemPerformanceData(String jsonData)
	{
		jsonData = this.preProcess(jsonData);
		SystemPerformanceData systemPerformanceData = gson.fromJson(jsonData, SystemPerformanceData.class);
		return systemPerformanceData;
	}
	
	public SystemStateData jsonToSystemStateData(String jsonData)
	{
		jsonData = this.preProcess(jsonData);
		SystemStateData systemStateData = gson.fromJson(jsonData, SystemStateData.class);
		return systemStateData;
	}

	private String preProcess(String jsonData){
		if(jsonData.charAt(0) == '"' && jsonData.charAt(jsonData.length() - 1) == '"'){
			jsonData = jsonData.substring(1,jsonData.length() - 1);
		}
		return  StringEscapeUtils.unescapeJava(jsonData);
	}


}
