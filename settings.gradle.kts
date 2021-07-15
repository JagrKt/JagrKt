rootProject.name = "Jagr"
include(":jagr-grader-api")
include(":jagr-common")
project(":jagr-grader-api").projectDir = File("grader-api")
project(":jagr-common").projectDir = File("common")
