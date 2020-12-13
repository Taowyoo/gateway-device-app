package programmingtheiot.gda.connection.handlers;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import programmingtheiot.common.ResourceNameEnum;
import programmingtheiot.data.DataUtil;
import programmingtheiot.data.SensorData;
import programmingtheiot.data.SystemPerformanceData;

import java.util.logging.Logger;

/**
 * ResourceHandler for SYSTEM PERFORMANCE RESOURCE
 */
public class SysPerfResourceHandler extends GenericCoapResourceHandler {
    // static

    private static final Logger _Logger =
            Logger.getLogger(SysPerfResourceHandler.class.getName());

    public SysPerfResourceHandler(String resourceName, ResourceNameEnum resource) {
        super(resourceName, resource);
    }



    /**
     * Handle arrived SystemPerformanceData from CDA
     *
     * @param context Request that contains json format SystemPerformanceData in payload
     */
    @Override
    public void handlePOST(CoapExchange context) {
        _Logger.info(String.format("Receive a POST from %s payload: %s",
                context.getSourceAddress().toString(),
                context.getRequestText()));
        context.accept();
        ResourceNameEnum resourceNameEnum = ResourceNameEnum.getEnumFromValue(this.getPath());
        SystemPerformanceData systemPerformanceData = DataUtil.getInstance().jsonToSystemPerformanceData(context.getRequestText());
        if (resourceNameEnum != null && systemPerformanceData != null){
            _Logger.info("Handling a SystemPerformanceData");
            this.dataMsgListener.handleSystemPerformanceMessage(resourceNameEnum,systemPerformanceData);
            context.respond(CoAP.ResponseCode.CHANGED);
        }
        else{
            context.respond(CoAP.ResponseCode.BAD_REQUEST);
        }
    }
}
