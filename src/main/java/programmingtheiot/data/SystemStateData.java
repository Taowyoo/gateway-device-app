/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ObjectUtils;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.gda.system.BaseSystemUtilTask;

/**
 * Convenience wrapper to store system state data, including location
 * information, action command, state data and a list of the following
 * data items:
 * <p>SystemPerformanceData
 * <p>SensorData
 * 
 */
public class SystemStateData extends BaseIotData implements Serializable
{
	// static
	private static final Logger _Logger = Logger.getLogger(SystemStateData.class.getName());

	public static final int NO_ACTION = 0;
	public static final int REBOOT_SYSTEM_ACTION = 1;
	public static final int GET_SYSTEM_STATE_ACTION = 2;
	
	public static final String DEFAULT_LOCATION = ConfigConst.NOT_SET;
	
	// private var's
	
    private String location = DEFAULT_LOCATION;
    
    private int actionCmd = NO_ACTION;
    
    private List<SystemPerformanceData> sysPerfDataList = null;
    private List<SensorData> sensorDataList = null;
    
    
	// constructors
	
	public SystemStateData()
	{
		super();
		super.setName(ConfigConst.SYS_STATE_DATA);
		this.sysPerfDataList = new ArrayList<SystemPerformanceData>();
		this.sensorDataList = new ArrayList<SensorData>();
	}
	
	
	// public methods
	
	public boolean addSensorData(SensorData data)
	{
		if(this.sensorDataList != null){
			this.sensorDataList.add(data);
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean addSystemPerformanceData(SystemPerformanceData data)
	{
		if(this.sysPerfDataList != null){
			this.sysPerfDataList.add(data);
			return true;
		}
		else {
			return false;
		}
	}
	
	public int getActionCommand()
	{
		return actionCmd;
	}
	
	public String getLocation()
	{
		return location;
	}
	
	public List<SensorData> getSensorDataList()
	{
		return sensorDataList;
	}
	
	public List<SystemPerformanceData> getSystemPerformanceDataList()
	{
		return sysPerfDataList;
	}
	
	public void setActionCommand(int actionCmd)
	{
		this.actionCmd = actionCmd;
	}
	
	public void setLocation(String location)
	{
		this.location = location;
	}

	@Override
	public String toString() {
		return "SystemStateData{" +
				"location='" + location + '\'' +
				", actionCmd=" + actionCmd +
				", sysPerfDataList=" + sysPerfDataList +
				", sensorDataList=" + sensorDataList +
				"} " + super.toString();
	}

	// protected methods

	/**
	 * Use given data to update current instance
	 * @param data While the parameter must implement this method,
	 */
	protected void handleUpdateData(BaseIotData data)
	{
		if (data instanceof SystemStateData){
			SystemStateData systemStateData = (SystemStateData)data;
			this.location = systemStateData.location;
			this.actionCmd = systemStateData.actionCmd;
			this.sysPerfDataList = systemStateData.sysPerfDataList;
			this.sensorDataList = systemStateData.sensorDataList;
		}
		else{
			_Logger.log(Level.WARNING, "Got invalid data when handleUpdateData!");
		}
	}
	
}
