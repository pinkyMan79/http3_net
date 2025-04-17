package one.terenin.rpc.generator;

import one.terenin.rpc.spec.EndpointSpec;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.function.Function;

public class RpcHandlerGenerator {

    public static RpcHandler generateFromSpec(EndpointSpec spec,
                                              Class<?> inputType,
                                              Class<?> outputType) {
        Function<?, ?> fn = spec.getProcessingFunction();

        boolean isArrayIn = false;
        boolean isArrayOut = false;

        return (inputBytes) -> {
            try {
                Object[] inputs;

                if (isArrayIn) {
                    inputs = deserializeArray(inputType, inputBytes);
                } else {
                    Object singleInput = deserializeSingle(inputType, inputBytes);
                    inputs = new Object[]{singleInput};
                }

                Object result = ((Function<Object, Object>) fn).apply(isArrayIn ? inputs : inputs[0]);

                if (isArrayOut) {
                    return serializeArray((Object[]) result);
                } else {
                    return serializeSingle(result);
                }

            } catch (Exception e) {
                throw new RuntimeException("RPC invoke failed", e);
            }
        };
    }

    private static Object deserializeSingle(Class<?> type, byte[] input) throws Exception {
        Method parse = type.getMethod("getRootAs" + type.getSimpleName(), ByteBuffer.class);
        return parse.invoke(null, ByteBuffer.wrap(input));
    }

    private static byte[] serializeSingle(Object obj) throws Exception {
        Method bbMethod = obj.getClass().getMethod("getByteBuffer");
        ByteBuffer buffer = (ByteBuffer) bbMethod.invoke(obj);
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    // TODO: расширяем на массивы
    private static Object[] deserializeArray(Class<?> elementType, byte[] input) {
        throw new UnsupportedOperationException("Array input not implemented yet.");
    }

    private static byte[] serializeArray(Object[] objs) {
        throw new UnsupportedOperationException("Array output not implemented yet.");
    }
}