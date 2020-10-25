/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2020 by Andrew D. King
 */ 

package programmingtheiot.part02.unit.data;

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Test;

import com.google.gson.Gson;

import programmingtheiot.common.ConfigUtil;
import programmingtheiot.data.*;

/**
 * This test case class contains very basic unit tests for
 * DataUtilTest. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class DataUtilTest
{
	// static
	
	public static final String DEFAULT_NAME = "DataUtilTestName";
	public static final String DEFAULT_LOCATION = "DataUtilTestLocation";
	public static final int DEFAULT_STATUS = 1;
	public static final int DEFAULT_CMD = 1;
	public static final float DEFAULT_VAL = 12.5f;
	
	private static final Logger _Logger =
		Logger.getLogger(DataUtilTest.class.getName());
	
	// member var's
	
	
	// test setup methods
	
	
	// test methods
	
	@Test
	public void testActuatorDataToJsonAndBack()
	{
		DataUtil dataUtil = DataUtil.getInstance();
		
		ActuatorData data = new ActuatorData();
		data.setName(DEFAULT_NAME);
		data.setStatusCode(DEFAULT_STATUS);
		data.setCommand(DEFAULT_CMD);
		data.setValue(DEFAULT_VAL);
		
		String jsonData = dataUtil.actuatorDataToJson(data);
		_Logger.log(Level.INFO,"jsonData:\n"+jsonData);
		assertNotNull(jsonData);

		ActuatorData data2 = dataUtil.jsonToActuatorData(jsonData);
		_Logger.log(Level.INFO,"Deserialized ActuatorData:\n"+data2);
		assertEquals(data.getName(), data2.getName());
		assertTrue(data.getStatusCode() == data2.getStatusCode());
		assertTrue(data.getCommand() == data2.getCommand());
		assertTrue(data.getValue() == data2.getValue());
	}
	
	@Test
	public void testSensorDataToJsonAndBack()
	{
		DataUtil dataUtil = DataUtil.getInstance();
		
		SensorData data = new SensorData();
		data.setName(DEFAULT_NAME);
		data.setStatusCode(DEFAULT_STATUS);
		data.setValue(DEFAULT_VAL);
		
		String jsonData = dataUtil.sensorDataToJson(data);
		_Logger.log(Level.INFO,"jsonData:\n"+jsonData);
		assertNotNull(jsonData);
		
		SensorData data2 = dataUtil.jsonToSensorData(jsonData);
		
		assertEquals(data.getName(), data2.getName());
		assertTrue(data.getStatusCode() == data2.getStatusCode());
		assertTrue(data.getValue() == data2.getValue());
	}
	
	@Test
	public void testSystemPerformanceDatatoJsonAndBack()
	{
		DataUtil dataUtil = DataUtil.getInstance();
		
		SystemPerformanceData data = new SystemPerformanceData();
		data.setName(DEFAULT_NAME);
		data.setStatusCode(DEFAULT_STATUS);
		data.setCpuUtilization(DEFAULT_VAL);
		data.setDiskUtilization(DEFAULT_VAL);
		data.setMemoryUtilization(DEFAULT_VAL);
		
		String jsonData = dataUtil.systemPerformanceDataToJson(data);
		_Logger.log(Level.INFO,"jsonData:\n"+jsonData);
		assertNotNull(jsonData);
		
		SystemPerformanceData data2 = dataUtil.jsonToSystemPerformanceData(jsonData);
		
		assertEquals(data.getName(), data2.getName());
		assertTrue(data.getStatusCode() == data2.getStatusCode());
		assertTrue(data.getCpuUtilization() == data2.getCpuUtilization());
		assertTrue(data.getDiskUtilization() == data2.getDiskUtilization());
		assertTrue(data.getMemoryUtilization() == data2.getMemoryUtilization());
	}
	
	@Test
	public void testSystemStateDatatoJsonAndBack()
	{
		DataUtil dataUtil = DataUtil.getInstance();

		// SystemPerformanceData
		SystemPerformanceData systemPerformanceData = new SystemPerformanceData();
		systemPerformanceData.setName(DEFAULT_NAME);
		systemPerformanceData.setStatusCode(DEFAULT_STATUS);
		systemPerformanceData.setCpuUtilization(DEFAULT_VAL);
		systemPerformanceData.setDiskUtilization(DEFAULT_VAL);
		systemPerformanceData.setMemoryUtilization(DEFAULT_VAL);

		// SensorData
		SensorData sensorData = new SensorData();
		sensorData.setName(DEFAULT_NAME);
		sensorData.setStatusCode(DEFAULT_STATUS);
		sensorData.setValue(DEFAULT_VAL);

		SystemStateData data = new SystemStateData();
		data.setName(DEFAULT_NAME);
		data.setLocation(DEFAULT_LOCATION);
		data.setStatusCode(DEFAULT_STATUS);
		data.setActionCommand(DEFAULT_CMD);
		data.addSensorData(sensorData);
		data.addSystemPerformanceData(systemPerformanceData);

		String jsonData = dataUtil.systemStateDataToJson(data);
		_Logger.log(Level.INFO,"jsonData:\n"+jsonData);
		assertNotNull(jsonData);
		
		SystemStateData data2 = dataUtil.jsonToSystemStateData(jsonData);
		
		assertEquals(data.getName(), data2.getName());
		assertEquals(data.getLocation(), data2.getLocation());
		assertTrue(data.getStatusCode() == data2.getStatusCode());
		assertTrue(data.getActionCommand() == data2.getActionCommand());

		// test SystemPerformanceData in the list
		SystemPerformanceData systemPerformanceData2 = data2.getSystemPerformanceDataList().get(0);

		assertEquals(systemPerformanceData.getName(), systemPerformanceData2.getName());
		assertTrue(systemPerformanceData.getStatusCode() == systemPerformanceData2.getStatusCode());
		assertTrue(systemPerformanceData.getCpuUtilization() == systemPerformanceData2.getCpuUtilization());
		assertTrue(systemPerformanceData.getDiskUtilization() == systemPerformanceData2.getDiskUtilization());
		assertTrue(systemPerformanceData.getMemoryUtilization() == systemPerformanceData2.getMemoryUtilization());

		// test SensorData in the list
		SensorData sensorData2 = data2.getSensorDataList().get(0);

		assertEquals(sensorData.getName(), sensorData2.getName());
		assertTrue(sensorData.getStatusCode() == sensorData2.getStatusCode());
		assertTrue(sensorData.getValue() == sensorData2.getValue());

	}
	
}
