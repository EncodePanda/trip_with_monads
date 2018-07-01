package twm

import monix.eval.Task
import monix.execution.Scheduler
import scala.concurrent.Await
import scala.concurrent.duration._

object Monix {
  implicit class TaskOps[A](task: Task[A])(implicit scheduler: Scheduler) {
    def unsafeRunSync: A =
      Await.result(task.runAsync, Duration.Inf)
  }
}
