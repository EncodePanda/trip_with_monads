name := "twm"

scalaVersion := "2.12.4"

resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")

scalacOptions ++= Seq("-language:higherKinds")

scalafmtOnCompile := true

val scalaz_mtl = ProjectRef(uri("git://github.com/rabbitonweb/scalaz-mtl.git#0f9fb0635852ba1d58a11d1fa19e053e3b2b7b84"), "root")
val zio = ProjectRef(uri("git://github.com/scalaz/scalaz-zio.git"), "coreJVM")

lazy val root = Project("root", file("."))
  .settings(libraryDependencies += "org.scalaz"     %% "scalaz-core" % "7.2.24")
  .settings(libraryDependencies += "io.monix"       %% "monix"       % "3.0.0-RC1")
  .settings(libraryDependencies += "com.codecommit" %% "shims"       % "1.3.0")
  .dependsOn(scalaz_mtl,zio)

