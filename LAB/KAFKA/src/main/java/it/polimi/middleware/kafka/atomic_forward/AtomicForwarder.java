package it.polimi.middleware.kafka.atomic_forward;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AtomicForwarder {
    // è UN CONSUMATORE ED UTILIZZATORE ALLO STESSO TEMPO
    // se crashia riparte da dove ha lasciato
    private static final String defaultConsumerGroupId = "groupA";
    private static final String defaultInputTopic = "topicA";
    private static final String defaultOutputTopic = "topicB";

    private static final String serverAddr = "localhost:9092";

    // transaction per commit l'offset del messaggio letto da topicA e push di TopicB come parte della transazione
    private static final String producerTransactionalId = "forwarderTransactionalId";

    public static void main(String[] args) {
        // If there are arguments, use the first as group, the second as input topic, the third as output topic.
        // Otherwise, use default group and topics.
        String consumerGroupId = args.length >= 1 ? args[0] : defaultConsumerGroupId;
        String inputTopic = args.length >= 2 ? args[1] : defaultInputTopic;
        String outputTopic = args.length >= 3 ? args[2] : defaultOutputTopic;

        // Consumer
        final Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        // The consumer does not commit automatically, but within the producer transaction
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(false));

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList(inputTopic));

        // Producer
        final Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, producerTransactionalId);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, String.valueOf(true));

        final KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
        producer.initTransactions();

        while (true) {
            // QUI GLI ARRIVA PIù MESSAGGI DI SEGUITO QUANDO IL PRODUCER INVIA PIù MESSAGGI UNO DIESTRO L'ALTRO NEL PROCESSO
            final ConsumerRecords<String, String> records = consumer.poll(Duration.of(5, ChronoUnit.MINUTES));
            ProducerRecord<String, String> recordR = null;

            producer.beginTransaction();
            for (final ConsumerRecord<String, String> record : records) {
                System.out.println("Partition: " + record.partition() +
                        "\tOffset: " + record.offset() +
                        "\tKey: " + record.key() +
                        "\tValue: " + record.value()
                );
                recordR = new ProducerRecord<>(outputTopic, record.key(), record.value());
                producer.send(recordR);
            }

            // The producer manually commits the offsets for the consumer within the transaction
            final Map<TopicPartition, OffsetAndMetadata> map = new HashMap<>();
            System.out.println(records);
            for (final TopicPartition partition : records.partitions()) {
                System.out.println(partition);
                final List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                System.out.println(partitionRecords + " " + partitionRecords.size());
                final long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                System.out.println(lastOffset);
                // AGGIUNGO UNO COSì QUANDO IL CONSUMER RI PARTE -> VA A LEGGERE DIRETTAMENTE IL NUOVO VALORE
                // INFATTI QUANDO RIFACCIO RIPARTIRE QUESTO PROCESSO STA IN ASCOLTO E NON CARICA I MESSAGGI PRECEDENTI PERCHè IL SUO OFFSET è QUELLO NUOVO DEL MESSAGGIO CHE DEVE ARRIVARE
                // INFATTI NON DOVREBBE PROCESSARE MESSAGGI VECCHI PER LA POLITICA  EOS => QUINDI LI PROCESSA UNA SOLA VOLTA QUANDO ARRIVA E PER QUESTO QUANDO IL PROCESSO
                // RESUSCITA ALLORA STA IN ASCOLTO E NN RIPROCESSA I MESSAGGI INDIETRO
                // QUESTO FATTO DI ESSERE IN ASCOLTO DEL NUOVO MESSAGGIO LO POSSO FARE SOLO CON lastOffset + 1
                map.put(partition, new OffsetAndMetadata(lastOffset + 1));
                System.out.println("INTERMEDIA: "+map);
            }

            System.out.println("MAPPA: "+map);

            producer.sendOffsetsToTransaction(map, consumer.groupMetadata());
            producer.commitTransaction();

            // GARANTISCE END TO END SEMANTIC COMUNICATION
        }
    }
}