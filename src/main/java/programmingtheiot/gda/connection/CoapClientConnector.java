/**
 * This class is part of the Programming the Internet of Things project.
 * 
 * It is provided as a simple shell to guide the student and assist with
 * implementation for the Programming the Internet of Things exercises,
 * and designed to be modified by the student as needed.
 */ 

package programmingtheiot.gda.connection;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import org.eclipse.californium.elements.exception.ConnectorException;
import programmingtheiot.common.ConfigConst;
import programmingtheiot.common.ConfigUtil;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

import programmingtheiot.data.DataUtil;
import programmingtheiot.gda.connection.handlers.GenericCoapResponseHandler;

/**
 * Implementation of CoapClientConnector to connect to CoAP sever
 *
 */
public class CoapClientConnector implements IRequestResponseClient
{
	// static

	private static final Logger _Logger =
		Logger.getLogger(CoapClientConnector.class.getName());
	
	// params
	private String     protocol;
	private String     host;
	private int        port;
	private String     serverAddr;
	private CoapClient clientConn;
	private IDataMessageListener dataMsgListener;
	private boolean enableConfirmedMsgs = true;
	private Set<String> resources = null;
	// constructors

	/**
	 * Default.
	 * 
	 * All config data will be loaded from the config file.
	 */
	public CoapClientConnector()
	{
		ConfigUtil config = ConfigUtil.getInstance();
		this.host = config.getProperty(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.HOST_KEY, ConfigConst.DEFAULT_HOST);

		if (config.getBoolean(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.ENABLE_CRYPT_KEY)) {
			this.protocol = ConfigConst.DEFAULT_COAP_SECURE_PROTOCOL;
			this.port     = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.SECURE_PORT_KEY, ConfigConst.DEFAULT_COAP_SECURE_PORT);
		} else {
			this.protocol = ConfigConst.DEFAULT_COAP_PROTOCOL;
			this.port     = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_COAP_PORT);
		}

		// NOTE: URL does not have a protocol handler for "coap",
		// so we need to construct the URL manually
		this.serverAddr = this.protocol + "://" + this.host + ":" + this.port;

		initClient();

		_Logger.info("Using URL for server connection: " + this.serverAddr);
	}
		
	/**
	 * Constructor for customize with some given settings
	 * 
	 * @param host Given host
	 * @param isSecure Whether use crypt to secure
	 * @param enableConfirmedMsgs Whether enable confirmed type CoAP message
	 */
	public CoapClientConnector(String host, boolean isSecure, boolean enableConfirmedMsgs)
	{
		this.host = host;
		this.enableConfirmedMsgs = enableConfirmedMsgs;
		ConfigUtil config = ConfigUtil.getInstance();
		if (isSecure) {
			this.protocol = ConfigConst.DEFAULT_COAP_SECURE_PROTOCOL;
			this.port     = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.SECURE_PORT_KEY, ConfigConst.DEFAULT_COAP_SECURE_PORT);
		} else {
			this.protocol = ConfigConst.DEFAULT_COAP_PROTOCOL;
			this.port     = config.getInteger(ConfigConst.COAP_GATEWAY_SERVICE, ConfigConst.PORT_KEY, ConfigConst.DEFAULT_COAP_PORT);
		}

		// NOTE: URL does not have a protocol handler for "coap",
		// so we need to construct the URL manually
		this.serverAddr = this.protocol + "://" + this.host + ":" + this.port;

		initClient();

		_Logger.info("Using URL for server connection: " + this.serverAddr);
	}
	
	
	// public methods

	/**
	 * Send Discovery request to server to get list of available sources
	 * 
	 * @param timeout Given timeout
	 * @return Whether succeed to send request
	 */
	@Override
	public boolean sendDiscoveryRequest(int timeout)
	{
		_Logger.info("Discovering resources on CoAP server...");
		this.clientConn.setURI("/.well-known/core");
		this.clientConn.setTimeout((long)timeout);
		if (resources == null){
			resources = new HashSet<>();
		}
		else{
			resources.clear();
		}
		this.clientConn.get(new CoapHandler() {
			@Override
			public void onLoad(CoapResponse response) {
				String[] resourceList = response.getResponseText().split(",");

				for (String resource : resourceList) {
					String url = resource.replace("<", "").replace(">",  "");
					_Logger.info("--> URI: " + url);
					resources.add(url);
				}
			}

			@Override
			public void onError() {
				_Logger.warning("Error processing CoAP response for Discovery Request");
			}
		});
		return true;
	}

	/**
	 * Send Delete request to server to delete specific resource
	 * 
	 * @param resource The resource where to delete
	 * @param enableCON Whether use CON CoAP message
	 * @param timeout Given timeout
	 * @return Whether succeed to send request
	 */
	@Override
	public boolean sendDeleteRequest(ResourceNameEnum resource, boolean enableCON, int timeout)
	{
		CoapResponse response = null;

		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}

		this.clientConn.setTimeout((long)timeout);
		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());
		try {
			response = this.clientConn.delete();
		} catch (ConnectorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (response != null) {
			// TODO: implement your logic here

			_Logger.info("Handling DELETE. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
					response.getCode() + " - " + response.getResponseText());

			if (this.dataMsgListener != null) {
				// TODO: implement this
			}

			return true;
		} else {
			_Logger.warning("Handling DELETE. No response received.");
		}

		return false;
	}

	/**
	 * Send GET request to server to GET specific resource data
	 * 
	 * @param resource The resource where to GET
	 * @param enableCON Whether use CON CoAP message
	 * @param timeout Given timeout
	 * @return Whether succeed to send request
	 */
	@Override
	public boolean sendGetRequest(ResourceNameEnum resource, boolean enableCON, int timeout)
	{
		CoapResponse response = null;

		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}

		this.clientConn.setTimeout((long)timeout);
		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());
		try {
			response = this.clientConn.get();
		} catch (ConnectorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (response != null) {
			// TODO: implement your logic here

			_Logger.info("Handling GET. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
					response.getCode() + " - " + response.getResponseText());

			if (this.dataMsgListener != null) {
				// TODO: implement this
			}

			return true;
		} else {
			_Logger.warning("Handling GET. No response received.");
		}

		return false;
	}

	/**
	 * Send POST request to server to POST specific data to server
	 * 
	 * @param resource The resource where to POST
	 * @param enableCON Whether use CON CoAP message
	 * @param payload Given data to put in the payload
	 * @param timeout Given timeout
	 * @return Whether succeed to send request
	 */
	@Override
	public boolean sendPostRequest(ResourceNameEnum resource, boolean enableCON, String payload, int timeout)
	{
		CoapResponse response = null;

		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}

		this.clientConn.setTimeout((long)timeout);
		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());

		// TODO: determine which MediaTypeRegistry const should be used for this call
		try {
			response = this.clientConn.post(payload, MediaTypeRegistry.TEXT_PLAIN);
		} catch (ConnectorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (response != null) {
			// TODO: implement your logic here

			_Logger.info("Handling POST. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
					response.getCode() + " - " + response.getResponseText());

			if (this.dataMsgListener != null) {
				// TODO: implement this
			}

			return true;
		} else {
			_Logger.warning("Handling POST. No response received.");
		}

		return false;
	}

	/**
	 * Send PUT request to server to PUT specific data to server
	 * 
	 * @param resource The resource where to PUT
	 * @param enableCON Whether use CON CoAP message
	 * @param payload Given data to put in the payload
	 * @param timeout Given timeout
	 * @return Whether succeed to send request
	 */
	@Override
	public boolean sendPutRequest(ResourceNameEnum resource, boolean enableCON, String payload, int timeout)
	{
		CoapResponse response = null;

		if (enableCON) {
			this.clientConn.useCONs();
		} else {
			this.clientConn.useNONs();
		}

		this.clientConn.setTimeout((long)timeout);
		this.clientConn.setURI(this.serverAddr + "/" + resource.getResourceName());

		// TODO: determine which MediaTypeRegistry const should be used for this call
		try {
			response = this.clientConn.put(payload, MediaTypeRegistry.TEXT_PLAIN);
		} catch (ConnectorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (response != null) {
			// TODO: implement your logic here

			_Logger.info("Handling PUT. Response: " + response.isSuccess() + " - " + response.getOptions() + " - " +
					response.getCode() + " - " + response.getResponseText());

			if (this.dataMsgListener != null) {
				// TODO: implement this
			}

			return true;
		} else {
			_Logger.warning("Handling PUT. No response received.");
		}

		return false;
	}

	/**
	 * Set the IDataMessageListener for further data process
	 * @param listener Given IDataMessageListener
	 */
	@Override
	public boolean setDataMessageListener(IDataMessageListener listener)
	{
		this.dataMsgListener = listener;
		return true;
	}

	@Override
	public boolean startObserver(ResourceNameEnum resource, int ttl)
	{
		return false;
	}

	@Override
	public boolean stopObserver(int timeout)
	{
		return false;
	}

	
	// private methods

	/**
	 * Method for init CoapClient instance
	 */
	private void initClient(){
		try {
			this.clientConn = new CoapClient(this.serverAddr);

			_Logger.info("Created client connection to server / resource: " + this.serverAddr);
		} catch (Exception e) {
			_Logger.log(Level.SEVERE, "Failed to connect to broker: " + (this.clientConn != null ? this.clientConn.getURI() : this.serverAddr), e);
		}
	}
}
