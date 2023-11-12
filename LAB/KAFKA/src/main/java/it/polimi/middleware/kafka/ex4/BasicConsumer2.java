package it.polimi.middleware.kafka.ex4;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class BasicConsumer2 {
    // IN QUESTO CASO USO DUE CONSUMER NELLO STESSO GRUPPO E QUINDI CIASCUNO LEGGE DA DUE PARTIZIONI
    // IN MODO PARALLELRO RICORDO CHE DI SOLITO I MESSAGGI CON LA STESSA CHIAVE VIENE  MESSO NELLA STESSA PARTIZIONE
    // QUINDI LA CHIAVE DIVIDE UN PO' I MESSAGGI NELLE VARIE PARTIZIONI
    // DI SOLITO LEGAME 1:1 TRA PARTIZIONE E CONSUMER

    // QUINDI IN QUESTO CASO ANDRò AD UTILIZZARE 2 PARTIZIONI, UNA PARTIZIONE PER CONSUMER

    // IN QUESTO CASO ANCHE SE è GRUPPO A COME CONSUMER1 NON LO BLOCCA PERCHè ASCOLTANO TOPIC DIFFERENTI
    private static final String defaultGroupId = "groupB";
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
        String topic = args.length >= 2 ? args[1] : defaultTopic2;

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
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Collections.singletonList(topic));
        while (true) {
            // è UN BLOCK METHOD CHE MI SBLOCCO SOLO QUANDO C'è UN NUOVO TOPIC O C'è UN LIMITE DI TIMEOUT
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));

            records.forEach(record -> {
                String jsonString = record.value();

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    // poi da json string la ristrasformo in hash map
                    Map<String, String> hashMap = objectMapper.readValue(jsonString, Map.class);

                    // Ora puoi lavorare con la tua HashMap
                    System.out.println("HashMap ricevuta: " + hashMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }
    }
}