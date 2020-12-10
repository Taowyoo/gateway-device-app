package programmingtheiot.gda.connection.handlers;

import programmingtheiot.common.ResourceNameEnum;

public class SysPerfResourceHandler extends GenericCoapResourceHandler{
    public SysPerfResourceHandler(ResourceNameEnum resource) {
        super(resource);
    }

    public SysPerfResourceHandler(String resourceName) {
        super(resourceName);
    }
}
