/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * Copyright (c) 2020 by Andrew D. King
 */ 

package programmingtheiot.part03.integration.app;

import org.junit.*;
import programmingtheiot.gda.app.DeviceDataManager;

import java.util.logging.Logger;

/**
 * This test case class contains very basic integration tests for
 * DeviceDataManager. It should not be considered complete,
 * but serve as a starting point for the student implementing
 * additional functionality within their Programming the IoT
 * environment.
 *
 */
public class DeviceDataManagerIntegrationTest
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(DeviceDataManagerIntegrationTest.class.getName());
	

	// member var's
	
	
	// test setup methods
	
	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}
	
	/**
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}
	
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	}
	
	/**
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}
	
	
	// test methods
	

	@Test
	public void testDeviceDataManagerIntegration()
	{
		boolean enableMqtt = true;
		boolean enableCoap = false;
		boolean enableCloud = false;
		boolean enableSmtp = false;
		boolean enablePersistence = false;

		DeviceDataManager devDataMgr =
				new DeviceDataManager(enableMqtt, enableCoap, enableCloud, enableSmtp, enablePersistence);

		devDataMgr.startManager();

		try {
			Thread.sleep(300000L);
		} catch (InterruptedException e) {
			// ignore
		}

		devDataMgr.stopManager();
	}
	
}
