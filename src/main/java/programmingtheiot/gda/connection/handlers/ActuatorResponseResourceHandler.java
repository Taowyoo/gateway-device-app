package programmingtheiot.gda.connection.handlers;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;

import java.util.logging.Logger;

/**
 * ResourceHandler for ACTUATOR RESPONSE RESOURCE
 */
public class ActuatorResponseResourceHandler extends GenericCoapResourceHandler {
    // static

    private static final Logger _Logger =
            Logger.getLogger(ActuatorResponseResourceHandler.class.getName());

    public ActuatorResponseResourceHandler(String resourceName, ResourceNameEnum resource) {
        super(resourceName, resource);
    }



    /**
     * Handle arrived response ActuatorData from CDA
     *
     * @param context Request that contains json format ActuatorData in payload
     */
    @Override
    public void handlePOST(CoapExchange context) {
        _Logger.info(String.format("Receive a POST from %s payload: %s",
                context.getSourceAddress().toString(),
                context.getRequestText()));
        context.accept();
        ResourceNameEnum resourceNameEnum = ResourceNameEnum.getEnumFromValue(this.getPath());
        ActuatorData actuatorData = DataUtil.getInstance().jsonToActuatorData(context.getRequestText());
        if (resourceNameEnum != null && actuatorData != null){
            _Logger.info("Handling a ActuatorCommandResponse");
            this.dataMsgListener.handleActuatorCommandResponse(resourceNameEnum,actuatorData);
            context.respond(CoAP.ResponseCode.CHANGED);
        }
        else{
            context.respond(CoAP.ResponseCode.BAD_REQUEST);
        }

    }
}
