package kafka.writer

import org.apache.kafka.clients.producer.{Callback, ProducerRecord}

import scala.reflect.ClassTag

abstract class KafkaWriter[T: ClassTag] extends Serializable {
  /**
    * Write a DStream, RDD, or Dataset to Kafka
    * @param producerConfig producer configuration for creating KafkaProducer
    * @param transformFunc a function used to transform values of T type into [[ProducerRecord]]s
    * @param callback an optional [[Callback]] to be called after each write, default value is None.
    */
  def writeToKafka[K, V](
                          producerConfig: Map[String, Object],
                          transformFunc: T => ProducerRecord[K, V],
                          callback: Option[Callback] = None
                        ): Unit
}