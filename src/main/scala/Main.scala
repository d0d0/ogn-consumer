import java.util.UUID

import kafka.writer._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

object Main {

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    val spark = SparkSession.builder()
      .appName("ogn-consumer")
      .master("local[*]")
      .getOrCreate()

    val topic = "my-topic"
    val producerConfig = Map(
      "bootstrap.servers" -> "127.0.0.1:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "key.serializer" -> classOf[StringSerializer],
      "value.serializer" -> classOf[StringSerializer],
      "group.id" -> UUID.randomUUID().toString,
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Milliseconds(500))

    val receiver = new OGNSparkReceiver("aprs.glidernet.org", 10152)

    val stream = ssc
      .receiverStream(receiver)

    stream
      .filter(Parser.isAPRSStatus)
      .writeToKafka(producerConfig = producerConfig, s => new ProducerRecord[String, String]("aprs_status", s))

    stream
      .filter(Parser.isAircraft)
      .writeToKafka(producerConfig = producerConfig, s => new ProducerRecord[String, String]("aircraft", s))

    stream
      .filter(Parser.isBeacon)
      .writeToKafka(producerConfig = producerConfig, s => new ProducerRecord[String, String]("beacon", s))

    ssc.start()
    ssc.awaitTermination()
  }
}
