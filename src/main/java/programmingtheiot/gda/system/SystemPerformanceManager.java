/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.system;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.IDataMessageListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemPerformanceManager
{
	// logger
	private static final Logger _Logger =
			Logger.getLogger(SystemPerformanceManager.class.getName());

	// private var's
	private int pollSecs = 30;  // The number of seconds between each scheduled task poll

	private ScheduledExecutorService schedExecSvc = null;
	private SystemCpuUtilTask cpuUtilTask = null;
	private SystemMemUtilTask memUtilTask = null;

	private Runnable taskRunner = null;
	private boolean isStarted = false;
	// constructors
	
	/**
	 * Default constructor of SystemPerformanceManager.
	 * Initialize SystemPerformanceManager with default pollSecs.
	 *
	 */
	public SystemPerformanceManager()
	{
		this(ConfigConst.DEFAULT_POLL_CYCLES);
	}
	
	/**
	 * Constructor of SystemPerformanceManager.
	 * Initialize SystemPerformanceManager with given pollSecs, given pollSecs should be in range of (1,Integer.MAX_VALUE).
	 * 
	 * @param pollSecs The number of seconds between each scheduled task poll.
	 */
	public SystemPerformanceManager(int pollSecs)
	{
		_Logger.log(Level.INFO, "Initialize SystemPerformanceManager with pollSecs: {0}.", pollSecs);
		if (pollSecs > 1 && pollSecs < Integer.MAX_VALUE ){
			this.pollSecs = pollSecs;
		}
		else {
			_Logger.log(Level.WARNING, "Got invalid pollSecs: {0}!", pollSecs);
			_Logger.log(Level.WARNING, "Initialize SystemPerformanceManager with default pollSecs: {0}.",
					ConfigConst.DEFAULT_POLL_CYCLES);
			this.pollSecs = ConfigConst.DEFAULT_POLL_CYCLES;
		}
		this.schedExecSvc = Executors.newScheduledThreadPool(1);
		this.cpuUtilTask = new SystemCpuUtilTask();
		this.memUtilTask = new SystemMemUtilTask();

		this.taskRunner = () -> {
			this.handleTelemetry();
		};
	}
	
	
	// public methods

	/**
	 *
	 */
	public void handleTelemetry()
	{
		float cpuUtilPct = this.cpuUtilTask.getTelemetryValue();
		float memUtilPct = this.memUtilTask.getTelemetryValue();
		_Logger.log(Level.INFO, "Got system util values, CPU: {0}, Mem: {1}.", new Object[]{cpuUtilPct, memUtilPct});
	}
	
	public void setDataMessageListener(IDataMessageListener listener)
	{
	}

	/**
	 * Starts the SystemPerformanceManager.
	 */
	public void startManager()
	{
		_Logger.log(Level.INFO, "Starting SystemPerformanceManager  ...");
		if (! this.isStarted) {
			_Logger.log(Level.INFO, "Starting a schedule for polling Telemetry data ...");
			ScheduledFuture<?> futureTask = this.schedExecSvc.scheduleAtFixedRate(this.taskRunner, 0L, this.pollSecs, TimeUnit.SECONDS);
			this.isStarted = true;
			_Logger.log(Level.INFO, "Schedule for polling Telemetry data started.");
		}
		else {
			_Logger.log(Level.INFO, "Schedule for polling Telemetry data already started.");
		}
		_Logger.log(Level.INFO, "SystemPerformanceManager Started.");
	}

	/**
	 * Stops the SystemPerformanceManager.
	 */
	public void stopManager()
	{
		_Logger.log(Level.INFO, "Stopping SystemPerformanceManager ...");
		this.schedExecSvc.shutdown();
		_Logger.log(Level.INFO, "SystemPerformanceManager stopped.");
	}
	
}
