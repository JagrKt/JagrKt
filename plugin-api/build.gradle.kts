plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
}

val configurateVersion: String by project
val guiceVersion: String by project
val kotlinxSerializationVersion: String by project
val slf4jVersion: String by project

dependencies {
  api(project(":jagr-grader-api"))
  api("com.google.inject:guice:$guiceVersion")
  api("org.slf4j:slf4j-api:$slf4jVersion")
  implementation("org.spongepowered:configurate-hocon:$configurateVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.0")
  implementation("org.fusesource.jansi:jansi:2.3.1")
  implementation("com.github.albfernandez:juniversalchardet:2.4.0")
  implementation(kotlin("reflect"))
}
