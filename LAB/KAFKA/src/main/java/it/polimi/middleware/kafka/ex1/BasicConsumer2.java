package it.polimi.middleware.kafka.ex1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BasicConsumer2 {
    private static final String defaultGroupId = "groupB";
    private static final String defaultTopic = "topicC1";
    private static final String defaultTopic2 = "topicC2";


    private static final String serverAddr = "localhost:9092";
    private static final boolean autoCommit = true;
    private static final int autoCommitIntervalMs = 15000;

    // Default is "latest": try "earliest" instead
    // QUANDO MESSO latest PARTE DAL LEGGERE ULTIMO OFFSET CHE è STATO MANDATO
    // SE METTO earliest -> RICARICA TUTTI I MESSAGGI MEMORIZZATI PRIMA DEL CARICAMENTO
    private static final String offsetResetStrategy = "latest";

    public static void main(String[] args) {
        // If there are arguments, use the first as group and the second as topic.
        // Otherwise, use default group and topic.
        String groupId = args.length >= 1 ? args[0] : defaultGroupId;
        String topic = args.length >= 2 ? args[1] : defaultTopic;

        final Properties props = new Properties();
        // LIST OF SERVER THAT THE CONSUMER CONNECTS TO
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        // SE DUE CONSUMER METTONO LO STESSO ID DI GRUPPO ALLORA APPARTENGONO ALLO STESSO GRUPPO
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // OFFSET DEL MESSAGGIO CHE LEGGO VIENE MEMORIZZATO / AGGIORNATO PERIODICAMENTE autoCommit NON DOPO OGNI LETTURA
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(autoCommitIntervalMs));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        final Properties propsp = new Properties();
        propsp.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        // gli passo la serializzazione per mandare i messaggi per key e string
        propsp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // INIZIALIZZAZIONE DEL CONSUMER COMPONENTS
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        final KafkaProducer<String, Integer> producer = new KafkaProducer<>(propsp);
        HashMap<String,Integer> storagemex =new HashMap<>();
        // IN QUESTO CASO SOLO IL TOPICA
        consumer.subscribe(Collections.singletonList(topic));
        while (true) {
            // è UN BLOCK METHOD CHE MI SBLOCCO SOLO QUANDO C'è UN NUOVO TOPIC O C'è UN LIMITE DI TIMEOUT
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            for (final ConsumerRecord<String, String> record : records) {
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tValue: " + record.value().toLowerCase()
                );
                if(storagemex.containsKey(record.key())){
                    int x=storagemex.get(record.key());
                    x++;
                    storagemex.put(record.key(),x);
                }
                else{
                    storagemex.put(record.key(), 1);
                }
                final ProducerRecord<String, Integer> r = new ProducerRecord<>(defaultTopic2, storagemex.get(record.key()));
                producer.send(r);
                final Future<RecordMetadata> future = producer.send(r);
                // DI DEFAULT ASPETTO ACK
                try {
                        RecordMetadata ack = future.get();
                        System.out.println("Ack for topic " + ack.topic() + ", partition " + ack.partition() + ", offset " + ack.offset());
                    } catch (InterruptedException | ExecutionException e1) {
                        e1.printStackTrace();
                    }


            }

        }
    }
}