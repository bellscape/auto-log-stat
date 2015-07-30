
scalaVersion := "2.11.6"

// common

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"

libraryDependencies += "joda-time" % "joda-time" % "2.8.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.36"

libraryDependencies += "com.alibaba" % "druid" % "1.0.15"

libraryDependencies += "commons-dbutils" % "commons-dbutils" % "1.6"

// for crontab

libraryDependencies += "ch.ethz.ganymed" % "ganymed-ssh2" % "262"

// for web

//libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"
libraryDependencies += "javax" % "javaee-api" % "6.0" % "provided"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.0"

libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.6.0-1"

enablePlugins(JettyPlugin)

