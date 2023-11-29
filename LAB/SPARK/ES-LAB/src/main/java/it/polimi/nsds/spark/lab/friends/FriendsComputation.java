package it.polimi.nsds.spark.lab.friends;

import org.apache.spark.internal.config.R;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.mean;

/**
 * Implement an iterative algorithm that implements the transitive closure of friends
 * (people that are friends of friends of ... of my friends).
 *
 * Set the value of the flag useCache to see the effects of caching.
 */
public class FriendsComputation {
    private static final boolean useCache = true;

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? "FriendsCache" : "FriendsNoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(appName)
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> fields = new ArrayList<>();
        fields.add(DataTypes.createStructField("person", DataTypes.StringType, false));
        fields.add(DataTypes.createStructField("friend", DataTypes.StringType, false));
        final StructType schema = DataTypes.createStructType(fields);

        final Dataset<Row> input = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(schema)
                .csv(filePath + "files/friends/friends.csv");

        /*
        // TODO VEDERE ESERVICIO SUL VOLO
        // UNION FA TIPO INSERT METTE INSIEME QUELLI CHE HANNO LO STESSO SCHEMA

        Dataset<Row> inputBase = input;
        int num = 0 ;

        while (inputBase.count() != num ){
            num = (int) inputBase.count();

            final Dataset<Row> livello = inputBase.as("base")
                    .join(inputBase.as("nuovo"), col("base.friend").equalTo(col("nuovo.person")))
                    .select(col("base.person"), col("nuovo.friend"))
                    .union(inputBase);

            inputBase = livello;
            inputBase.cache();
            System.out.println(num);
            // VA USATO IL CACHE PER AVERE IL PUNTATORE AL VALORE
        }

        inputBase.show();
        */


        var i2 = input.as("i2");
        var i1 = input.as("i1");

        var newFriends = i1.join(i2, col("i1.friend").equalTo(col("i2.person")), "inner")
                        .select(col("i1.person").as("person"), col("i2.friend").as("friend"));

        var lastIteration = input;
        var margedFriends =  input.union(newFriends).distinct();

        while (lastIteration.count() != margedFriends.count()){
            lastIteration = margedFriends;

            i2 = margedFriends.as("i2");
            i1 = margedFriends.as("i1");

            newFriends = i1.join(i2, col("i1.friend").equalTo(col("i2.person")), "inner")
                    .select(col("i1.person").as("person"), col("i2.friend").as("friend"));

            margedFriends =  input.union(newFriends).distinct();
            // SICCOME è UNA COSA ITERATIVA ALLORA USO LA CACHE PER EVITARE DI RICALCOLARE TUTTO
            margedFriends.cache();

        }

        margedFriends.show();

        spark.close();
    }
}

/*
LEGENDA:

.filter == .where

nomeTabella.as("ciao").select(col("ciao.nome"))

*/

/*
In Spark, le trasformazioni sui DataFrame sono di natura "lazy", il che significa che l'esecuzione delle operazioni non avviene immediatamente quando la trasformazione viene chiamata. Invece, le trasformazioni vengono pianificate e vengono eseguite solo quando viene chiamata un'azione (ad esempio, una chiamata a count(), show(), ecc.).

Tuttavia, Spark ottimizza le trasformazioni in modo da evitare di ricalcolare l'intero DataFrame da zero ogni volta che viene utilizzato. Quando un DataFrame viene trasformato, Spark cerca di sfruttare la pianificazione laziale e le dipendenze tra le trasformazioni per eseguire solo le parti necessarie dei calcoli.

La ragione principale per cui in alcuni casi potrebbe sembrare che Spark stia "ricalcolando da zero" è che a volte il piano di esecuzione viene modificato, ad esempio, a causa di un cambiamento nelle trasformazioni o nelle azioni richieste. In questi casi, Spark può dover ricalcolare o ottimizzare nuovamente le operazioni da eseguire.

L'uso di cache() può essere utile in situazioni in cui il DataFrame viene riutilizzato più volte in seguito a operazioni costose, garantendo che i risultati intermedi siano mantenuti in memoria per evitare di doverli ricalcolare ad ogni azione successiva.

Tuttavia, l'uso di cache() deve essere ponderato, in quanto può comportare un utilizzo significativo di memoria, e in alcune situazioni potrebbe non essere necessario o addirittura controproducente. È consigliabile utilizzare cache() solo quando si è sicuri che il beneficio in termini di velocità di accesso superi il costo di memorizzazione in memoria.

 ITERAZIONE VIENE VISTA COME CAMBIO DI TRASFORNAZIONE
 */
