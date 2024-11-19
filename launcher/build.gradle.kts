import org.sourcegrade.jagr.script.apiProject

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("jagr-publish")
    id("jagr-sign")
}

dependencies {
    apiProject(project, "jagr-grader-api")
    api(libs.coroutines) {
        exclude("org.jetbrains", "annotations")
    }
    implementation(kotlin("reflect"))
    implementation(libs.configurate.hocon)
    implementation(libs.configurate.kotlin)
    implementation(libs.annotations)
    implementation(libs.serialization)
    implementation(libs.logging.core)
    kapt(libs.logging.core)
}

tasks {
    withType<ProcessResources> {
        from(rootProject.file("version")) {
            into("org/sourcegrade/jagr/")
        }
    }
}
