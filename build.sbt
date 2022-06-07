scalaVersion := "3.1.2"

libraryDependencies += "org.fomkin" %% "korolev-standalone" % "1.3.0"

enablePlugins(UniversalPlugin, AshScriptPlugin, DockerPlugin)

Docker / packageName := "holyjs-livecoding-app"

Docker / version := "1.0.0"

Docker / maintainer := "Aleksey Fomkin <aleksey.fomkin@gmail.com>"

dockerBaseImage := "adoptopenjdk"

dockerExposedPorts := Seq(8080)

dockerUsername := Some("fomkin")

dockerUpdateLatest := true
