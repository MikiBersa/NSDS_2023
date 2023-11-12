package it.polimi.middleware.kafka.ex4;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BasicProducer1 {
    private static final String defaultTopic = "topicC1";

    private static final int numMessages = 100000;
    private static final int waitBetweenMsgs = 500;
    private static final boolean waitAck = true;

    // connessione al singolo broker locale
    private static final String serverAddr = "localhost:9092";

    public static void main(String[] args) {
        // If there are no arguments, publish to the default topic
        // Otherwise publish on the topics provided as argument
        List<String> topics = args.length < 1 ?
                Collections.singletonList(defaultTopic) :
                Arrays.asList(args);

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        // gli passo la serializzazione per mandare i messaggi per key e string
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // ISTANZIARE KAFKA PRODUCER BISONGA CREARE L'OGGETTO KafkaProducer
        // String -> key and value per il producer
        final KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        final Random r = new Random();

        for (int i = 0; i < numMessages; i++) {
            final String topic = topics.get(r.nextInt(topics.size()));
            final String key = "Key" + r.nextInt(5); // va a creare un random key
            final String value = "Val " + i;
            System.out.println(
                    "Topic: " + topic +
                            "\tKey: " + key +
                            "\tValue: " + value
            );

            final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            // ritorna il Future che contiene metadata dove è stato memorizzato il messaggio partition e offset
            final Future<RecordMetadata> future = producer.send(record);
            // DI DEFAULT ASPETTO ACK

            if (waitAck) {
                try {
                    RecordMetadata ack = future.get();
                    System.out.println("Ack for topic " + ack.topic() + ", partition " + ack.partition() + ", offset " + ack.offset());
                } catch (InterruptedException | ExecutionException e1) {
                    e1.printStackTrace();
                }
            }

            try {
                Thread.sleep(waitBetweenMsgs);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }
}
