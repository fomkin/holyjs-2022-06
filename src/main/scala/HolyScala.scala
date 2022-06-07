import korolev.*
import korolev.server.*
import korolev.state.javaSerialization.*
import korolev.effect.*
import levsha.dsl.*
import levsha.dsl.html.*

import java.net.InetSocketAddress
import scala.concurrent.Future

case class Message(author: String, text: String)

case class State(
    title: String = "Hello Scala",
    messages: Vector[Message] = Vector.empty
)

object HolyScala
    extends KorolevApp[Future, Array[Byte], State, Any](
      address = new InetSocketAddress("0.0.0.0", 8080)
    ) {

  import context.*

  private val messagesQueue = Queue[Future, Message]()
  private val messagesHub = Hub(messagesQueue.stream)

  private val authorInput = elementId(Some("authorInput"))
  private val messageTextInput = elementId(Some("messageTextInput"))

  def onSubmit(access: Access): Future[Unit] =
    for {
      author <- access.valueOf(authorInput)
      text <- access.valueOf(messageTextInput)
      _ <- messagesQueue.offer(Message(author, text))
    } yield ()

  def render(state: State): Node = optimize {
    Html(
      body(
        div(
          state.messages.map(message =>
            div(s"${message.author}: ${message.text}")
          )
        ),
        form(
          input(authorInput),
          input(messageTextInput),
          button("Send"),
          event("submit")(onSubmit)
        )
      )
    )
  }

  private val itemsQueueExtension = Extension[Future, State, Any] { access =>
    for {
      stream <- messagesHub.newStream()
      _ <- stream.foreach { message =>
        access.transition(state =>
          state.copy(messages = state.messages :+ message)
        )
      }
    } yield Extension.Handlers()
  }

  val config = Future.successful {
    KorolevServiceConfig(
      stateLoader = StateLoader.default(State()),
      document = render,
      extensions = List(itemsQueueExtension),
      rootPath = "/holyjs-2022-06/" // REMOVE THIS IF YOU RUN APP ON LOCALHOST
    )
  }
}
