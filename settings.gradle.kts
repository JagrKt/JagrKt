rootProject.name = "Jagr"
include(":jagr-grader-api")
include(":jagr-core")
project(":jagr-grader-api").projectDir = File("grader-api")
project(":jagr-core").projectDir = File("core")
