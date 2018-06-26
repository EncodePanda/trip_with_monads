name := "twm"

scalaVersion := "2.12.4"

val scalaz_mtl = ProjectRef(uri("git://github.com/rabbitonweb/scalaz-mtl.git"), "root")
val zio = ProjectRef(uri("git://github.com/scalaz/scalaz-zio.git"), "coreJVM")

lazy val root = Project("root", file("."))
  .settings(libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.24")
  .dependsOn(scalaz_mtl,zio)

