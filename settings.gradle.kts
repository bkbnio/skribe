rootProject.name = "sourdough-gradle"

include("root")
include("application-jvm")
include("library-jvm")
include("library-mpp")

// Set Project Gradle Names
run {
  rootProject.children.forEach { it.name = "${rootProject.name}-${it.name}" }
}
