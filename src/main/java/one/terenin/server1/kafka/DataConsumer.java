package one.terenin.server1.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import one.terenin.server1.kafka.util.DataAccumulator;
import one.terenin.srv_common.dto.DataBundle;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DataConsumer {

    private final Properties properties;
    private final ObjectMapper objectMapper;

    public DataConsumer(String bootstrapServers, String groupId) {
        this.properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        objectMapper = new ObjectMapper();
    }

    public KafkaConsumer<String, String> initCli(String topic, String partition) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        addShutdown(consumer);
        Thread loader = new Thread(() -> {
            while (true) {
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    if (record.partition() == 2 || record.partition() == 3) {
                        String value = record.value();
                        try {
                            DataBundle[] dataBundles = objectMapper.readValue(value, DataBundle[].class);
                            DataAccumulator.dataBundlesQueue.addAll(Stream.of(dataBundles)
                                    .collect(Collectors.toList()));
                        } catch (JsonProcessingException e) {
                            // not an array of json data
                            try {
                                DataBundle dataBundle = objectMapper.readValue(value, DataBundle.class);
                                DataAccumulator.dataBundlesQueue.add(dataBundle);
                            } catch (JsonProcessingException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }
            }
        });
        return consumer;
    }

    private void addShutdown(KafkaConsumer<?, ?> consumer) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Detected a shutdown, let's exit by calling consumer.wakeup()...");
            consumer.wakeup();

            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }
}
