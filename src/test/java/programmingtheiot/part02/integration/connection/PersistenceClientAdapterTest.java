/**
 * 
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2020 by Andrew D. King
 */ 

package programmingtheiot.part02.integration.connection;

import static org.junit.Assert.*;
import static programmingtheiot.common.ConfigConst.DEFAULT_QOS;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;
import programmingtheiot.gda.connection.RedisPersistenceAdapter;

/**
 * This test case class contains very basic integration tests for
 * RedisPersistenceAdapter. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class PersistenceClientAdapterTest
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(PersistenceClientAdapterTest.class.getName());
	
	
	// member var's
	
	private RedisPersistenceAdapter rpa = null;
	private Random random = null;
	
	// test setup methods
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.random = new Random();
		this.rpa = new RedisPersistenceAdapter();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}
	
	// test methods
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#connectClient()}.
	 */
	@Test
	public void testConnectClient()
	{
		assertNotNull("Fail to construct RedisPersistenceAdapter instance!",this.rpa);
		this.rpa.connectClient();
		assertTrue("RedisPersistenceAdapter fail to connect to Redis server!",this.rpa.isConnected());
	}
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#disconnectClient()}.
	 */
	@Test
	public void testDisconnectClient()
	{
		assertNotNull("Fail to construct RedisPersistenceAdapter instance!",this.rpa);
		this.rpa.connectClient();
		this.rpa.disconnectClient();
		assertFalse("RedisPersistenceAdapter fail to disconnect to Redis server!",this.rpa.isConnected());
	}
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#getActuatorData(java.lang.String, java.util.Date, java.util.Date)}.
	 */
	@Test
	public void testGetActuatorData()
	{
		// prepare data array
		List<ActuatorData> dataList = new ArrayList<>();
		String dataName = "TestActuatorData";
		for (int i = 0; i < 5; i++) {
			ActuatorData data = new ActuatorData();
			data.setName(dataName);
			data.setValue(this.random.nextFloat());
			data.setStateData("This is a " + dataName);
			dataList.add(data);
		}
		this.rpa.connectClient();
		// store data
		this.rpa.storeData(dataName+"Topic", DEFAULT_QOS, dataList.toArray(new ActuatorData[dataList.size()]));
		// get data
		Date startDate = new Date();
		startDate.setTime(dataList.get(0).getTimeStampMillis());
		Date endDate = new Date();
		endDate.setTime(dataList.get(dataList.size()-1).getTimeStampMillis());
		ActuatorData[] retDataArray = this.rpa.getActuatorData(dataName+"Topic",startDate,endDate);
		assertNotNull("Fail to get ActuatorDataArray from Redis Server!",retDataArray);
		assertEquals("Get DataArray with wrong size!",retDataArray.length,dataList.size());
		this.rpa.disconnectClient();
	}
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#getSensorData(java.lang.String, java.util.Date, java.util.Date)}.
	 */
	@Test
	public void testGetSensorData()
	{
		// prepare data array
		List<SensorData> dataList = new ArrayList<>();
		String dataName = "TestSensorData";
		for (int i = 0; i < 5; i++) {
			SensorData data = new SensorData();
			data.setName(dataName);
			data.setValue(this.random.nextFloat());
			dataList.add(data);
		}

		this.rpa.connectClient();
		// store data
		this.rpa.storeData(dataName+"Topic", DEFAULT_QOS, dataList.toArray(new SensorData[dataList.size()]));
		// get data
		Date startDate = new Date();
		startDate.setTime(dataList.get(0).getTimeStampMillis());
		Date endDate = new Date();
		endDate.setTime(dataList.get(dataList.size()-1).getTimeStampMillis());
		SensorData[] retDataArray = this.rpa.getSensorData(dataName+"Topic",startDate,endDate);
		assertNotNull("Fail to get SensorDataArray from Redis Server!",retDataArray);
		assertEquals("Get DataArray with wrong size!",retDataArray.length,dataList.size());
		this.rpa.disconnectClient();
	}
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#storeData(java.lang.String, int, programmingtheiot.data.ActuatorData[])}.
	 */
	@Test
	public void testStoreDataStringIntActuatorDataArray()
	{
		// prepare data array
		List<ActuatorData> dataList = new ArrayList<>();
		String dataName = "TestActuatorData";
		for (int i = 0; i < 5; i++) {
			ActuatorData data = new ActuatorData();
			data.setName(dataName);
			data.setValue(this.random.nextFloat());
			data.setStateData("This is a " + dataName);
			dataList.add(data);
		}
		// store data array
		this.rpa.connectClient();
		boolean ret = this.rpa.storeData(dataName+"Topic", DEFAULT_QOS, dataList.toArray(new ActuatorData[dataList.size()]));
		assertTrue("Fail to store ActuatorDataArray to Redis Server", ret);
		this.rpa.disconnectClient();
	}
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#storeData(java.lang.String, int, programmingtheiot.data.SensorData[])}.
	 */
	@Test
	public void testStoreDataStringIntSensorDataArray()
	{
		// prepare data array
		List<SensorData> dataList = new ArrayList<>();
		String dataName = "TestSensorData";
		for (int i = 0; i < 5; i++) {
			SensorData data = new SensorData();
			data.setName(dataName);
			data.setValue(this.random.nextFloat());
			dataList.add(data);
		}
		// store data array
		this.rpa.connectClient();
		boolean ret = this.rpa.storeData(dataName+"Topic", DEFAULT_QOS, dataList.toArray(new SensorData[dataList.size()]));
		assertTrue("Fail to store SensorDataArray to Redis Server", ret);
		this.rpa.disconnectClient();
	}
	
	/**
	 * Test method for {@link programmingtheiot.gda.connection.RedisPersistenceAdapter#storeData(java.lang.String, int, programmingtheiot.data.SystemPerformanceData[])}.
	 */
	@Test
	public void testStoreDataStringIntSystemPerformanceDataArray()
	{
		// prepare data array
		List<SystemPerformanceData> dataList = new ArrayList<>();
		String dataName = "TestSystemPerformanceData";
		for (int i = 0; i < 5; i++) {
			SystemPerformanceData data = new SystemPerformanceData();
			data.setName(dataName);
			data.setCpuUtilization(this.random.nextFloat());
			data.setMemoryUtilization(this.random.nextFloat());
			data.setDiskUtilization(this.random.nextFloat());
			dataList.add(data);
		}
		// store data array
		this.rpa.connectClient();
		boolean ret = this.rpa.storeData(dataName+"Topic", DEFAULT_QOS, dataList.toArray(new SystemPerformanceData[dataList.size()]));
		assertTrue("Fail to store SystemPerformanceDataArray to Redis Server", ret);
		this.rpa.disconnectClient();
	}
	
}
