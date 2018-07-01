package twm

import scalaz._

object domain {
  case class Config(host: String, port: Int)
  sealed trait Error
  case class UnknownCity(city: String) extends Error

  implicit val show: Show[Error] = Show.showFromToString[Error]
}
