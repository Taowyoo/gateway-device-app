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
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
//		gson = new Gson();
		gson = new GsonBuilder().setPrettyPrinting().create();
	}
	
	
	// public methods
	
	public String actuatorDataToJson(ActuatorData actuatorData)
	{
		String jsonData = gson.toJson(actuatorData);
		return jsonData;
	}
	
	public String sensorDataToJson(SensorData sensorData)
	{
		String jsonData = gson.toJson(sensorData);
		return jsonData;
	}
	
	public String systemPerformanceDataToJson(SystemPerformanceData sysPerfData)
	{
		String jsonData = gson.toJson(sysPerfData);
		return jsonData;
	}
	
	public String systemStateDataToJson(SystemStateData sysStateData)
	{
		String jsonData = gson.toJson(sysStateData);
		return jsonData;
	}
	
	public ActuatorData jsonToActuatorData(String jsonData)
	{
		ActuatorData actuatorData = gson.fromJson(jsonData, ActuatorData.class);
		return actuatorData;
	}
	
	public SensorData jsonToSensorData(String jsonData)
	{
		SensorData sensorData = gson.fromJson(jsonData, SensorData.class);
		return sensorData;
	}
	
	public SystemPerformanceData jsonToSystemPerformanceData(String jsonData)
	{
		SystemPerformanceData systemPerformanceData = gson.fromJson(jsonData, SystemPerformanceData.class);
		return systemPerformanceData;
	}
	
	public SystemStateData jsonToSystemStateData(String jsonData)
	{
		SystemStateData systemStateData = gson.fromJson(jsonData, SystemStateData.class);
		return systemStateData;
	}
	
}
