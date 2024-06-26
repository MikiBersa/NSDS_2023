package it.polimi.middleware.spark.streaming.event_time;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;

import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.window;

public class WindowedCount {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";

        // STREAMING OGNI DATO CHE ARRIVA RAPPRESENTA UN EVENTO -> CHE HA UN TIMESTAMP TIME CREATO DAL PUNTO DI VISTA CHI LO HA CREATO
        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("WindowedCount")
                .getOrCreate();

        // Create DataFrame from a rate source.
        // A rate source generates records with a timestamp and a value at a fixed rate.
        // It is used for testing and benchmarking.
        final Dataset<Row> inputRecords = spark
                .readStream()
                /*
                La fonte "rate" è comunemente utilizzata per scopi di sviluppo e test.
                Permette di generare dati sintetici in modo controllato impostando
                un tasso di generazione specifico (rowsPerSecond).

                che è diverso dal socket dove invece lì è utente che scrive

                // RICORDO CHE IL RATE CREA UNA SUCCESSIONE DI DUE COLONNE => TIMESTAMP, VALUE
                 */
                .format("rate")
                .option("rowsPerSecond", 10)
                .load();
        spark.sparkContext().setLogLevel("ERROR");

        // IN QUESTO CASO TENGO I DATI FINO A 1H PER QUELLI CHE ARRIVANO IN RITARDO
        // SE ARRIVA UN DATO IN RITARDO MENO DI 1H ALLORA TUTTI I WINDOW IN CUI APPARE QUEL DATO
        // VERRANNO AGGIORNATI -> CON IL NUOVO VALORE
        inputRecords.withWatermark("timestamp", "1 hour");

        // Q1: group by window (size 30 seconds, slide 10 seconds)

        final StreamingQuery query = inputRecords
                .groupBy(
                        window(col("timestamp"), "30 seconds", "10 seconds")
                )
                // SOMMA TUTTI I VALORI CHE HANNO STESSO VALUE E CHE SONO DENTRO I 30 SECONDI DELLA FINESTRA
                .count()
                // SCRIVI I VALORI
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        /*
        try {
            query.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }*/

        // Q2: group by window (size 30 seconds, slide 10 seconds) and value

        final StreamingQuery queryQ2 = inputRecords
                .groupBy(
                        window(col("timestamp"), "30 seconds", "10 seconds"),
                        col("value")
                )
                // SOMMA TUTTI I VALORI CHE HANNO STESSO VALUE E CHE SONO DENTRO I 30 SECONDI DELLA FINESTRA
                .count()
                // SCRIVI I VALORI
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        /*
        try {
            queryQ2.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }*/

        // Q3: group only by value

        final StreamingQuery queryQ3 = inputRecords
                .groupBy(
                        col("value")
                )
                // SOMMA TUTTI I VALORI CHE HANNO STESSO VALUE E CHE SONO DENTRO I 30 SECONDI DELLA FINESTRA
                .count()
                // SCRIVI I VALORI
                // in questo caso solo quelli aggiornati
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            // MI FA COMUQUE VEDERE LA TABELLA CON I TIMESTAMP |window|value|count|
            queryQ3.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }

}