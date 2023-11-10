package it.polimi.middleware.kafka.basic;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

public class BasicProducer {
    private static final String defaultTopic = "topicA";

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
            final String key = "Key" + r.nextInt(1000); // va a creare un random key
            final String value = "Val" + i;
            System.out.println(
                    "Topic: " + topic +
                    "\tKey: " + key +
                    "\tValue: " + value
            );

            /*
            La chiave è un identificatore che può essere utilizzato per determinare a quale
            partizione del topic il messaggio deve essere assegnato.

            Partizionamento: Kafka assegna i messaggi alle partizioni dei topic. Il partizionamento è importante per garantire la distribuzione uniforme dei dati tra i
            nodi del cluster Kafka. La chiave di un messaggio viene utilizzata nel processo di partizionamento. Idealmente,
            messaggi con la stessa chiave vengono inviati alla stessa partizione, garantendo che siano elaborati in ordine.

            Garanzia di ordinamento: Se i messaggi condividono la stessa chiave e sono inviati al medesimo topic e partizione,
            Kafka garantisce che essi saranno elaborati in ordine rispetto al timestamp. Questo può essere utile quando è
            importante mantenere un ordine specifico per i messaggi correlati.

            Ottimizzazioni di lettura/scrittura: La chiave può essere sfruttata per ottimizzare la lettura e la scrittura dei dati,
            poiché determina come i dati vengono distribuiti tra le partizioni.
             */

            final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            // ritorna il Future che contiene metadata dove è stato memorizzato il messaggio partition e offset
            final Future<RecordMetadata> future = producer.send(record);
            // DI DEFAULT ASPETTO ACK

            if (waitAck) {
                try {
                    /*
                    il metodo di produzione di solito restituisce un oggetto Future che rappresenta il risultato
                    asincrono dell'operazione di produzione del messaggio.

                    future.get() è un'operazione di blocco che ottiene il risultato (nel tuo caso, un oggetto di tipo RecordMetadata)
                    dal futuro (Future). Questo blocca l'esecuzione del thread fino a quando il risultato non è disponibile.

                    RecordMetadata è una classe in Kafka che fornisce informazioni sull'offset, partizione e altro ancora per
                    un messaggio prodotto con successo. Quindi, l'oggetto ack conterrà metadati relativi al messaggio prodotto.

                    In generale, questo tipo di operazione è comune quando si desidera attendere il completamento della produzione
                    di un messaggio e ottenere informazioni su quel messaggio prodotto.
                     */
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