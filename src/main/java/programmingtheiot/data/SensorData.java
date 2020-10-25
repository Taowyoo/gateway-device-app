/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.data;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shell representation of class for student implementation.
 *
 */
public class SensorData extends BaseIotData implements Serializable
{
	// static
	private static final Logger _Logger = Logger.getLogger(SystemStateData.class.getName());

	public static final int DEFAULT_SENSOR_TYPE = 0;

	// private var's
	private int sensorType = DEFAULT_SENSOR_TYPE;
    private  float value = DEFAULT_VAL;

	// constructors
	public SensorData()
	{
		super();
	}
	
	public SensorData(int sensorType)
	{
		this();
		this.sensorType = sensorType;
	}
	
	
	// public methods

	public float getValue()
	{
		return value;
	}
	
	public void setValue(float val)
	{
		this.value = val;
	}

	public int getSensorType() {
		return sensorType;
	}

	public void setSensorType(int sensorType) {
		this.sensorType = sensorType;
	}

	@Override
	public String toString() {
		return "SensorData{" +
				"sensorType=" + sensorType +
				", value=" + value +
				"} " + super.toString();
	}

	// protected methods
	
	/* (non-Javadoc)
	 * @see programmingtheiot.data.BaseIotData#handleUpdateData(programmingtheiot.data.BaseIotData)
	 */
	protected void handleUpdateData(BaseIotData data)
	{
		if (data instanceof SensorData){
			SensorData sensorData = (SensorData)data;
			this.sensorType = sensorData.sensorType;
			this.value = sensorData.value;
		}
		else{
			_Logger.log(Level.WARNING, "Got invalid data when handleUpdateData!");
		}
	}
}
