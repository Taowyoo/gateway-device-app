package programmingtheiot.gda.connection.handlers;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;

import java.util.logging.Logger;

/**
 * ResourceHandler for ACTUATOR CMD RESOURCE
 */
public class ActuatorResourceHandler extends GenericCoapResourceHandler {
    // static

    private static final Logger _Logger =
            Logger.getLogger(ActuatorResourceHandler.class.getName());

    private ActuatorData lastActuatorData;

    public ActuatorResourceHandler(String resourceName, ResourceNameEnum resource) {
        super(resourceName, resource);
    }

    @Override
    public void handleGET(CoapExchange context) {
        _Logger.info(String.format("Receive a DELETE from %s, payload: %s",
                context.getSourceAddress().toString(),
                new String(context.getRequestPayload())));
        // accept the request
        context.accept();
        synchronized (lastActuatorData){
            if (lastActuatorData!=null){

            }
            else {
                context.respond(CoAP.ResponseCode.NOT_FOUND, "No new ActuatorData Cmd");
            }
        }

        // send an appropriate response

    }

    @Override
    public void handlePOST(CoapExchange context) {
        _Logger.info(String.format("Receive a DELETE from %s, payload: %s",
                context.getSourceAddress().toString(),
                new String(context.getRequestPayload())));
        // accept the request
        context.accept();
        ActuatorData data = DataUtil.getInstance().jsonToActuatorData(context.getRequestText());
        if (data != null){
            synchronized (lastActuatorData){
                lastActuatorData = data;
                context.respond(CoAP.ResponseCode.CHANGED);
            }
        }
        else{
            context.respond(CoAP.ResponseCode.BAD_REQUEST, "Invalid json of ActuatorData");
        }


    }

    private void initResource(){
        setObservable(true); // enable observing
        setObserveType(CoAP.Type.CON); // configure the notification type to CONs
        getAttributes().setObservable(); // mark observable in the Link-Format
    }
}
