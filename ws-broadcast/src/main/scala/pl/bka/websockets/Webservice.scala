package pl.bka.websockets

import akka.actor._
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Source, Sink, Flow}

case class Broadcast(text: String)
case class Check()

class Webservice()(implicit fm: Materializer, system: ActorSystem) extends Directives {

  def route =
    get {
      pathSingleSlash {
        getFromResource("webapp/index.html")
      } ~
        path("logs_broadcast") {
          parameter('name) { name =>
            handleWebsocketMessages(websocketFlow(sender = name))
          }
        }
    } ~
      getFromResourceDirectory("webapp")

  def wsFlow(sender: String): Flow[Message, Broadcast, Unit] = {
    val tick = Source(0 seconds, 1 seconds, Check())
    val out = tick.map(checkBroadcast)
    Flow.wrap(Sink.ignore, out)(Keep.none)
  }

  def websocketFlow(sender: String): Flow[Message, Message, Unit] =
    Flow[Message]
      .via(wsFlow(sender)) // ... and route them through the gameFlow ...
      .map{ case b: Broadcast => TextMessage.Strict(b.text) } // ... text from outgoing messages

  def checkBroadcast(tick: Check): Broadcast = {
    val msg = FileCommunication.getMessage.map(FileCommunication.formatHtml).getOrElse("")
    Broadcast(msg)
  }
}
