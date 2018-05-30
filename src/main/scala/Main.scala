import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Main {
  def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    val spark = SparkSession.builder()
      .appName("ogn-consumer")
      .master("local[*]")
      .getOrCreate()

    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Seconds(5))

    val receiver = new OGNSparkReceiver("aprs.glidernet.org", 10152)
    val stream = ssc.receiverStream(receiver)

    stream.foreachRDD(x => {
      x.foreach(println)
      x.foreach(Parser.parse)
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
