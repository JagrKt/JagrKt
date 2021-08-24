import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.sourcegrade.jagr.script.JagrPublishPlugin

plugins {
  val kotlinVersion = "1.5.0"
  kotlin("jvm").version(kotlinVersion)
  kotlin("plugin.serialization").version(kotlinVersion)
  id("com.github.johnrengelman.shadow").version("7.0.0")
}

allprojects {
  group = "org.sourcegrade"
  version = "0.1.0-SNAPSHOT"

  project.findProperty("buildNumber")
    ?.takeIf { version.toString().contains("SNAPSHOT") }
    ?.also { version = version.toString().replace("SNAPSHOT", "RC$it") }

  repositories {
    mavenCentral()
  }

  tasks {
    withType<KotlinCompile> {
      kotlinOptions.jvmTarget = "11"
    }
    withType<JavaCompile> {
      options.encoding = "UTF-8"
      sourceCompatibility = "11"
      targetCompatibility = "11"
    }
  }
}

subprojects {
  apply<JagrPublishPlugin>()
}
