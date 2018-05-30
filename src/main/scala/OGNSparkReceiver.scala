import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import java.net.{InetAddress, Socket}
import java.util.UUID

import org.apache.spark.internal.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

class OGNSparkReceiver(host: String, port: Int)
  extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) with Logging {
  var socket: Socket = _


  def onStart() {
    // Start the thread that receives data over a connection
    new Thread("Socket Receiver") {
      override def run() {
        receive()
      }
    }.start()
  }

  def onStop() {
    // There is nothing much to do as the thread calling receive()
    // is designed to stop by itself if isStopped() returns false
  }

  /** Create a socket connection and receive data until receiver is stopped */
  private def receive() {
    var userInput: String = null
    try {
      // Connect to host:port
      val srvAddress = InetAddress.getByName(host)
      socket = new Socket(srvAddress, port)

      val writer = new PrintWriter(socket.getOutputStream, true)
      writer.println("user " + generateClientId + " pass -1 vers spark-client 0.0.1")

      // Until stopped or connection broken continue reading
      val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))
      userInput = reader.readLine()
      while (!isStopped && userInput != null) {
        store(userInput)
        userInput = reader.readLine()
      }
      reader.close()
      socket.close()

      // Restart in an attempt to connect again when server is active again
      restart("Trying to connect again")
    } catch {
      case e: java.net.ConnectException =>
        // restart if could not connect to server
        restart("Error connecting to " + host + ":" + port, e)
      case t: Throwable =>
        // restart if there is any other error
        restart("Error receiving data", t)
    }
  }

  def generateClientId: String = {
    val suffix = UUID.randomUUID.toString.split("-")(3).toUpperCase
    val res = InetAddress.getLocalHost.getHostName.substring(0, 3).toUpperCase
    val bld = new StringBuilder(res.replace("-", ""))
    bld.append("-")
    bld.append(suffix)
    bld.toString
  }
}