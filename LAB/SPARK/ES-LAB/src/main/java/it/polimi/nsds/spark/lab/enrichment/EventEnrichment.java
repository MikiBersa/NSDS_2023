package it.polimi.nsds.spark.lab.enrichment;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.streaming.Durations;

import javax.naming.ldap.PagedResultsControl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.window;

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
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> productClassificationFields = new ArrayList<>();
        productClassificationFields.add(DataTypes.createStructField("product", DataTypes.StringType, false));
        productClassificationFields.add(DataTypes.createStructField("classification", DataTypes.StringType, false));
        final StructType productClassificationSchema = DataTypes.createStructType(productClassificationFields);

        final Dataset<Row> inStream = spark
                .readStream()
                // QUI VUOLE SOCKET O RATE? -> rate
                .format("rate")
                // AGGIORNAMENTO OGNI SECONDO
                .option("rowsPerSecond", 1)
                .option("host", socketHost)
                .option("port", socketPort)
                .load();

        final Dataset<Row> productsClassification = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(productClassificationSchema)
                .csv(filePath + "files/enrichment/product_classification.csv");

        productsClassification.show();

        // I DATI CHE RICEVO LI TRASFORMO IN UNA TABELLA CON UNA COLONNA RINOMINATA product
        // NEL CASO IN CUI HO
        Dataset<Row> inStreamDF = inStream.toDF( "timestamp","product");

        // MI DAVA ERRORE SUL TIMESTAMP PERCHè HA VISTO CHE DOPO LO CHIEDEVA E QUINDI HO BISOGNO DEL TIMESTAMP
        // DA NOTARE CHE SE USO RATE -> HO COLONNE TIMESTAMP E VALUE
        // SE USO SOLO SOCKET VIENE MESSO SOLO VALUE
        final StreamingQuery query = inStreamDF
                .join(productsClassification, inStreamDF.col("product").equalTo(productsClassification.col("product")), "left_outer")
                .groupBy(
                        window(col("timestamp"), "30 seconds", "5 seconds"),
                        col("classification")
                )
                .count()
                .filter(col("classification").isNotNull())
                .writeStream()
                .outputMode("update")
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