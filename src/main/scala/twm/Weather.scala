package twm

import monix.eval.Task
import scalaz._, Scalaz._, mtl._
import domain._
import shims._

trait Weather[F[_]] {
  def forcast(city: City): F[Forcast]
}

object WeatherForTrans {
  def forTrans[F[_]: Monad, T[_[_], _]: MonadTrans](
      implicit W: Weather[F]): Weather[T[F, ?]] = new Weather[T[F, ?]] {
    def forcast(city: City): T[F, Forcast] =
      MonadTrans[T].liftM(Weather[F].forcast(city))
  }
}

object Weather extends Weather0 {
  def apply[F[_]: Weather]: Weather[F] = implicitly[Weather[F]]

  def monixWeather(config: Config): Weather[Task] =
    new Weather[Task] {
      val client: WeatherClient = new WeatherClient(config.host, config.port)
      def forcast(city: City): Task[Forcast] = Task.delay {
        client.forcast(city)
      }
    }

}

trait Weather0 {
  implicit def eitherTWeather[F[_]: Monad: Weather, E]: Weather[EitherT[F, E, ?]] =
    WeatherForTrans.forTrans[F, EitherT[?[_], E, ?]]
}
