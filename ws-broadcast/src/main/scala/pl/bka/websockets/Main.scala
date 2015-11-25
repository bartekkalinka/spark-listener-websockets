package pl.bka.websockets

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import scala.util.{ Success, Failure }
import scala.concurrent.ExecutionContext.Implicits.global

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
