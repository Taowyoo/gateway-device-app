/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;


/**
 * Shell representation of class for student implementation.
 *
 */
public class GenericCoapResourceHandler extends CoapResource
{
	// static
	
	private static final Logger _Logger =
		Logger.getLogger(GenericCoapResourceHandler.class.getName());
	
	// params

	private IDataMessageListener dataMsgListener = null;

	// constructors
	
	/**
	 * Constructor.
	 * 
	 * @param resource Basically, the path (or topic)
	 */
	public GenericCoapResourceHandler(ResourceNameEnum resource)
	{
		this(resource.getResourceName());
	}
	
	/**
	 * Constructor.
	 * 
	 * @param resourceName The name of the resource.
	 */
	public GenericCoapResourceHandler(String resourceName)
	{
		super(resourceName);
	}
	
	
	// public methods
	
	@Override
	public void handleDELETE(CoapExchange context)
	{
		_Logger.info(String.format("Receive a DELETE from %s with text: %s, payload: %s",
				context.getSourceAddress().toString(),
				context.getRequestText(),
				new String(context.getRequestPayload())));
		// TODO: validate 'context'

		// accept the request
		context.accept();

		// TODO: retrieve the requested data and generate a response message: 'msg'
		String msg = "Default response to DELETE"; // fill this in

		// send an appropriate response
		context.respond(ResponseCode.DELETED, msg);
	}
	
	@Override
	public void handleGET(CoapExchange context)
	{
		_Logger.info(String.format("Receive a GET from %s with text: %s, payload: %s",
				context.getSourceAddress().toString(),
				context.getRequestText(),
				new String(context.getRequestPayload())));
		// TODO: validate 'context'

		// accept the request
		context.accept();

		// TODO: retrieve the requested data and generate a response message: 'msg'
		String msg = "Default response to GET"; // fill this in

		// send an appropriate response
		context.respond(ResponseCode.VALID, msg);
	}
	
	@Override
	public void handlePOST(CoapExchange context)
	{
		_Logger.info(String.format("Receive a POST from %s with text: %s, payload: %s",
				context.getSourceAddress().toString(),
				context.getRequestText(),
				new String(context.getRequestPayload())));
		// TODO: validate 'context'

		// accept the request
		context.accept();

		// TODO: create (or update) the resource with the payload
		String payload = context.getRequestText();

		// TODO: generate a response message: 'msg'
		String msg = "Default response to POST"; // fill this in

		// send an appropriate response
		context.respond(ResponseCode.CREATED, msg);
	}
	
	@Override
	public void handlePUT(CoapExchange context)
	{
		_Logger.info(String.format("Receive a PUT from %s with text: %s, payload: %s",
				context.getSourceAddress().toString(),
				context.getRequestText(),
				new String(context.getRequestPayload())));
		// TODO: validate 'context'

		// accept the request
		context.accept();

		// TODO: retrieve the requested data and generate a response message: 'msg'
		String msg = "Default response to PUT"; // fill this in

		// send an appropriate response
		context.respond(ResponseCode.CHANGED, msg);
	}
	
	public void setDataMessageListener(IDataMessageListener listener)
	{
		this.dataMsgListener = listener;
	}
	
}
