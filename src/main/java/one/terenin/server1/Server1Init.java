package one.terenin.server1;

import lombok.SneakyThrows;
import one.terenin.server1.handler.SingleRequestHandler;
import one.terenin.server1.kafka.DataConsumer;
import one.terenin.srv_common.Init;

public class Server1Init {
    @SneakyThrows
    public static void main(String[] args) {
        DataConsumer consumer = new DataConsumer("", "");
        consumer.initCli("", "");
        SingleRequestHandler requestHandler = new SingleRequestHandler();
        Init.start(8776, true, requestHandler, null, null);
    }
}
