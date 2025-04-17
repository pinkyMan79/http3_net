package tech.kwik.flupke.impl.frames;

import tech.kwik.core.generic.InvalidIntegerEncodingException;
import tech.kwik.core.generic.VariableLengthInteger;
import tech.kwik.flupke.impl.frames.base.Http3Frame;

import java.nio.ByteBuffer;

public final class GoAwayFrame extends Http3Frame {

    public static final byte GOAWAY_FRAME_TYPE = 0x07;

    private long streamId;

    public GoAwayFrame() {
    }

    public GoAwayFrame parseFromBytes(ByteBuffer data) {
        try {
            this.streamId = VariableLengthInteger.parseLong(data);
            return this;
        } catch (InvalidIntegerEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public long getStreamId() {
        return streamId;
    }

}
