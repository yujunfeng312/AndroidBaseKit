pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "AndroidBaseKit"
include(":core:common", ":core:network", ":core:image", ":core:storage", ":core:lifecycle", ":core:ui", ":core:system", ":sample")
