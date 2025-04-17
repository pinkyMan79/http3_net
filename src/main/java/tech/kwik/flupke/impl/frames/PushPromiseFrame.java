package tech.kwik.flupke.impl.frames;

import tech.kwik.core.generic.InvalidIntegerEncodingException;
import tech.kwik.core.generic.VariableLengthInteger;
import tech.kwik.flupke.impl.frames.base.Http3Frame;
import tech.kwik.qpack.Decoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PushPromiseFrame extends Http3Frame {

    public static final byte GOAWAY_FRAME_TYPE = 0x05;

    private long pushId;
    private HttpHeaders httpHeaders;

    public PushPromiseFrame() {
        this.httpHeaders = HttpHeaders.of(Collections.emptyMap(), (a, b) -> true);
    }

    public PushPromiseFrame parsePayload(byte[] headerBlock, Decoder decoder) throws IOException {
        ByteBuffer wrap = ByteBuffer.wrap(headerBlock);
        try {
            this.pushId = VariableLengthInteger.parseLong(wrap);
        } catch (InvalidIntegerEncodingException e) {
            throw new RuntimeException(e);
        }
        List<Map.Entry<String, String>> headersList = decoder.decodeStream(new ByteArrayInputStream(headerBlock));
        Map<String, List<String>> headersMap = headersList.stream()
                .collect(Collectors.toMap(Map.Entry::getKey, this::mapValue, this::mergeValues));
        // https://www.rfc-editor.org/rfc/rfc9114#name-http-control-data
        // "Pseudo-header fields are not HTTP fields."
        httpHeaders = HttpHeaders.of(headersMap, (key, value) -> ! key.startsWith(":"));
        return this;
    }

    private List<String> mapValue(Map.Entry<String, String> entry) {
        return List.of(entry.getValue());
    }

    private List<String> mergeValues(List<String> value1, List<String> value2) {
        List<String> result = new ArrayList<>();
        result.addAll(value1);
        result.addAll(value2);
        return result;
    }

}
