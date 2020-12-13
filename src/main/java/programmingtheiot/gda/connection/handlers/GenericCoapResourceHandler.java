/**
 * This class is part of the Programming the Internet of Things project.
 * <p>
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */

package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
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
	protected IDataMessageListener dataMsgListener = null;
	protected ResourceNameEnum fullResourceNameEnum;
	// constructors
	
	/**
	 * Constructor.
	 * 
	 * @param resource Basically, the path (or topic)
	 */
	public GenericCoapResourceHandler(String resourceName, ResourceNameEnum resource)
	{
		super(resourceName);
		this.fullResourceNameEnum = resource;
	}

	// public methods
	
	@Override
	public void handleDELETE(CoapExchange context)
	{
		_Logger.info(String.format("Receive a DELETE from %s, payload: %s",
				context.getSourceAddress().toString(),
				new String(context.getRequestPayload())));
		// accept the request
		context.accept();
		String msg = "Cannot to be DELETE"; // fill this in
		// send an appropriate response
		context.respond(ResponseCode.NOT_IMPLEMENTED, msg);
	}
	
	@Override
	public void handleGET(CoapExchange context)
	{
		_Logger.info(String.format("Receive a DELETE from %s, payload: %s",
				context.getSourceAddress().toString(),
				new String(context.getRequestPayload())));
		// accept the request
		context.accept();
		String msg = "Cannot to be GET"; // fill this in
		// send an appropriate response
		context.respond(ResponseCode.NOT_IMPLEMENTED, msg);

	}
	
	@Override
	public void handlePOST(CoapExchange context)
	{
		_Logger.info(String.format("Receive a DELETE from %s, payload: %s",
				context.getSourceAddress().toString(),
				new String(context.getRequestPayload())));
		// accept the request
		context.accept();

		String msg = "Cannot to be POST"; // fill this in
		// send an appropriate response
		context.respond(ResponseCode.NOT_IMPLEMENTED, msg);

	}
	
	@Override
	public void handlePUT(CoapExchange context)
	{
		_Logger.info(String.format("Receive a DELETE from %s, payload: %s",
				context.getSourceAddress().toString(),
				new String(context.getRequestPayload())));
		context.accept();

		String msg = "Cannot to be PUT"; // fill this in
		// send an appropriate response
		context.respond(ResponseCode.NOT_IMPLEMENTED, msg);
	}
	
	public void setDataMessageListener(IDataMessageListener listener)
	{
		this.dataMsgListener = listener;
	}
	
}
