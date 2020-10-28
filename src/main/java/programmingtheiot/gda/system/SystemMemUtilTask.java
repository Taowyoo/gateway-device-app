/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.system;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class SystemMemUtilTask extends BaseSystemUtilTask
{
	// constructors
	
	/**
	 * Default.
	 * 
	 */
	public SystemMemUtilTask()
	{
		super();
		this.dataName = "SystemMemUtil";
	}
	
	
	// protected methods
	/**
	 * Get System Memory current occupied in
	 *
	 * @return float System Memory current occupied
	 */
	@Override
	protected float getSystemUtil()
	{
		return (float)ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
	}
	
}
