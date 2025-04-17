package one.terenin.server2.handler;

import tech.kwik.flupke.server.HttpRequestHandler;
import tech.kwik.flupke.server.HttpServerRequest;
import tech.kwik.flupke.server.HttpServerResponse;

import java.io.IOException;

public class SingleRequestHandler implements HttpRequestHandler {

    private static final int MAX_DOWNLOAD_SIZE = 128 * 8 * 1024 * 1024;

    @Override
    public void handleRequest(HttpServerRequest request, HttpServerResponse response) throws IOException {

    }
}
