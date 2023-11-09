package it.polimi.middleware.kafka.basic;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

public class BasicConsumerManual {
    private static final String defaultGroupId = "groupA";
    private static final String defaultTopic = "topicA";

    private static final String serverAddr = "localhost:9092";
    private static final boolean autoCommit = false;
    //  VIENE FATTO IL COMMIT DOPO 100 MESSAGGI
    private static final int commitEvery = 100;

    // Default is "latest": try "earliest" instead
    private static final String offsetResetStrategy = "latest";

    public static void main(String[] args) {
        // If there are arguments, use the first as group and the second as topic.
        // Otherwise, use default group and topic.
        String groupId = args.length >= 1 ? args[0] : defaultGroupId;
        String topic = args.length >= 2 ? args[1] : defaultTopic;

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        // VADO A GESTIRLO MANUALMENTE
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(autoCommit));

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetResetStrategy);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        int numConsumed = 0;
        while (true) {
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            for (final ConsumerRecord<String, String> record : records) {
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tValue: " + record.value()
                );
                numConsumed++;
                if (numConsumed == commitEvery) {
                    numConsumed = 0;
                    // It is also possible to commit for individual partitions
                    // Asynchronous versions with callback functions also available
                    consumer.commitSync();

                    /*
                    Quando un consumatore Kafka legge un messaggio da un topic, può mantenere internamente un offset,
                    che è una posizione specifica in una partizione del topic. Questo offset indica fino a quale
                    punto il consumatore ha letto i messaggi. Dopo aver elaborato un insieme di messaggi, il
                    consumatore può fare commit dell'offset, segnalando a Kafka che quei messaggi sono stati processati con successo.

                     Il commit dell'offset è cruciale per garantire l'affidabilità nel processo di consumo dei messaggi.
                     Se un consumatore non fa commit degli offset, Kafka non sa fino a quale punto il consumatore ha letto i messaggi.
                     In caso di un'interruzione o un riavvio, il consumatore potrebbe iniziare a leggere nuovamente i messaggi già processati,
                     portando a duplicati o perdita di dati.
                     */
                }
            }
        }
    }
}