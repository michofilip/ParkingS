name := "ParkingS"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies += "org.springframework.boot" % "spring-boot-starter-web" % "1.5.17.RELEASE"
//libraryDependencies += "org.springframework.boot" % "spring-boot-starter-data-jpa" % "1.5.17.RELEASE"
//libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.47"
libraryDependencies += "org.hibernate" % "hibernate-core" % "5.3.7.Final"
libraryDependencies += "org.hibernate" % "hibernate-java8" % "5.3.7.Final"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.6"

mainClass in Compile := Some("com.example.ParkingS.Application")
enablePlugins(JavaAppPackaging)

//assemblyMergeStrategy in assembly := {
//    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
//    case x => MergeStrategy.first
//}
