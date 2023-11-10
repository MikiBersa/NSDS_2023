package it.polimi.middleware.kafka.ex1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

public class BasicConsumer1 {
    private static final String defaultGroupId = "groupA";
    private static final String defaultTopic = "topicC2";

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

        // INIZIALIZZAZIONE DEL CONSUMER COMPONENTS
        KafkaConsumer<String, Integer> consumer = new KafkaConsumer<>(props);
        // IN QUESTO CASO SOLO IL TOPICA
        consumer.subscribe(Collections.singletonList(topic));
        while (true) {
            // è UN BLOCK METHOD CHE MI SBLOCCO SOLO QUANDO C'è UN NUOVO TOPIC O C'è UN LIMITE DI TIMEOUT
            final ConsumerRecords<String, Integer> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            for (final ConsumerRecord<String, Integer> record : records) {
                System.out.print("Consumer group: " + groupId + "\t");
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tNumb: " + record.value()
                );
            }
        }
    }
}