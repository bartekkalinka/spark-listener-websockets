/* code patterns copied from https://github.com/jrudolph/akka-http-scala-js-websocket-chat*/
package pl.bka.websockets

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

case class NewParticipant(name: String, subscriber: ActorRef)
case class ParticipantLeft(name: String)

case class WsFlow(wsActor: ActorRef) {
  def wsFlow(sender: String): Flow[UserInput, Broadcast, Unit] = {
    val in = Sink.actorRef[UserInput](wsActor, ParticipantLeft(sender))
    val out =
      Source.actorRef[Broadcast](1, OverflowStrategy.fail)
        .mapMaterializedValue(wsActor ! NewParticipant(sender, _))
    Flow.wrap(in, out)(Keep.none)
  }
}

