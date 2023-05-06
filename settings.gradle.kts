rootProject.name = "skribe"

include("plugin")

// Set Project Gradle Names
run {
  rootProject.children.forEach { it.name = "${rootProject.name}-${it.name}" }
}
