/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.data;

import programmingtheiot.common.ConfigConst;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shell representation of class for student implementation.
 *
 */
public class SystemPerformanceData extends BaseIotData implements Serializable
{
	// static
	private static final Logger _Logger = Logger.getLogger(SystemPerformanceData.class.getName());

	public static final float DEFAULT_VAL = 0.0f;

	
	// private var's
	private float cpuUtil = DEFAULT_VAL;
	private float diskUtil = DEFAULT_VAL;
	private float memUtil = DEFAULT_VAL;
    
	// constructors
	
	public SystemPerformanceData()
	{
		super();
		super.setName(ConfigConst.SYS_PERF_DATA);
	}
	
	
	// public methods
	
	public float getCpuUtilization()
	{
		return cpuUtil;
	}
	
	public float getDiskUtilization()
	{
		return diskUtil;
	}
	
	public float getMemoryUtilization()
	{
		return memUtil;
	}
	
	public void setCpuUtilization(float val)
	{
		this.cpuUtil = val;
	}
	
	public void setDiskUtilization(float val)
	{
		this.diskUtil = val;
	}
	
	public void setMemoryUtilization(float val)
	{
		this.memUtil = val;
	}

	@Override
	public String toString() {
		return "SystemPerformanceData{" +
				"cpuUtil=" + cpuUtil +
				", diskUtil=" + diskUtil +
				", memUtil=" + memUtil +
				"} " + super.toString();
	}

	// protected methods

	/**
	 * Use given data to update current instance
	 * @param data While the parameter must implement this method,
	 */
	protected void handleUpdateData(BaseIotData data)
	{
		if (data instanceof SystemPerformanceData){
			SystemPerformanceData sysData = (SystemPerformanceData)data;
			this.diskUtil = sysData.diskUtil;
			this.cpuUtil = sysData.cpuUtil;
			this.memUtil = sysData.memUtil;
		}
		else{
			_Logger.log(Level.WARNING, "Got invalid data when handleUpdateData!");
		}

	}
	
}
