package programmingtheiot.gda.connection.handlers;

import programmingtheiot.common.ResourceNameEnum;

public class ActuatorResponseResourceHandler extends GenericCoapResourceHandler{
    public ActuatorResponseResourceHandler(ResourceNameEnum resource) {
        super(resource);
    }

    public ActuatorResponseResourceHandler(String resourceName) {
        super(resourceName);
    }
}
