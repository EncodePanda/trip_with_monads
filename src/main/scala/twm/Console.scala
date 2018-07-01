package twm

import scalaz._, Scalaz._
import monix.eval.Task

trait Console[F[_]] {
  def readLine: F[String]
  def printLn(line: String): F[Unit]
}

object ForTrans {
  def forTrans[F[_]: Monad, T[_[_], _]: MonadTrans](
      implicit C: Console[F]): Console[T[F, ?]] = new Console[T[F, ?]] {
    def readLine: T[F, String] = MonadTrans[T].liftM(Console[F].readLine)
    def printLn(str: String): T[F, Unit] =
      MonadTrans[T].liftM(Console[F].printLn(str))
  }
}

object Console extends Console0 {
  def apply[F[_]: Console]: Console[F] = implicitly[Console[F]]

  val monixConsole: Console[Task] = new Console[Task] {
    def readLine: Task[String]            = Task.delay(scala.io.StdIn.readLine)
    def printLn(line: String): Task[Unit] = Task.delay(println(line))
  }
}

trait Console0 {
  implicit def eitherTConsole[F[_]: Monad: Console, E]: Console[EitherT[F, E, ?]] =
    ForTrans.forTrans[F, EitherT[?[_], E, ?]]
}
/**
trait ConsoleIO1 extends ConsoleIO0 {
  import readerT._
  implicit def readerTConsoleIO[F[_]: Monad: ConsoleIO, E]: ConsoleIO[ReaderT[F, E, ?]] =
    ForTrans.forTrans[F, ReaderT[?[_], E, ?]]
}

trait ConsoleIO2 extends ConsoleIO1 {
  import stateT._
  implicit def stateTConsoleIO[F[_]: Monad: ConsoleIO, E]: ConsoleIO[StateT[F, E, ?]] =
    ForTrans.forTrans[F, StateT[?[_], E, ?]]
}
  */
