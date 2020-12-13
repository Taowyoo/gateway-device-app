/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * 
 * You may find it more helpful to your design to adjust the
 * functionality, constants and interfaces (if there are any)
 * provided within in order to meet the needs of your specific
 * Programming the Internet of Things project.
 */ 

package programmingtheiot.gda.app;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.gda.system.SystemPerformanceManager;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Main GDA application.
 * 
 */
public class GatewayDeviceApp
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(GatewayDeviceApp.class.getName());

	public static final long DEFAULT_TEST_RUNTIME =
			ConfigUtil.getInstance().getInteger(ConfigConst.GATEWAY_DEVICE,ConfigConst.TEST_GDA_RUN_TIME_KEY, 0) * 1000;

	// private var's
	private DeviceDataManager devDataMgr = null;
	// constructors
	
	/**
	 * Constructor of GatewayDeviceApp
	 *
	 * @param args given args from cmd
	 */
	public GatewayDeviceApp(String[] args)
	{
		super();
		
		_Logger.info("Initializing GDA...");
		parseArgs(args);
		this.devDataMgr = new DeviceDataManager();
	}
	
	
	// static
	
	/**
	 * Main application entry point.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		// setup logging
		Logger logger = Logger.getLogger("");
		FileHandler fh;
		try {
			// This block configure the logger with handler and formatter
			fh = new FileHandler("logs/LogFile.log");
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// start the app
		GatewayDeviceApp gwApp = new GatewayDeviceApp(args);
		
		gwApp.startApp();
		
		try {
			if(DEFAULT_TEST_RUNTIME > 0){
				Thread.sleep(DEFAULT_TEST_RUNTIME);
			}
			else{
				Thread.currentThread().join();
			}
		} catch (InterruptedException e) {
			// ignore
		}
		
		gwApp.stopApp(0);
	}
	
	
	// public methods
	
	/**
	 * Initializes and starts the application.
	 * 
	 */
	public void startApp()
	{
		_Logger.info("Starting GDA...");
		
		try {
			this.devDataMgr.startManager();
			_Logger.info("GDA started successfully.");
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to start GDA. Exiting.", e);
			
			stopApp(-1);
		}
	}
	
	/**
	 * Stops the application.
	 * 
	 * @param code The exit code to pass to {@link System.exit()}
	 */
	public void stopApp(int code)
	{
		_Logger.info("Stopping GDA...");
		
		try {
			this.devDataMgr.stopManager();
			_Logger.log(Level.INFO, "GDA stopped successfully with exit code {0}.", code);
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to cleanly stop GDA. Exiting.", e);
		}
		
//		System.exit(code);
	}
	
	
	// private methods
	
	/**
	 * Load the config file.
	 * 
	 * NOTE: This will be added later.
	 * 
	 * @param configFile The name of the config file to load.
	 */
	private void initConfig(String configFile)
	{
		_Logger.log(Level.INFO, "Attempting to load configuration: {0}", (configFile != null ? configFile : "Default."));
		
		// TODO: Your code here
	}
	
	/**
	 * Parse any arguments passed in on app startup.
	 * <p>
	 * This method should be written to check if any valid command line args are provided,
	 * including the name of the config file. Once parsed, call {@link #initConfig(String)}
	 * with the name of the config file, or null if the default should be used.
	 * <p>
	 * If any command line args conflict with the config file, the config file
	 * in-memory content should be overridden with the command line argument(s).
	 * 
	 * @param args The non-null and non-empty args array.
	 */
	private void parseArgs(String[] args)
	{
		_Logger.info("Parsing args...");
		String configFile = null;
		
		if (args != null) {
			_Logger.log(Level.INFO, "Parsing {0} command line args.", args.length);
			
			for (String arg : args) {
				if (arg != null) {
					arg = arg.trim();
					
					// TODO: Your code here
				}
			}
		} else {
			_Logger.info("No command line args to parse.");
		}
		
		initConfig(configFile);
	}

}
