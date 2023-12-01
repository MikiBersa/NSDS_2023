package it.polimi.middleware.spark.batch.wordcount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;

public class WordCount {

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        // final String filePath = Objects.requireNonNull(WordCount.class.getResource("/wordcount/in.txt")).getPath();
        // System.out.println(filePath);


        // VADO A CREARE IL CONTESTO LOCALMENTE CON LOCAL[4]
        final SparkConf conf = new SparkConf().setMaster(master).setAppName("WordCount");
        final JavaSparkContext sc = new JavaSparkContext(conf);
        // RDD -> CONTIENE LE LINEE
        // DA MODIFICARE PER METTERE IL FILE DENTRO AL JAR
        final JavaRDD<String> lines = sc.textFile(filePath+"files/wordcount/in.txt");
        // PER OGNI LINEA DIVIDO IN WORDS => TRASFORMATION
        final JavaRDD<String> words = lines.flatMap(line -> Arrays.asList(line.split(" ")).iterator());
        // ONGI WORD VIENE CREATO PAIR (S, 1) => QUI VADO A DEFINIRE IL LAVORO DEI MAP
        final JavaPairRDD<String, Integer> pairs = words.mapToPair(s -> new Tuple2<>(s, 1));
        // DEFINISCO LA FUNZIONE DI RIDUZIONE FINALE -> QUESTA FA L'ACTION CHE RITORNA IL VALORE
        // RITORNA AL DRIVER PROGRAMM CHE è QUESTO FILE IN CUI IO SONO ORA
        final JavaPairRDD<String, Integer> counts = pairs.reduceByKey((a, b) -> a + b);
        // quando lo chiamo poi ASPARK DIVIDE IL LAVORO INTERNAMENTE E POI MI RITORNA IL VALORE QUI
        // DIVIDE IN BASE AI 4 CORE
        // DIMEZZO C'è IL SHIFFLE
        System.out.println(counts.collect());

        sc.close();
    }

}

/*
CREAZIONE DELL'AMBIENTE PER LA DISTRIBUZIONE DEI COMPITI
1) ADDRESS DELL'ENVARIAMENT -> export SPARK_MASTER_HOST=127.0.0.1
2) enable event log => così posso vedere cosa sta succedendo a livello visivo
directory per specificare il log e verficare che la directory esiste

./sbin/start-master.sh  => FACCIO PARTIRE IL MASTER
./sbin/start-worker.sh spark://127.0.0.1:7077  => FACCIO PASSARE ADDRESS DEL MASTER
./sbin/start-history-server.sh  => FACCIO PARTIRE INTERFACCIA GRAFICA PER VEDERE COSA STA SUCCEDENDO
IN localhost:8080

FAR PARTIRE UN WOKRER
3) package del maven
4) nella cartella di spark => ./bin/spark-submit --class it.polimi.middleware.spark.batch.wordcount.WordCount ~/Users/michelebersani/Desktop/spark_tutorial-1.0.jar spark://127.0.0.1:7077 ~/Desktop/

// RIFARE TUTTI I PASSAGGI DAL PRIMO ALL'ULTIMO OGNI VOLTA

// ONE JOB PER OGNI ACTION
// un stage => sequenza di operazioni senza shuffle
// tutto quello fatto finora viene fatto in un solo worker
*/

/*
// TODO DESCRIZIONE GENERALE DI SPARK
1) VIENE CREATO IL DRVIER CHE CREA IL CONTEXT, CHE è QUESTO FILE  STESSO
2) VIENE ESEGUITO ALL'INTERNO DEL WORKER CHE SI CONNETE AL MASTER PER LA GESTIONE DEI JOB
3) OGNI WORKER FA ESECUZIOEN ESECUTOR DEI TASK CHE DERIVANO DALLA DIVISIOEN DEI JOB => MA IMPORTANTE OGNI WORKER HA UNA CACHE
RICORDARSI CHE UN RDD è TIPO UNA LISTA DI DATI CHE VIENE DIVISA TRA I WROKER CHE  FANNO LE ESECUZIONI  E POI FANNO CACHE => IL CACHE DEVE ESSERE FATTO PRIMA DELL'AZIONE
COSì I DATI CON CUI HO FATTO POI UNA REDUCE LI HO SALVATI PER  UNA ESECUZIOEN DOPO

FACCIAMO I CACHE PERCHè OGNI TRASFORMAZIONE DI DIMENTICA DI QUELLA PRECEDENTE O VIENE PERSA => SE POI DOBBIAMO FARE DELLE OPERAZIONI SU DI ESSO
MEGLIO FARE IL CACHE TIPO NELL'ITERAZIONE => PARTE DAL REDUCE E POI VEDE COSA HA BISOGNO MA SE LO HA GIà PARTE DAL CACHE COSì NON DEVE
RIFARE TUTTA LA COMPUTAZIONE

4) LE OPERAZIONI:
-> TRASFORMAZIONI: MAP, FILTER SONO OPERAZIONI LAZY CIOè NON VENGONO ESEGIUITE SUBUTO MA SOLO QUANDO VIENE IDENTIFICATA UN'AZIONE PERCHè COSì
IL WORKER LEGGE DALLA ACRTION E TORNA INDIETRO COSì DA RIDURRE LE OPERAZIONI CHE NON SERBONO => IL WORKER O MASTER SI CREA UN ALBERO (GAP) DELLE OPERAZIONI
DIVIDENDOLE IN STAGE E RIDUZIONI

-> ACTION: SUM,... SONO QUELLE AZIONI CHE FANNO ESEGUIRE LE TRASFORMAZIONI => IDEA è CHE IL WORKER (CHE HA I DATI NEL SUO DISCO) QUANDO RICEVE
IL SOFWTARE DA FARE DA PARTE DEL MASTER LEGGE IL SOFTWRAE => CREA GAP => VEDE LE ACTION => FA LE TRASFORMAZIONI E POI LE ACTION E POI RITORNA AL MASTER
IL RISULTATO COSì PUò UNIRLI CON TUTTI GLI ALTRI RISULTATI DEI VARI WORKER

5) QUI ABBIAMO CHE IL CODICE CHE SI SPOSTA INVECE I DATI STANNO DENTRO AL WOERKER, INFATTI:
    -> QUANDO ATTIVAIMO IL WORKER MANDIAMO:
        A) INDIRIZZO DEL MASTER PER RICEVERE IL CODICE => SAPENDO QUALI DATI SONO IN QUEL WORKER
        B) LA RISORCA DEI DATI DA CUI ATTINGERE

    -> CI SONO ALCUNE TRASFORMAIONI  COME GROUP BY CHE RICHIDE DI FARE LO SHUFFLE DEI DATI TRA I WORKER (GESTITO DAL MASTER) PER FARE L'ESECUZIONE

6) RDD VECCHI => DATAFRAME CHE SONO RDD MA STRUTTURATI IN TABELLE CON COLONNE MA LE RIGHE SONO INFINITE
-> MOLTO PIù VELOCI DA ANALIZZARE E GESTIRE E DISTRIBUIBILI TRA I VARI WORKER
 */