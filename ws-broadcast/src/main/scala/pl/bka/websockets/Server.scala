package pl.bka.websockets

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import scala.util.{ Success, Failure }
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.File

object FileCommunication {
  val filePath = "../spark-listener/test"

  def formatHtml(text: String): String = text.split('\n').mkString("<br>")

  def getMessage: Option[String] = {
    val file = new File(filePath)
    if(file.exists) {
      val testTxtSource = scala.io.Source.fromFile(filePath)
      val str = testTxtSource.mkString
      testTxtSource.close
      Some(str)
    } else None
  }

  def rmFile = {
    val file = new File(filePath)
    if (file.exists) file.delete()
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    FileCommunication.rmFile

    val interface = "localhost"
    val port = 8008
    val service = new Webservice()
    val binding = Http().bindAndHandle(service.route, interface, port)

    binding.onComplete {
      case Success(binding) ⇒
        val localAddress = binding.localAddress
        println(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
        system.awaitTermination()
      case Failure(e) ⇒
        println(s"Binding failed with ${e.getMessage}")
        system.shutdown()
    }
  }
}
