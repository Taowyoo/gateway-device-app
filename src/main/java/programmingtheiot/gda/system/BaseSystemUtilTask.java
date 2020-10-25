/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.system;

import java.util.logging.Level;
import java.util.logging.Logger;

import programmingtheiot.data.SensorData;

/**
 *
 */
public abstract class BaseSystemUtilTask
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(BaseSystemUtilTask.class.getName());
	
	
	// private
	private SensorData latestSensorData = null;
	
	// constructors
	
	public BaseSystemUtilTask()
	{
		super();
	}
	
	
	// public methods

	/**
	 * Generate Telemetry Sensor Data.
	 *
	 * @return
	 */
	public SensorData generateTelemetry()
	{
		this.latestSensorData = new SensorData();
		this.latestSensorData.setValue(getSystemUtil());
		return this.latestSensorData;
	}

	/**
	 * Retrieve Telemetry Value by calling method implemented by sub-class.
	 *
	 * @return float Telemetry value
	 */
	public float getTelemetryValue()
	{
		if (this.latestSensorData == null){
			generateTelemetry();
		}
		float val = this.latestSensorData.getValue();
		_Logger.log(Level.INFO, "Got telemetry value: {0}.", val);
		return val;
	}
	
	
	// protected methods
	
	/**
	 * Template method definition. Sub-class will implement this to retrieve
	 * the system utilization measure.
	 * 
	 * @return float System util measure data
	 */
	protected abstract float getSystemUtil();
	
}
