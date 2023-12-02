package it.polimi.middleware.spark.streaming.enrichment;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.internal.config.R;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.lit;

/**
 * This code snippet exemplifies a typical scenario in event processing: merging
 * incoming events with some background knowledge.
 *
 * A static dataset (read from a file) classifies products (associates products to the
 * class they belong to).  A stream of products is received from a socket.
 *
 * We want to count the number of products of each class in the stream. To do so, we
 * need to integrate static knowledge (product classification) and streaming data
 * (occurrences of products in the stream).
 */

// IN QUESTO CASO POSSO UNIRE EVENTI DINAMICI E STATICI IN QUANTO IL TUTTO ARRIVA NELLO STESSO FRAMEWORK SPARK
public class EventEnrichment {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String socketHost = args.length > 1 ? args[1] : "localhost";
        final int socketPort = args.length > 2 ? Integer.parseInt(args[2]) : 9999;
        final String filePath = args.length > 3 ? args[3] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("EventEnrichment")
                .getOrCreate();

        // GESTIONE DELLA STRUTTURA DATO STATICO
        final List<StructField> productClassificationFields = new ArrayList<>();
        productClassificationFields.add(DataTypes.createStructField("product", DataTypes.StringType, false));
        productClassificationFields.add(DataTypes.createStructField("classification", DataTypes.StringType, false));
        final StructType productClassificationSchema = DataTypes.createStructType(productClassificationFields);

        // Create DataFrame representing the stream of input products from connection to localhost:9999
        final Dataset<Row> inStream = spark
                .readStream()
                .format("socket")
                // SE NON METTO IL RATE LO DECIDE LUI (SPARK) COME DEFAULT
                // ANCHE IN BASE AL CARICO DI LAVORO / INPUT
                .option("host", socketHost)
                .option("port", socketPort)
                .load();

        spark.sparkContext().setLogLevel("ERROR");

        // TODO QUI USO I DATI INPUT COME TABELLE NON COME IN STRAMINGWORDCOUNT CHE CONTO I DATI IN ENTRATA NON COME TABELLE
        // I DATI CHE RICEVO LI TRASFORMO IN UNA TABELLA CON UNA COLONNA RINOMINATA product
        Dataset<Row> inStreamDF = inStream.toDF("product");

        // IN QUESTO CASO è UN STREAM DA UN FILE
        final Dataset<Row> productsClassification = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(productClassificationSchema)
                .csv(filePath + "files/enrichment/product_classification.csv");


        productsClassification.show();

        // QUI L'IDEA è DI USARE LA STRUTTURA BASE DATA DALLA TABELLA DAL FILE
        // POI PER OGNI PRODOTTO IN ENTRATA ASSOCIARLO ALLA SUA CLASSE MEDIANTE IL JOIN
        // E POI FARE IL CONTEGGIO
        // QUI STO PREPARANDO LA QUERY CHE POI FACCIO ESEGUIRE ALLO StreamingQuery -> CHE QUESTO FA ATTIVARE IL JOB
        // CHE PUò PARTIRE IN RITARDO INFATTI VIENE STAMPATA LA TABELLA 104 PRIMA DEL BATCH
        final Dataset<Row> queryT   = inStreamDF
                .join(productsClassification, inStreamDF.col("product").equalTo(productsClassification.col("product")))
                .groupBy("classification")
                .count();

        final StreamingQuery query = queryT
                .writeStream()
                .outputMode("complete")
                .format("console")
                .start();

        productsClassification.show();

        try {
            query.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}

// LE SOLUZIONI STANNO NEL SOLUTIONS BRANCH