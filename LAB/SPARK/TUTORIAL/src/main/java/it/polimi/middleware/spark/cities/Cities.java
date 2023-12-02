package it.polimi.middleware.spark.cities;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

public class Cities {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> citiesRegionsFields = new ArrayList<>();
        citiesRegionsFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesRegionsFields.add(DataTypes.createStructField("region", DataTypes.StringType, false));
        final StructType citiesRegionsSchema = DataTypes.createStructType(citiesRegionsFields);

        final List<StructField> citiesPopulationFields = new ArrayList<>();
        citiesPopulationFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, false));
        citiesPopulationFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesPopulationFields.add(DataTypes.createStructField("population", DataTypes.IntegerType, false));
        final StructType citiesPopulationSchema = DataTypes.createStructType(citiesPopulationFields);

        final Dataset<Row> citiesPopulation = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesPopulationSchema)
                .csv(filePath + "files/cities/cities_population.csv");

        citiesPopulation.cache();

        final Dataset<Row> citiesRegions = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesRegionsSchema)
                .csv(filePath + "files/cities/cities_regions.csv");

        final Dataset<Row> joinedDataset = citiesRegions
                .join(citiesPopulation, citiesRegions.col("city").equalTo(citiesPopulation.col("city")))
                .select(citiesPopulation.col("id"),
                        citiesPopulation.col("city"),
                        citiesRegions.col("region"),
                        citiesPopulation.col("population"));

        // FACCIO CACHE DI QUELLO DI CUI HO BISOGNO
        joinedDataset.cache();

        final Dataset<Row> q1 = joinedDataset
                .groupBy("region")
                .sum("population")
                .sort(desc("sum(population)"));

        q1.show();

        final Dataset<Row> q2 = joinedDataset
                .groupBy("region")
                // COSì VADO A CALCOLARE IN PARALLELO DUE STATISTICHE UNA è IL CONTEGGIO DELLE CITTà IN QUELLA REGIONE E POI IL MAX DELLA POPOLAZIONE
                // IN QUELLA REGIONE
                .agg(count("city"), max("population"));

        q2.show();

        // TRASFORMO LA TABELLA IN RDD DA DATASET DI RUGHE A LISTA DI "TUPLE" CHE RAPPRESENTANO LE TIGHE COSì POSSO USARE
        // IL MAP => TRASFORMAZIONE
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));
        population.cache();
        long count  = population.reduce((a, b) -> a+b);

        int year = 0;
        while (count < 100 * 1000 * 1000) {
            year++;
            population = population.map(p -> p > 1000 ? p+(int)(p*0.01) : p-(int)(p*0.1));
            // TENGO IN MEMORIA IL RISULTATO DELLA MAP PER QUELLO DOPO
            population.cache();
            count = population.reduce((a, b) -> a+b);
            System.out.println("Year: " + year + ", count: " + count);
        }

        // Bookings: the value represents the city of the booking
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load();

        // gli mando il booking sulla città => valore è id della città
        final StreamingQuery q4 = bookings
                .join(
                        joinedDataset,
                        bookings.col("value").equalTo(joinedDataset.col("id")))
                // cancello le tabelle che non mi servono
                // perchè c'è id città region popolazione time value
                .drop("population", "value")
                .groupBy(
                        window(col("timestamp"), "30 seconds", "5 seconds"),
                        col("region")
                )
                .sum()
                // avrò come attributi della tabella quello che vado a ragdruppare => window, region e poi sommo
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}