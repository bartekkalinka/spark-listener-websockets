package pl.bka.websockets

import akka.actor._
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives
import akka.stream.{SourceShape, Materializer}
import akka.stream.scaladsl._

case class Broadcast(text: String)
case class Check(counter: Long)

class Webservice()(implicit fm: Materializer, system: ActorSystem) extends Directives {

  def route =
    get {
      pathSingleSlash {
        getFromResource("webapp/index.html")
      } ~
        path("logs_broadcast") {
          parameter('name) { name =>
            handleWebsocketMessages(websocketFlow)
          }
        }
    } ~
      getFromResourceDirectory("webapp")

  def websocketFlow: Flow[Message, Message, Unit] = {
    val tick = Source(0 seconds, 1 seconds, ())
    val counter = Source(Stream.iterate(0L)(_ + 1))
    val source = Source() { implicit builder: FlowGraph.Builder[Unit] =>
      import FlowGraph.Implicits._
      val zip = builder.add(ZipWith[Unit, Long, Check]((a: Unit, i: Long) => Check(i)))
      val checkMap = builder.add(Flow[Check].map(readMessage))
      tick ~> zip.in0
      counter ~> zip.in1
      zip.out ~> checkMap.inlet
      checkMap.outlet
    }

    Flow.wrap(Sink.ignore, source)(Keep.none)
  }

  def readMessage(tick: Check): Message = {
    val sparkMsg = FileCommunication.getMessage.map(FileCommunication.formatHtml).getOrElse("")
    val msg = s"counter ${tick.counter} <br>$sparkMsg"
    TextMessage.Strict(msg)
  }
}
