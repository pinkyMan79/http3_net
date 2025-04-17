package one.terenin.rpc.generator;

@FunctionalInterface
public interface RpcHandler {
    byte[] handle(byte[] inputBytes) throws Exception;
}