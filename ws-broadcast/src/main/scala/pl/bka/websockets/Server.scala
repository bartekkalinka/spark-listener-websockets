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
      val b = Broadcast(getMessage.map(counter + ": " + _).getOrElse(counter.toString))
      subscribers.foreach(_ ! b)
      counter += 1
    case ParticipantLeft(person) => log.debug(s"$person left!")
    case Terminated(sub)         => subscribers -= sub // clean up dead subscribers
  }

  private def getMessage: Option[String] = {
    val path = "../spark-listener/test"
    val file = new File(path)
    if(file.exists) Some(scala.io.Source.fromFile(path).mkString) else None
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

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
