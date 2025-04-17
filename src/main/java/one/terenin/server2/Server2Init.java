package one.terenin.server2;

import lombok.SneakyThrows;
import one.terenin.server1.handler.SingleRequestHandler;
import one.terenin.srv_common.Init;

public class Server2Init {
    @SneakyThrows
    public static void main(String[] args) {
        Init.start(8877, true, new SingleRequestHandler(), null, null);
    }
}
