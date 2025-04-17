package one.terenin.server1.kafka.util;

import one.terenin.srv_common.dto.DataBundle;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DataAccumulator {

    public static ConcurrentLinkedQueue<DataBundle> dataBundlesQueue = new ConcurrentLinkedQueue<>();

}
