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
public class ActuatorData extends BaseIotData implements Serializable
{
	// static
	private static final Logger _Logger = Logger.getLogger(SystemStateData.class.getName());

	public static final int DEFAULT_COMMAND = 0;
	public static final int COMMAND_OFF = DEFAULT_COMMAND;
	public static final int COMMAND_ON = 1;

	public static final boolean DEFAULT_RESPONSE_FLAG = false;
	public static final float DEFAULT_VALUE = 0.0f;

	public static final int DEFAULT_ACTUATOR_TYPE = 0;
	public static final String DEFAULT_STATE_DATA = "Default state data string";
	// private var's

	private int actuatorType = DEFAULT_ACTUATOR_TYPE;
	private int command = DEFAULT_COMMAND;
	private boolean isResponse = DEFAULT_RESPONSE_FLAG;
	private float value = DEFAULT_VALUE;
	private String stateData = DEFAULT_STATE_DATA;

	// constructors
	
	/**
	 * Default.
	 */
	public ActuatorData()
	{
		super();
	}

	// public methods
	
	public int getCommand()
	{
		return command;
	}
	
	public float getValue()
	{
		return value;
	}
	
	public void setCommand(int command)
	{
		this.command = command;
	}
	
	public void setValue(float val)
	{
		this.value = val;
	}

	public int getActuatorType() {
		return actuatorType;
	}

	public void setActuatorType(int actuatorType) {
		this.actuatorType = actuatorType;
	}

	public boolean isResponseFlagEnabled(){
		return true;
	}

	public boolean isResponse() {
		return isResponse;
	}

	public void setAsResponse() {
		this.isResponse = true;
	}

	public void setStateData(String data) {
		this.stateData = data;
	}

	public String getStateData() {
		return stateData;
	}
	// protected methods


	@Override
	public String toString() {
		return "ActuatorData{" +
				"actuatorType=" + actuatorType +
				", command=" + command +
				", isResponse=" + isResponse +
				", value=" + value +
				", stateData='" + stateData + '\'' +
				"} " + super.toString();
	}

	/**
	 * Use given data to update current instance
	 * @param data While the parameter must implement this method,
	 */
	protected void handleUpdateData(BaseIotData data)
	{
		if (data instanceof ActuatorData){
			ActuatorData actuatorData = (ActuatorData) data;
			this.value = actuatorData.value;
			this.actuatorType = actuatorData.actuatorType;
			this.isResponse = actuatorData.isResponse;
			this.command = actuatorData.command;
			this.stateData = actuatorData.stateData;
		}
		else{
			_Logger.log(Level.WARNING, "Got invalid data when handleUpdateData!");
		}

	}
	
}
