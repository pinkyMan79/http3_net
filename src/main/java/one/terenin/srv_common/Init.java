package one.terenin.srv_common;

import tech.kwik.core.QuicConnection;
import tech.kwik.core.log.SysOutLogger;
import tech.kwik.core.server.ServerConnectionConfig;
import tech.kwik.core.server.ServerConnector;
import tech.kwik.flupke.server.Http3ApplicationProtocolFactory;
import tech.kwik.flupke.server.HttpRequestHandler;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class Init {

    public static void start(int port, boolean retry, HttpRequestHandler requestHandler, File certificate, File key) throws Exception {

        ServerConnectionConfig serverConnectionConfig = ServerConnectionConfig.builder()
                .maxIdleTimeoutInSeconds(30)
                .maxUnidirectionalStreamBufferSize(1_000_000)
                .maxBidirectionalStreamBufferSize(1_000_000)
                .maxConnectionBufferSize(10_000_000)
                .maxOpenPeerInitiatedUnidirectionalStreams(10)
                .maxOpenPeerInitiatedBidirectionalStreams(100)
                .retryRequired(retry)
                .connectionIdLength(8)
                .build();

        SysOutLogger log = new SysOutLogger();
        log.logInfo(true);

        ServerConnector serverConnector = ServerConnector.builder()
                .withPort(port)
                .withCertificate(new FileInputStream(certificate), new FileInputStream(key))
                .withSupportedVersions(List.of(QuicConnection.QuicVersion.V1))
                .withConfiguration(serverConnectionConfig)
                .withLogger(log)
                .build();

        Http3ApplicationProtocolFactory http3ApplicationProtocolFactory = new Http3ApplicationProtocolFactory(requestHandler);
        serverConnector.registerApplicationProtocol("h3", http3ApplicationProtocolFactory);
        serverConnector.start();
    }

}
