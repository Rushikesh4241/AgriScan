pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()        // ✅ REQUIRED for Firebase & Play Services
        mavenCentral()  // ✅ REQUIRED for other libs
    }
}

rootProject.name = "AgriScan"
include(":app")
