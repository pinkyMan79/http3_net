/*
 * Copyright © 2023 Peter Doornbosch
 *
 * This file is part of Flupke, a HTTP3 client Java library
 *
 * Flupke is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Flupke is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.luminis.http3.impl;

import net.luminis.http3.server.HttpError;
import net.luminis.quic.QuicConnection;
import net.luminis.quic.QuicStream;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static net.luminis.http3.impl.Http3ConnectionImpl.H3_CLOSED_CRITICAL_STREAM;
import static net.luminis.http3.impl.Http3ConnectionImpl.STREAM_TYPE_PUSH_STREAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class Http3ConnectionImplTest {

    @Test
    public void readFrameShouldThrowErrorWhenDataFrameTooLarge() {
        // Given
        QuicConnection quicConnection = mock(QuicConnection.class);
        Http3ConnectionImpl connection = new Http3ConnectionImpl(quicConnection);
        byte[] data = new byte[10000];
        data[0] = 0x00; // Type: Data frame
        data[1] = 0x4f; // Length: 0x4fff = 4095
        data[2] = (byte) 0xff;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        // When
        assertThatThrownBy(() ->
                connection.readFrame(inputStream, Long.MAX_VALUE, 4094))
                .isInstanceOf(HttpError.class)
                .hasMessageContaining("max data");
    }

    @Test
    public void unknownFrameIsIgnored() throws IOException, HttpError {
        // Given
        QuicConnection quicConnection = mock(QuicConnection.class);
        Http3ConnectionImpl connection = new Http3ConnectionImpl(quicConnection);
        byte[] data = new byte[100];
        data[0] = 0x21; // Type: reserved
        data[1] = 0x09; // Length: 9
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        // When
        Http3Frame frame = connection.readFrame(inputStream, Long.MAX_VALUE, 4094);

        // Then
        assertThat(frame).isInstanceOf(UnknownFrame.class);
        assertThat(inputStream.available()).isEqualTo(89);
    }

    @Test
    public void attemptToRegisterDefaultStreamTypeShouldFail() {
        // Given
        Http3ConnectionImpl connection = new Http3ConnectionImpl(mock(QuicConnection.class));

        // When
        assertThatThrownBy(() -> connection.registerUnidirectionalStreamType(STREAM_TYPE_PUSH_STREAM, mock(Consumer.class)))
                // Then
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("standard");
    }

    @Test
    public void attemptToRegisterReservedStreamTypeShouldFail() {
        // Given
        Http3ConnectionImpl connection = new Http3ConnectionImpl(mock(QuicConnection.class));

        // When
        long reservedType = 0x1f * 3 + 0x21;  // 0x1f * N + 0x21 for non-negative integer values of N
        assertThatThrownBy(() -> connection.registerUnidirectionalStreamType(reservedType, mock(Consumer.class)))
                // Then
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved");
    }

    @Test
    public void registeredHandlerShouldBeCalled() {
        // Given
        Http3ConnectionImpl connection = new Http3ConnectionImpl(mock(QuicConnection.class));
        ByteBuffer buffer = ByteBuffer.allocate(11);
        connection.registerUnidirectionalStreamType(0x22, stream -> {
            try {
                buffer.put(stream.readNBytes(11));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // When
        byte[] streamData = new byte[12];
        streamData[0] = 0x22; // Type: 0x22
        System.arraycopy("Hello World".getBytes(), 0, streamData, 1, 11);
        QuicStream quicStream = mock(QuicStream.class);
        when(quicStream.getInputStream()).thenReturn(new ByteArrayInputStream(streamData));

        connection.handleUnidirectionalStream(quicStream);

        // Then
        assertThat(new String(buffer.array())).isEqualTo("Hello World");
    }

    @Test
    public void closingProcessControlStreamShouldLeadToConnectionError() {
        // Given
        QuicConnection quicConnection = mock(QuicConnection.class);
        Http3ConnectionImpl connection = new Http3ConnectionImpl(quicConnection);

        // When
        connection.processControlStream(new ByteArrayInputStream(new byte[0]));

        // Then
        ArgumentCaptor<Long> errorCaptor = ArgumentCaptor.forClass(Long.class);
        verify(quicConnection).close(errorCaptor.capture(), any());
        assertThat(errorCaptor.getValue()).isEqualTo(H3_CLOSED_CRITICAL_STREAM);
    }

    @Test
    public void closingExtensionControlStreamShouldNotLeadToConnectionError() {
        // Given
        QuicConnection quicConnection = mock(QuicConnection.class);
        Http3ConnectionImpl connection = new Http3ConnectionImpl(quicConnection);
        connection.registerUnidirectionalStreamType(0x22, stream -> {});

        // When
        QuicStream quicStream = mock(QuicStream.class);
        when(quicStream.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 0x40 }));
        connection.handleUnidirectionalStream(quicStream);

        // Then
        verify(quicConnection, never()).close(anyLong(), any());
    }

    @Test
    public void unknownExtensionControlStreamLeadsToQuicStreamClose() {
        // Given
        QuicConnection quicConnection = mock(QuicConnection.class);
        Http3ConnectionImpl connection = new Http3ConnectionImpl(quicConnection);

        // When
        QuicStream quicStream = mock(QuicStream.class);
        when(quicStream.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 0x22 }));
        connection.handleUnidirectionalStream(quicStream);

        // Then
        verify(quicConnection, never()).close(anyLong(), any());
        verify(quicStream).closeInput(anyLong());
    }
}
