package one.terenin.rpc.generator;

import java.util.HashMap;
import java.util.Map;

public class RpcDispatcher {
    private final Map<String, RpcHandler> routes = new HashMap<>();

    public void register(String uri, RpcHandler handler) {
        routes.put(uri, handler);
    }

    public RpcHandler getHandler(String uri) {
        return routes.get(uri);
    }
}