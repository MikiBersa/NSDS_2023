package it.polimi.middleware.spark.batch.bank;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.max;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javassist.compiler.ast.Pair;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.internal.config.R;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

/**
 * Bank example
 *
 * Input: csv files with list of deposits and withdrawals, having the following
 * schema ("person: String, account: String, amount: Int)
 *
 * Queries
 * Q1. Print the total amount of withdrawals for each person.
 * Q2. Print the person with the maximum total amount of withdrawals
 * Q3. Print all the accounts with a negative balance
 */
public class Bank {
    private static final boolean useCache = true;

    // QUI USIAMO SPARK SQL
    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? "BankWithCache" : "BankNoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(appName)
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> mySchemaFields = new ArrayList<>();

        // VADO A CARICARE LO SCHEMA DELLA TABELLA
        mySchemaFields.add(DataTypes.createStructField("person", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("account", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("amount", DataTypes.IntegerType, true));
        final StructType mySchema = DataTypes.createStructType(mySchemaFields);

        final Dataset<Row> deposits = spark
                .read()
                // se non un header ok, ma se l'ho carico direttamente lo schema
                .option("header", "false")
                .option("delimiter", ",")
                .schema(mySchema)
                // faccio la lettura del file con lo schema descritto sopra
                .csv(filePath + "files/bank/deposits.csv");

        final Dataset<Row> withdrawals = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(mySchema)
                .csv(filePath + "files/bank/withdrawals.csv");

        if(useCache){
            withdrawals.cache();
        }

        // Q1. Total amount of withdrawals for each person

        // MI RITORNA UNA TABELLA DI RIGHE PER OGNI PERSONA
        // FACCIO LA QUERY CON LA STRUTTURA DI SPARK
        final Dataset<Row> sumWithdrawals = withdrawals
                .groupBy("person")
                .sum("amount")
                .select("person", "sum(amount)");

        if(useCache){
            withdrawals.cache();
        }

        sumWithdrawals.show();

        // Q2. Person with the maximum total amount of withdrawals

        /*
        final Dataset<Row> max = sumWithdrawals
                .agg(max("sum(amount)"))
                .withColumnRenamed("max(sum(amount))", "sum(amount)")
                .join(sumWithdrawals, "sum(amount)")
                .select("person");*/

        final long maxTotal = sumWithdrawals
                .agg(max("sum(amount)"))
                .first()
                .getLong(0);

        final Dataset<Row> maxWithdrawals = sumWithdrawals
                .filter(sumWithdrawals.col("sum(amount)").equalTo(maxTotal));


        if(useCache){
            maxWithdrawals.cache();
        }

        maxWithdrawals.show();

        /*
        // Q3 Accounts with negative balance

        final Dataset<Row> withdrawalsMod = withdrawals
                .withColumnRenamed("account", "account_w");
        // DEVO FARE LA SOTTRAZIONE
        final Dataset<Row> aggregazione = deposits
                .withColumnRenamed("amount", "base")
                .join(withdrawalsMod, deposits.col("account").equalTo(withdrawalsMod.col("account_w")))
                .withColumn("amount_difference", col("base").minus(col("amount")))
                .filter(col("amount_difference").lt(0))
                .select("account");
        */

        // SOLUZIONE DEL PROF
        final Dataset<Row> totWithdrawals = withdrawals
                .groupBy("account")
                .sum("amount")
                .drop("person")
                .as("totalWithdrawals");

        final Dataset<Row> totDeposits = deposits
                .groupBy("account")
                .sum("amount")
                .drop("person")
                .as("totalDeposits");

        final Dataset<Row> negativeAccounts = totWithdrawals
                .join(totDeposits, totDeposits.col("account").equalTo(totWithdrawals.col("account")), "left_outer")
                .filter(totDeposits.col("sum(amount)").isNull().and(totWithdrawals.col("sum(amount)").gt(0)).or
                        (totWithdrawals.col("sum(amount)").gt(totDeposits.col("sum(amount)")))
                ).select(totWithdrawals.col("account"));

        negativeAccounts.show();

        // Q4 Accounts in descending order of balance

        // TODO

        spark.close();

    }
}