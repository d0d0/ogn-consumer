import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("ogn-consumer")
      .master("local[*]")
      .getOrCreate()

    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Seconds(5))

    import spark.implicits._

    val stream = ssc.socketTextStream("aprs.glidernet.org", 14580)
    stream.print()

    ssc.start()
    ssc.awaitTermination()
  }
}
