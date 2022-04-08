import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.sourcegrade.jagr.script.JagrPublishPlugin

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

val cliktVersion: String by project

dependencies {
    runtimeOnly(project("jagr-core"))
    implementation(project("jagr-launcher"))
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
}

application {
    mainClass.set("org.sourcegrade.jagr.MainKt")
}

tasks {
    val runDir = File("build/run")
    named<JavaExec>("run") {
        doFirst {
            error("Use runShadow instead")
        }
    }
    named<JavaExec>("runShadow") {
        doFirst {
            runDir.mkdirs()
        }
        workingDir = runDir
    }
    jar {
        enabled = false
    }
    shadowJar {
        transform(Log4j2PluginsCacheFileTransformer::class.java)
        from("gradlew") {
            into("org/gradle")
        }
        from("gradlew.bat") {
            into("org/gradle")
        }
        from("gradle/wrapper/gradle-wrapper.properties") {
            into("org/gradle")
        }
        archiveFileName.set("Jagr-${project.version}.jar")
    }
}

project.extra["apiVersion"] = "0.5-SNAPSHOT"

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = "org.sourcegrade"
    version = "0.4.1-SNAPSHOT"

    project.findProperty("buildNumber")
        ?.takeIf { version.toString().contains("SNAPSHOT") }
        ?.also { version = version.toString().replace("SNAPSHOT", "RC$it") }

    repositories {
        mavenCentral()
    }

    configure<KtlintExtension> {
        enableExperimentalRules.set(true)
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
