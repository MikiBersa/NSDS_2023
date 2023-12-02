package it.polimi.middleware.spark.streaming.wordcount;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

public class StreamingWordCount {
    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String socketHost = args.length > 1 ? args[1] : "localhost";
        final int socketPort = args.length > 2 ? Integer.parseInt(args[2]) : 9999;

        final SparkConf conf = new SparkConf().setMaster(master).setAppName("StreamingWordCountSum");
        // DEFINISCO CHE HO UNO STREAMING IN ENTRATA
        // DURATION CREA UN NUOVO RDD OGNI 1 SECONDO che contiene tutti i messaggi inivati in quel secondo
        // QUESTO LO FACCIO A LIVELLO CONTESTO QUINDI NON POSSO MODIFICARLO A LIVELLO DI JavaPairDStream
        // IN TAL CASO DEVO USARE WINDOWS
        final JavaStreamingContext sc = new JavaStreamingContext(conf, Durations.seconds(1));

        final JavaPairDStream<String, Integer> counts = sc
                .socketTextStream(socketHost, socketPort)
                // USATO GROUP ELEMENTS TOGHERS
                // SIZE DI VISIONE, HOW FREQUENTLY DEVE ESSERE VALUTATA LA FINESTRA
                // TIPO GESTIONE DELLO STATO PERCHè TIENE IN MEMORIA TUTTI GLI  ULTIMI DATI ENTRO I 10 SECONDI
                // VISIONATI I DATI OGNI 5 SECONDI
                .window(Durations.seconds(10), Durations.seconds(5))
                .map(String::toLowerCase)
                // TODO HO STREAM OF WORDS NON STRUTTURATO
                .flatMap(line -> Arrays.asList(line.split(" ")).iterator())
                // creo da 1
                .mapToPair(s -> new Tuple2<>(s, 1))
                // stessi key faccio la somma dei suoi valori from the same word
                .reduceByKey((a, b) -> a + b);
        sc.sparkContext().setLogLevel("ERROR");

        // SATRTS NEW JOB PER OGNI RDD SE HA QUALCOSA DENTRO
        // se faccio n parole in 1 secondo prende tutti quei elementi e ci fa
        // la somma in quei 1 sec
        counts.foreachRDD(rdd -> rdd
                .collect()
                .forEach(System.out::println)
        );

        sc.start();

        try {
            sc.awaitTermination();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        sc.close();
    }
}