package pl.bka.websockets

import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import scala.util.{ Success, Failure }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.File

case class Broadcast(text: String)
case class Check()
case class UserInput()

object WebSocketServer {
  def props() = Props(classOf[WebSocketServer])
}
class WebSocketServer extends Actor with ActorLogging {
  /* code patterns copied from https://github.com/jrudolph/akka-http-scala-js-websocket-chat*/
  var subscribers = Set.empty[ActorRef]

  var counter: Long = 1

  override def preStart(): Unit = { context.system.scheduler.schedule(1 seconds, 1 seconds, self, Check()) }

  def receive: Receive = {
    case NewParticipant(name, subscriber) =>
      context.watch(subscriber)
      subscribers += subscriber
      log.debug(s"$name joined!")
    case Check() =>
      val msg = FileCommunication.getMessage.map(counter + "<br>" + FileCommunication.formatHtml(_)).getOrElse(counter.toString)
      val b = Broadcast(msg)
      subscribers.foreach(_ ! b)
      counter += 1
    case Terminated(sub)         => subscribers -= sub // clean up dead subscribers
  }
}

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

    val server = system.actorOf(WebSocketServer.props(), "websocket")

    val interface = "localhost"
    val port = 8008
    val service = new Webservice(server)
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
