import korolev.*
import korolev.server.*
import korolev.state.javaSerialization.*
import korolev.effect.*

import levsha.dsl.*
import levsha.dsl.html.*

import scala.concurrent.Future

case class State(
    title: String = "Hello Scala",
    items: Vector[String] = Vector.empty
)

object HolyScala extends KorolevApp[Future, Array[Byte], State, Any] {

  import context.*

  private val itemsQueue = Queue[Future, String]()
  private val itemsHub = Hub(itemsQueue.stream)

  private val newItemInput = elementId(Some("newItemInput"))

  def onSubmit(access: Access): Future[Unit] =
    for {
      newItem <- access.valueOf(newItemInput)
      _ <- access.transition(state =>
        state.copy(items = state.items :+ newItem)
      )
    } yield ()

  def render(state: State): Node = optimize {
    Html(
      body(
        div(clazz := "title", state.title),
        ul(clazz := "list", state.items.map(item => li(clazz := "item", item))),
        form(
          input(newItemInput),
          button("Add"),
          event("submit")(onSubmit)
        )
      )
    )
  }

  private val itemsQueueExtension = Extension[Future, State, Any] { access =>
    for {
      stream <- itemsHub.newStream()
      _ <- stream.foreach { item =>
        access.transition(identity)
      }
    } yield Extension.Handlers()
  }

  val config = Future.successful {
    KorolevServiceConfig(
      stateLoader = StateLoader.default(State()),
      document = render,
      extensions = List(itemsQueueExtension)
    )
  }
}
