package twm

object ski {
  def κ[A, B](x: => B): A => B                = _ => x
  def κ2[A, B, C](x: => C): (A, B) => C       = (_, _) => x
  def κ3[A, B, C, D](x: => D): (A, B, C) => D = (_, _, _) => x
  def κ6[A, B, C, D, E, F, G](x: G): (A, B, C, D, E, F) => G =
    (_, _, _, _, _, _) => x
  def id[A]: A => A = x => x
}
