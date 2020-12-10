package programmingtheiot.gda.connection.handlers;

import programmingtheiot.common.ResourceNameEnum;

public class ActuatorResourceHandler extends GenericCoapResourceHandler{
    public ActuatorResourceHandler(ResourceNameEnum resource) {
        super(resource);
    }

    public ActuatorResourceHandler(String resourceName) {
        super(resourceName);
    }
}
