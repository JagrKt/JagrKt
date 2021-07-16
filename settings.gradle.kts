rootProject.name = "Jagr"
include(":jagr-core")
include(":jagr-grader-api")
include(":jagr-launcher")
include(":jagr-plugin-api")
project(":jagr-core").projectDir = File("core")
project(":jagr-grader-api").projectDir = File("grader-api")
project(":jagr-launcher").projectDir = File("launcher")
project(":jagr-plugin-api").projectDir = File("plugin-api")
