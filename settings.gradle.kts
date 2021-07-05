rootProject.name = "JagrKt"
include(":jagrkt-grader-api")
include(":jagrkt-plugin-api")
include(":jagrkt-common")
include(":jagrkt-launcher")
project(":jagrkt-grader-api").projectDir = File("grader-api")
project(":jagrkt-plugin-api").projectDir = File("plugin-api")
project(":jagrkt-common").projectDir = File("common")
project(":jagrkt-launcher").projectDir = File("launcher")
