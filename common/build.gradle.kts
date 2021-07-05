plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("com.github.johnrengelman.shadow")
}

val configurateVersion: String by project
val kotlinxSerializationVersion: String by project

repositories {
  mavenCentral()
  maven("https://repo.spongepowered.org/repository/maven-public/")
}
dependencies {
  api(project(":jagrkt-launcher"))
  api(project(":jagrkt-plugin-api"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("org.spongepowered:configurate-core:$configurateVersion")
  implementation("org.ow2.asm:asm:9.1")
  implementation("com.github.albfernandez:juniversalchardet:2.4.0")
  implementation(kotlin("reflect"))
  implementation(files("../gradle/wrapper/gradle-wrapper.jar"))
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
    archiveFileName.set("JagrKt-${project.version}.jar")
  }
  test {
    useJUnitPlatform()
  }
}
