package twm

import scalaz.{MonadState => _, _}, Scalaz._
import mtl._, ApplicativeAsk._, MonadState._
import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService
import MonadOps._
import shims._
import Monix._

object SemiFinal {

  import domain._

  type ErrorHandler[F[_]] = ApplicativeError[F, Error]
  object ErrorHandler {
    def apply[F[_]: ErrorHandler]: ErrorHandler[F] =
      implicitly[ApplicativeError[F, Error]]
  }

  type ConfigAsk[F[_]] = ApplicativeAsk[F, Config]
  object ConfigAsk {
    def apply[F[_]: ConfigAsk]: ConfigAsk[F] = implicitly[ApplicativeAsk[F, Config]]
  }

  type Requests = Map[City, Forcast]
  object Requests {
    def empty: Requests = Map.empty[City, Forcast]
  }
  type RequestsState[F[_]] = MonadState[F, Requests]

  object RequestsState {
    def apply[F[_]: RequestsState]: RequestsState[F] =
      implicitly[MonadState[F, Requests]]
  }

  def host[F[_]: ConfigAsk]: F[String] = ConfigAsk[F].reader(_.host)
  def port[F[_]: ConfigAsk]: F[Int]    = ConfigAsk[F].reader(_.port)

  def cityByName[F[_]: Applicative: ErrorHandler](cityName: String): F[City] =
    cityName match {
      case "Wroclaw"    => City(cityName).pure[F]
      case "Beer-Sheva" => City(cityName).pure[F]
      case _            => ErrorHandler[F].raiseError(UnknownCity(cityName))
    }

  def fetchForcast[F[_]: Weather: RequestsState: Monad](city: City): F[Forcast] =
    for {
      maybeForcast <- RequestsState[F].inspect(_.get(city))
      forcast <- maybeForcast.cata(
                  _.pure[F],
                  Weather[F].forcast(city)
                )
      _ <- RequestsState[F].modify(_ + (city -> forcast))
    } yield forcast

  def hottestCity[F[_]: RequestsState: Functor]: F[(City, Temperature)] =
    for {
      results <- RequestsState[F].inspect(
                  reqs =>
                    reqs.toList
                      .sortBy(_._2.temperature.value)
                      .map {
                        case (city, forcast) => (city, forcast.temperature)
                      }
                      .reverse)
    } yield results(0)

  def askCity[F[_]: Console: Monad: ErrorHandler]: F[City] =
    for {
      _        <- Console[F].printLn("What is the next city?")
      cityName <- Console[F].readLine
      city     <- cityByName[F](cityName)
    } yield city

  def askFetchJudge[F[_]: Console: Weather: RequestsState: ErrorHandler: Monad]
    : F[Unit] =
    for {
      city    <- askCity[F]
      forcast <- fetchForcast[F](city)
      _       <- Console[F].printLn(s"Forcast for $city is ${forcast.temperature}")
      hottest <- hottestCity[F]
      _       <- Console[F].printLn(s"Hottest city found so far is $hottest")
    } yield ()

  def program[F[_]: ConfigAsk: Console: Weather: RequestsState: ErrorHandler: Monad]
    : F[Unit] =
    for {
      h <- host[F]
      p <- port[F]
      _ <- Console[F].printLn(s"Using weather service at http://$h:$p \n")
      _ <- askFetchJudge[F].forever
    } yield ()

  def main(args: Array[String]): Unit = {

    val config   = Config("localhost", 8080)
    val requests = Requests.empty
    type Effect[A] = EitherT[Task, Error, A]

    implicit val requestsState = AtomicMonadState.create(Requests.empty)
    implicit val configAsk     = ApplicativeAsk.constant[Task, Config](config)
    implicit val weather       = Weather.monixWeather(config)
    implicit val console       = Console.monixConsole

    val app: Effect[Unit] = program[Effect]

    implicit val io: SchedulerService = Scheduler.io("io-scheduler")
    (app.run >>= {
      case -\/(error) => console.printLn(s"Encountered an error: ${error.shows}")
      case \/-(_)     => ().pure[Task]
    }).unsafeRunSync
  }
}
