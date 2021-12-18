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

dependencies {
    runtimeOnly(project("jagr-core"))
    runtimeOnly("fr.inria.gforge.spoon:spoon-core:10.0.0")
    implementation(project("jagr-launcher"))
    implementation("com.github.ajalt.clikt:clikt:3.3.0")
}

application {
    mainClass.set("org.sourcegrade.jagr.MainKt")
}

tasks {
    named<JavaExec>("run") {
        doFirst {
            error("Use runShadow instead")
        }
    }
    named<JavaExec>("runShadow") {
        workingDir = File("build/run").also(File::mkdirs)
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

project.extra["apiVersion"] = "0.3-SNAPSHOT"

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = "org.sourcegrade"
    version = "0.3.0-SNAPSHOT"

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
