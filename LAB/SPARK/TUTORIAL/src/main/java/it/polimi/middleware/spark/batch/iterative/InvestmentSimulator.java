package it.polimi.middleware.spark.batch.iterative;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

/**
 * Start from a dataset of investments. Each element is a Tuple2(amount_owned, interest_rate).
 * At each iteration the new amount is (amount_owned * (1+interest_rate)).
 *
 * Implement an iterative algorithm that computes the new amount for each investment and stops
 * when the overall amount overcomes 1000.
 */
public class InvestmentSimulator {
    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[1]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final double threshold = 35;

        final SparkConf conf = new SparkConf().setMaster(master).setAppName("InvestmentSimulator");
        final JavaSparkContext sc = new JavaSparkContext(conf);
        sc.setLogLevel("ERROR");

        final JavaRDD<String> textFile = sc.textFile(filePath + "files/iterative/investment.txt");

        int iteration = 0;

        // ESTRAPOLO QUELLO CHE C'è DENTRO ALLA RIGA IN TUPLE
        // <3, 0.01>
        JavaRDD<Tuple2<Double, Double>> investements = textFile.map(w ->{
            // FACCIO COSì PERCHè SO COM'è LA STRUTTURA
           String[] values = w.split(" ");
           double amountOwned = Double.parseDouble(values[0]);
           double investmentRate = Double.parseDouble(values[1]);
           return new Tuple2<>(amountOwned, investmentRate);
        });

        // sum è la somma di tutti gli investimenti del file
        double sum = sumAmount(investements);

        // PER FARE UNA ITERAZIONE DEVO CREARLO IO
        // POI SPARK IDENTIFICA OGNI MAP / REDUCE PER INVIARLO AI VARI WORK -> TRASFORMAZIONI CREA NUOVI RDD => STAGE
        // FA UNA FIVISIONE DEI VALORI
        while (sum < threshold) {
            iteration++;
            // ricalcolo il valore del nuovo balance
            // <10, 0.01>
            // con i._2
            // QUESTA è UNA MAP CHE NON VIENE ESEGUITA ORA NON CREA UN JOB
            // siccome ci sono 5 righe => viene eseguito 5 volte per ogni iterazione
            investements = investements.map(i -> {
                // DA NOTARE CHE PER FARE QUESTA OPERAZIONE DEVE PRENDERE investements DELL'ULTIMA OPERAZIONE
                // MA AD OGNI JOBS CANCELLA RDD CHE HO / PARTIAL RESULT E QUINDI RI FA DA CAMPO AD OGNI ITERAZIONE
                // QUINDI PER SALVARE I SUM TRA JOBS BISOGNA USARE CACHE
                System.out.println("AAA");
                return new Tuple2<>(i._1 * (1 + i._2), i._2);
            });
            // QUINDI PER SALVARE I SUM TRA JOBS BISOGNA USARE CACHE
            // INFATTI ULTIMO JOB NE FA 5 PERCHè RICORDA COSA HA FATTO PRIMA

            /*
            TODO NB: LE FUNZIONI DI RIDUZIONE / ACTION VANNO A MODIFICARE LA MEMORIA RAM NEL SENSO CHE DOP UNA ACTION
            NON SONO SICURO CHE HO TUTTI I DATI DELLE TRASFORMAZIONI PRECEDENTI
            QUINDI PER ESSERNE SICURI DEVO FARE IL CACHE PRIMA DELL'ACTION COSì SO CHE STA NEL DISCO
            TODO  QUINDI  MEMORIZZO LE TABELLE CHE COSO CHE VADO A UTILIZZARE SUCCESSIVAMENTE
             */

            investements.cache();
            sum = sumAmount(investements);
        }

        System.out.println("Sum: " + sum + " after " + iteration + " iterations");
        sc.close();
    }


    private static double sumAmount(JavaRDD<Tuple2<Double, Double>> investments){
        // in investments ho tutte le tuple del map di prima riga 42 o 31
        return investments
                // estraggo solo il primo valore della tupla perchè ci devo fare la somma
                .mapToDouble(a -> a._1)
                .sum();
        // faccio una riduzione della somma -> STARTS JOB INTO SPARK
        // java program aspetta il valore per proseguire per ogni reduce
    }



}
