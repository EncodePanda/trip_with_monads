package twm

sealed trait TempUnit
case object Celcius    extends TempUnit
case object Fahrenheit extends TempUnit

case class Temperature(value: Int, unit: TempUnit = Celcius)
case class Forcast(temperature: Temperature)
case class City(name: String)

class WeatherClient(host: String, port: Int) {
  def forcast(city: City): Forcast = city match {
    case City("Wroclaw")    => Forcast(Temperature(28))
    case City("Beer-Sheva") => Forcast(Temperature(34))
    case _                  => Forcast(Temperature(15))
  }
}
