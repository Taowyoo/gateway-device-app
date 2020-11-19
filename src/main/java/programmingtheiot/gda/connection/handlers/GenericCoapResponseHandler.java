/**
 * This class is part of the Programming the Internet of Things
 * project, and is available via the MIT License, which can be
 * found in the LICENSE file at the top level of this repository.
 * <p>
 * Copyright (c) 2020 by Andrew D. King
 */
package programmingtheiot.gda.connection.handlers;

import java.util.logging.Logger;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.OptionSet;

import programmingtheiot.common.IDataMessageListener;


/**
 * Generic CoAP response handler implementation
 * Implement basic logic to handle CoAP response from Server
 *
 */
public class GenericCoapResponseHandler implements CoapHandler {
    // static

    private static final Logger _Logger =
            Logger.getLogger(GenericCoapResponseHandler.class.getName());

    // params

    private IDataMessageListener dataMsgListener = null;


    // constructors

    /**
     * Default Constructor
     *
     */
    public GenericCoapResponseHandler() {
        this((IDataMessageListener) null);
    }

    /**
     * Constructor, init instance with given IDataMessageListener
     * @param listener Given IDataMessageListener
     */
    public GenericCoapResponseHandler(IDataMessageListener listener) {
        super();

        dataMsgListener = listener;

        _Logger.fine("Response handler created. IDataMessageListener is " + (listener != null ? "set" : "not set"));
    }


    // public methods

    /**
     * Implements the CoapHandler interface to load CoapResponse
     * @param response Given CoapResponse
     */
    @Override
    public void onLoad(CoapResponse response) {
        if (response != null) {
            OptionSet options = response.getOptions();

            // for debugging only
            _Logger.finest("Processing CoAP response. Options: " + options);
            _Logger.finest("Processing CoAP response. MID: " + response.advanced().getMID());
            _Logger.finest("Processing CoAP response. Token: " + response.advanced().getTokenString());
            _Logger.finest("Processing CoAP response. Code: " + response.getCode());


            // TODO: parse payload and notify listener
            _Logger.info(" --> Payload: " + response.getResponseText());

            if (this.dataMsgListener != null) {
                // TODO: send listener the response
            }
        } else {
            _Logger.warning("No CoAP response to process. Response is null.");
        }
    }


    /**
     * Implements the CoapHandler interface to handle error
     */
    @Override
    public void onError() {
        // TODO: handle this
        _Logger.warning("Error processing CoAP response. Ignoring.");
    }

}