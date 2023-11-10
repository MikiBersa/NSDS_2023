package it.polimi.middleware.kafka.transactional;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;


// VENGONO USATE TRANSACTION -> WRITES FANNO PARTE DEL TRANSACTION
public class TransactionalProducer {
    private static final String defaultTopic = "topicA";

    private static final int numMessages = 100000;
    private static final int waitBetweenMsgs = 500;

    // UNIQUE TRANSACTIONAL ID -> PASSATO COME PARAMETRO DEL PROPS
    private static final String transactionalId = "myTransactionalId";

    private static final String serverAddr = "localhost:9092";

    public static void main(String[] args) {
        // If there are no arguments, publish to the default topic
        // Otherwise publish on the topics provided as argument
        List<String> topics = args.length < 1 ?
                Collections.singletonList(defaultTopic) :
                Arrays.asList(args);

        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddr);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionalId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        final KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        final Random r = new Random();

        // This must be called before any method that involves transactions
        producer.initTransactions();

        for (int i = 0; i < numMessages; i++) {
            final String topic = topics.get(r.nextInt(topics.size()));
            final String key = "Key" + r.nextInt(1000);
            final String value = "Val" + i;
            System.out.println(
                    "Topic: " + topic +
                    "\tKey: " + key +
                    "\tValue: " + value
            );

            final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

            producer.beginTransaction();
            producer.send(record);
            if (i % 2 == 0) {
                // IN QUESTO CASO FA IL COMMIT E QUINDI CHIUDE LA TRANSACTION

                /*
                Questo metodo viene utilizzato quando si sta lavorando con transazioni in Kafka e si desidera confermare
                e rendere permanente un insieme di messaggi prodotti in una transazione.
                 */
                producer.commitTransaction();
            } else {
                // If not flushed, aborted messages are deleted from the outgoing buffer
                producer.flush();

                /*
                Il metodo flush() in Apache Kafka viene utilizzato per assicurarsi che tutti i messaggi prodotti dal
                produttore siano inviati al broker prima di chiudere il produttore o di terminare l'applicazione.
                Questo metodo è particolarmente utile quando si desidera sincronizzare la produzione di messaggi e
                assicurarsi che siano effettivamente inviati al broker prima di procedere con altre operazioni.

                Chiamare flush() forza il produttore Kafka a inviare tutti i messaggi accumulati in memoria al server Kafka.
                Questo può essere utile in scenari in cui è richiesta una produzione di messaggi sincrona o quando è necessario
                garantire che i messaggi siano pronti prima di eseguire operazioni successive.
                 */

                // MA EFFETTIVAMENTE NON LI MANDA I MESSAGGI DISAPRI
                producer.abortTransaction();
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