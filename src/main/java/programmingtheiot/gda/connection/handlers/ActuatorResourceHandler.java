package programmingtheiot.gda.connection.handlers;

import programmingtheiot.common.ResourceNameEnum;

public class SensorResourceHandler extends GenericCoapResourceHandler{
    public SensorResourceHandler(ResourceNameEnum resource) {
        super(resource);
    }

    public SensorResourceHandler(String resourceName) {
        super(resourceName);
    }
}
