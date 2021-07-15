plugins {
  `java-library`
  kotlin("jvm")
}
repositories {
  mavenCentral()
}
dependencies {
  api("com.google.inject:guice:5.0.1")
  api("org.slf4j:slf4j-api:1.7.30")
  val junitVersion = "5.7.1"
  api("org.junit.jupiter:junit-jupiter:$junitVersion")
  api("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
  api("org.junit.platform:junit-platform-launcher:1.7.1")
  implementation("org.jetbrains:annotations:20.1.0")
}
