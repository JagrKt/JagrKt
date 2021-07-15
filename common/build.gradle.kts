plugins {
  application
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.github.johnrengelman.shadow")
}
repositories {
  mavenCentral()
  maven("https://repo.spongepowered.org/repository/maven-public/")
}
dependencies {
  api(project(":jagr-grader-api"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
  val kotlinxSerializationVersion = "1.2.1"
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.0")
  implementation("org.fusesource.jansi:jansi:2.3.1")
  implementation("org.ow2.asm:asm:9.1")
  implementation("com.github.albfernandez:juniversalchardet:2.4.0")
  val configurateVersion = "4.1.1"
  implementation("org.spongepowered:configurate-hocon:$configurateVersion")
  implementation("org.spongepowered:configurate-extra-kotlin:$configurateVersion")
  implementation(kotlin("reflect"))
  implementation(files("../gradle/wrapper/gradle-wrapper.jar"))
}
application {
  mainClass.set("org.sourcegrade.jagr.common.MainKt")
}
tasks {
  shadowJar {
    from("../gradlew") {
      into("org/gradle")
    }
    from("../gradlew.bat") {
      into("org/gradle")
    }
    from("../gradle/wrapper/gradle-wrapper.properties") {
      into("org/gradle")
    }
    archiveFileName.set("Jagr-${project.version}.jar")
  }
  test {
    useJUnitPlatform()
  }
}
