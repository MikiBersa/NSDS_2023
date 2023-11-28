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
public class InvestmentSimulatorUnpersist {
    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[1]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final double threshold = 35;

        final SparkConf conf = new SparkConf().setMaster(master).setAppName("InvestmentSimulator");
        final JavaSparkContext sc = new JavaSparkContext(conf);
        sc.setLogLevel("ERROR");

        final JavaRDD<String> textFile = sc.textFile(filePath + "files/iterative/investment.txt");

        int iteration = 0;

        JavaRDD<Tuple2<Double, Double>> investements = textFile.map(w ->{
            String[] values = w.split(" ");
            double amountOwned = Double.parseDouble(values[0]);
            double investmentRate = Double.parseDouble(values[1]);
            return new Tuple2<>(amountOwned, investmentRate);
        });

        // CACHE -> WORKERS PROVIDE CACHING RESOURCES main o disk
        // POSSO CUSTOMIZZARE => FUNZIONA FINO A QUANDO C'è POSTO E POI TOGLIE
        // QUINDI MEGLIO NON SALVARE TUTTI GLI RDD MA SOLO QUELLO DI CUI HO BISOGNO
        JavaRDD<Tuple2<Double, Double>> oldInvestments = investements;
        investements.cache();

        double sum = sumAmount(investements);

        while (sum < threshold) {
            iteration++;
            System.out.println("Iteraction: "+iteration);
            investements = investements.map(i -> {

                System.out.println("AAA");
                return new Tuple2<>(i._1 * (1 + i._2), i._2);
            });
            investements.cache();
            sum = sumAmount(investements);

            // DA NOTARE CHE DEVE ESSERE FATTO DOPO LA SUM -> SE NO NON CALCOLA IL NUOVO INVESTIMENTO
            // DA SOSTITUIRE CON QUELLO VECCHIO
            // DICIAMO CHE LA VERSIONE PRECEDENTE SI PUò TOGLIERE DALLA CACHE
            oldInvestments.unpersist();
            oldInvestments = investements;
        }

        System.out.println("Sum: " + sum + " after " + iteration + " iterations");
        sc.close();
    }


    private static double sumAmount(JavaRDD<Tuple2<Double, Double>> investments){
        return investments.
                mapToDouble(a -> a._1)
                .sum();
    }


}

