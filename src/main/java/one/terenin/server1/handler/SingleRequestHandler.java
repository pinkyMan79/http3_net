package one.terenin.server1.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import one.terenin.server1.kafka.util.DataAccumulator;
import one.terenin.srv_common.dto.DataBundle;
import tech.kwik.flupke.server.HttpRequestHandler;
import tech.kwik.flupke.server.HttpServerRequest;
import tech.kwik.flupke.server.HttpServerResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SingleRequestHandler implements HttpRequestHandler {

    private static final int MAX_DOWNLOAD_SIZE = 128 * 8 * 1024 * 1024;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleRequest(HttpServerRequest request, HttpServerResponse response) throws IOException {
        if (request.method().equals("GET")) {
            String path = request.path();
            OutputStream dataStream = response.getOutputStream();
            if (path.contains("/single")) {
                String[] pathParts = path.split(" ");
                if (path.contains("/flush")) {
                    // need to get all data from accumulator
                    while (!DataAccumulator.dataBundlesQueue.isEmpty()) {
                        dataStream.write(objectMapper
                                .writeValueAsString(DataAccumulator.dataBundlesQueue.poll())
                                .getBytes());
                    }
                    dataStream.write(-1);
                    dataStream.close();
                } else {
                    try {
                        int countOfData = Integer.parseInt(pathParts[pathParts.length - 1]);
                        if (DataAccumulator.dataBundlesQueue.size() >= countOfData) {
                            while (countOfData > 0) {
                                DataBundle poll = DataAccumulator.dataBundlesQueue.poll();
                                dataStream.write(objectMapper.writeValueAsString(poll).getBytes());
                                countOfData --;
                            }
                        }
                        dataStream.write(-1);
                        dataStream.close();
                    } catch (NumberFormatException e) {
                        // send single data
                        dataStream.write(objectMapper.writeValueAsString(DataAccumulator.dataBundlesQueue.poll())
                                .getBytes());

                    } catch (Exception e) {
                        dataStream.write(e.getCause().getMessage().getBytes(StandardCharsets.UTF_8));
                        dataStream.close();
                        response.setStatus(500);
                    }
                }
                response.setStatus(200);
            } else if (path.contains("/pipe")) {
                // with http client for next server here, pipe imitation without server pushes
            } else {
                response.setStatus(404);
                dataStream.write("not a single request".getBytes());
                dataStream.close();
            }
        }
    }
}
