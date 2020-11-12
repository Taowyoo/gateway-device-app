/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.gda.connection.handlers.GenericCoapResourceHandler;

/**
 * Shell representation of class for student implementation.
 * 
 */
public class CoapServerGateway
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(CoapServerGateway.class.getName());
	
	// params
	private CoapServer coapServer = null;
	private IDataMessageListener dataMsgListener = null;

	// constructors
	/**
	 * Default.
	 * 
	 */
	public CoapServerGateway()
	{
		this(ResourceNameEnum.values());
	}

	/**
	 * Constructor.
	 * 
	 * @param useDefaultResources
	 */
	public CoapServerGateway(boolean useDefaultResources)
	{
		this(useDefaultResources ? ResourceNameEnum.values() : (ResourceNameEnum[]) null);
	}

	/**
	 * Constructor.
	 * 
	 * @param resources
	 */
	public CoapServerGateway(ResourceNameEnum ...resources)
	{
		initServer(resources);
	}

	
	// public methods
	
	public void addResource(ResourceNameEnum resource)
	{
		if (resource != null) {
			// break out the hierarchy of names and build the resource
			// handler generation(s) as needed, checking if any parent already
			// exists - and if so, add to the existing resource
			_Logger.info("Adding server resource handler chain: " + resource.getResourceName());

			createAndAddResourceChain(resource);
		}
	}
	
	public boolean hasResource(String name)
	{
		return false;
	}
	
	public void setDataMessageListener(IDataMessageListener listener)
	{
		this.dataMsgListener = listener;
	}
	
	public boolean startServer()
	{
		_Logger.info("CoAP server is starting...");
		this.coapServer.start();
		_Logger.info("CoAP server is started!");
		return true;
	}
	
	public boolean stopServer()
	{
		_Logger.info("CoAP server is stopping...");
		this.coapServer.stop();
		_Logger.info("CoAP server is stopped!");
		return true;
	}
	
	
	// private methods
	
	private void createAndAddResourceChain(ResourceNameEnum resource)
	{
		List<String> resourceNames = resource.getResourceNameChain();
		Queue<String> queue = new ArrayBlockingQueue<>(resourceNames.size());

		queue.addAll(resourceNames);

		// check if we have a parent resource
		Resource parentResource = this.coapServer.getRoot();

		// if no parent resource, add it in now (should be named "PIOT")
		if (parentResource == null) {
			parentResource = new GenericCoapResourceHandler(queue.poll());
			this.coapServer.add(parentResource);
		}

		while (! queue.isEmpty()) {
			// get the next resource name
			String   resourceName = queue.poll();
			Resource nextResource = parentResource.getChild(resourceName);

			if (nextResource == null) {
				// TODO: if this is the last entry, use a custom resource handler implementation that
				// is specific to the resource's implementation needs (e.g. SensorData, ActuatorData, etc.)
				nextResource = new GenericCoapResourceHandler(resourceName);
				parentResource.add(nextResource);
			}

			parentResource = nextResource;
		}

	}
	
	private void initServer(ResourceNameEnum ...resources)
	{
		this.coapServer = new CoapServer();
		if (resources != null){
			for(ResourceNameEnum resource : resources){
				this.addResource(resource);
			}
		}
	}
}
