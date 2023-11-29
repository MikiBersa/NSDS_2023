package it.polimi.middleware.spark.batch.wordcount;

import org.apache.hadoop.shaded.org.checkerframework.checker.units.qual.C;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

public class WordCountModified {

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkConf conf = new SparkConf().setMaster(master).setAppName("WordCount");
        final JavaSparkContext sc = new JavaSparkContext(conf);
        sc.setLogLevel("ERROR");

        final JavaRDD<String> lines = sc.textFile(filePath + "files/wordcount/in.txt");

        // Q1. For each character, compute the number of words starting with that character

        final JavaRDD<String> words  = lines.flatMap(line -> Arrays.asList(line.split(" ")).iterator());
        final JavaRDD<Character> char_list = words.map(string -> string.charAt(0));
        // devo creare la coppia
        final JavaPairRDD<Character, Integer> pairs = char_list.mapToPair(s -> new Tuple2<>(s, 1));
        final JavaPairRDD<Character, Integer> counts = pairs.reduceByKey((a, b) -> a + b);

        System.out.println(counts.collect());

        // Q2. For each character, compute the number of lines starting with that character

        final JavaPairRDD<Character, Integer> init = lines.mapToPair(line -> new Tuple2<>(line.charAt(0), 1));
        final JavaPairRDD<Character, Integer> cont = init.reduceByKey((a,b) -> a + b);

        System.out.println(cont.collect());

        // Q3. Compute the average number of characters in each line
        // POTREI FARE UN FILTER E TOGLIERE GLI SPAZI
        final JavaRDD<String> init_char = lines.map(line -> line.replace(" ",""));
        final JavaRDD<Integer> _char = init_char.map(l -> l.length());
        Integer sum = _char.reduce((a,b) -> a +b);
        Double avg = (double) (sum / lines.count());
        System.out.println(avg);

        sc.close();
    }

}