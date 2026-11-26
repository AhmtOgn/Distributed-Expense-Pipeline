// Spark task 1 of Project 3:
//   Kafka (4 department topics)  ->  Spark Structured Streaming  ->  Cassandra (test.expenses)
//
// Every record that arrives from Kafka is written to Cassandra right away.
// There is NO .trigger(Trigger.ProcessingTime("5 seconds")) here: with that trigger Spark
// waited 5 seconds between writes, which made new expenses appear in the web app late.
// Without a trigger, Spark starts the next micro-batch as soon as the previous one ends.
//
// Run it from spark-shell (see README.md, "Run the Kafka -> Spark -> Cassandra stream"):
//   scala> :paste spark-streaming/StreamHandler.scala
//   scala> StreamHandler.main(Array())

import org.apache.spark.sql._
import org.apache.spark.sql.cassandra._

// One POS record: <user_id, date_time, description, type, count, payment>
// ("type" is a Scala keyword, so the field is called ptype and renamed before writing.)
case class POSData(user_id: Int, date_time: String, description: String,
                   ptype: String, count: Int, payment: Double)

object StreamHandler {

  // Kafka topics, one per department. The pattern ignores upper/lower case,
  // so it matches both SALES and sales.
  val TopicPattern = "(?i)(accounting|research|sales|operations)"

  val CassandraKeyspace = "test"
  val CassandraTable    = "expenses"

  // Called once per micro-batch: append the batch to Cassandra.
  def writeToCassandra(batchDF: DataFrame, batchId: Long): Unit = {
    println(s"Writing batch $batchId to Cassandra")
    batchDF.write
      .cassandraFormat(CassandraTable, CassandraKeyspace)
      .mode("append")
      .save()
  }

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder
      .appName("Project-3")
      .config("spark.cassandra.connection.host", "127.0.0.1")
      .getOrCreate()

    import spark.implicits._

    // 1) Read from Kafka, all 4 topics.
    //    (Repeating .option("subscribe", ...) 4 times would keep only the last topic.)
    val inputDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribePattern", TopicPattern)
      .load()

    // 2) Kafka value (bytes) -> CSV line -> columns.
    //    Fields have a space after each comma ("7876, 2025-11-19T12:48:05, Chocolate, ..."),
    //    so every field is trimmed. Bad lines are skipped instead of stopping the stream.
    val expensesDF = inputDF
      .selectExpr("CAST(value AS STRING)").as[String]
      .map(_.split(",").map(_.trim))
      .filter(_.length == 6)
      .flatMap { r =>
        try Seq(POSData(r(0).toInt, r(1), r(2), r(3), r(4).toInt, r(5).toDouble))
        catch { case _: NumberFormatException => Seq.empty[POSData] }
      }
      .withColumnRenamed("ptype", "type")

    // 3) Write every record to Cassandra immediately (no aggregation, no trigger).
    val query = expensesDF.writeStream
      .outputMode("append")
      .foreachBatch(writeToCassandra _)
      .start()

    // until ^C
    query.awaitTermination()
  }
}
