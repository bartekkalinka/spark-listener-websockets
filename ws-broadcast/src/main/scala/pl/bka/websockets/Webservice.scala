/* code patterns copied from https://github.com/jrudolph/akka-http-scala-js-websocket-chat*/
package pl.bka.websockets

import akka.actor._
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.stage._

import akka.http.scaladsl.server.Directives
import akka.stream.{OverflowStrategy, Materializer}
import akka.stream.scaladsl.{Keep, Source, Sink, Flow}

case class NewParticipant(name: String, subscriber: ActorRef)

class Webservice(wsActor: ActorRef)(implicit fm: Materializer, system: ActorSystem) extends Directives {

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

  def wsFlow(sender: String): Flow[Unit, Broadcast, Unit] = {
    val out =
      Source.actorRef[Broadcast](1, OverflowStrategy.fail)
        .mapMaterializedValue(wsActor ! NewParticipant(sender, _))
    Flow.wrap(Sink.ignore, out)(Keep.none)
  }

  def websocketFlow(sender: String): Flow[Message, Message, Unit] =
    Flow[Message]
      .collect { case _ => () } // ignore input
      .via(wsFlow(sender)) // ... and route them through the gameFlow ...
      .map{ case b: Broadcast => TextMessage.Strict(b.text) } // ... text from outgoing messages
}
