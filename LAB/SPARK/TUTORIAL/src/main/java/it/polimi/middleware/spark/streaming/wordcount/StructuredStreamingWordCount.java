package it.polimi.middleware.spark.streaming.wordcount;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;

public class StructuredStreamingWordCount {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String socketHost = args.length > 1 ? args[1] : "localhost";
        final int socketPort = args.length > 2 ? Integer.parseInt(args[2]) : 9999;

        // UTILIZZIAMO IL TUTTO COME UNA TABELLA -> CEH SI AGGIORNA
        // CONTIAMO / GESTIAMO COME SE FOSSE UNA TABELLA CON INFINITE RIGHE
        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("StructuredStreamingWordCount")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        // Create DataFrame representing the stream of input lines from connection to localhost:9999
        final Dataset<Row> lines = spark
                .readStream()
                .format("socket")
                .option("host", socketHost)
                .option("port", socketPort)
                .load();

        // Split the lines into words
        // If your input dataset is not relational (as in this case, since we do not know the number of words/columns)
        // You can still translate a Dataframe (Dataset<Row>) into a normal Dataset (in this case Dataset<String>)
        final Dataset<String> words = lines
                .as(Encoders.STRING())
                .flatMap((FlatMapFunction<String, String>) x -> Arrays.asList(x.split(" ")).iterator(), Encoders.STRING());

        // TODO QUI TRSFORMO DA UN DATA SET DI RIGHE -> STRINGHE (PER LEGGERE ANCHE  VALORI TIPO A A CON IN MESSO LO SPAZIO DA  DIVIDERE)
        // TODO E POI IN SET DI  RIGHE PER GESTIRE LA TABELLA => RAPPRESENTAZIONE TABELLARE
        // TODO BISOGNO COMUNQUE DI FARE UN'AGGREGAZIONE

        // Generate running word count
        // IN QUESTO CASO OGNI RIGA HA SOLO UNA COLONNA  CHE è LA PAROLA
        // RAGGRUPPO NELLA TABELLA INFINITA LE PAROLA CHE HANNO LO STESSA PAROLA E CI FACCIO IL CONTEGGIO
        // IN USCITA MI FA VEDERE SOLO IL VALORE CHE è STATO MODIFICATO
        // IN QUESTO CASO SICCOME è UN COUNT ALLORA CONSIDERO SOLO IL VALORE NUOVO UPDATE
        final Dataset<Row> wordCounts = words.groupBy("value").count();


        // Start running the query that prints the running counts to the console
        // There are three types of output
        // 1. Complete: outputs the entire result table
        // 2. Append: outputs only the new rows appended to the result table.
        // It is applicable only when existing rows are not expected to change (so, not in this case).
        // 3. Update: outputs only the rows that were updated since the last trigger.
        // QUESTO SARà IL MIO OUTPUT
        final StreamingQuery query = wordCounts
                .writeStream()
                // IN QUESTO CASO VIENE FATTO UN COUNT UPDATE -> SI AGGIORNA NEL TEMPO
                .outputMode("complete")
                .format("console")
                .start();

        try {
            query.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }

}