package programmingtheiot.gda.connection.handlers;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.ActuatorData;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;

import java.util.logging.Logger;

/**
 * ResourceHandler for SENSOR DATA RESOURCE
 */
public class SensorResourceHandler extends GenericCoapResourceHandler {
    // static

    private static final Logger _Logger =
            Logger.getLogger(SensorResourceHandler.class.getName());

    public SensorResourceHandler(String resourceName, ResourceNameEnum resource) {
        super(resourceName, resource);
    }



    /**
     * Handle arrived SensorData from CDA
     *
     * @param context Request that contains json format SensorData in payload
     */
    @Override
    public void handlePOST(CoapExchange context) {
        _Logger.info(String.format("Receive a POST from %s payload: %s",
                context.getSourceAddress().toString(),
                context.getRequestText()));
        context.accept();
        ResourceNameEnum resourceNameEnum = ResourceNameEnum.getEnumFromValue(this.getPath());
        SensorData sensorData = DataUtil.getInstance().jsonToSensorData(context.getRequestText());
        if (resourceNameEnum != null && sensorData != null){
            _Logger.info("Handling a SensorData");
            this.dataMsgListener.handleSensorMessage(resourceNameEnum,sensorData);
            context.respond(CoAP.ResponseCode.CHANGED);
        }
        else{
            context.respond(CoAP.ResponseCode.BAD_REQUEST);
        }
    }

}
