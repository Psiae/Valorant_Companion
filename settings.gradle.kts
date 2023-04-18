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
        google()
        mavenCentral()
    }
    versionCatalogs {
        val compose_ui_version = "1.4.0"
        create("libs") {
            library(
                "koin-android",
                "io.insert-koin",
                "koin-android"
            ).version("3.3.3")
            library(
                "koin-androidx-compose",
                "io.insert-koin",
                "koin-androidx-compose"
            ).version("3.4.2")

            library(
                "androidx.core-core-ktx",
                "androidx.core",
                "core-ktx"
            ).version("1.9.0")

            library(
                "androidx.activity-activity-compose",
                "androidx.activity",
                "activity-compose"
            ).version("1.6.1")


            // androidx.compose
            library(
                "androidx.compose.ui-ui",
                "androidx.compose.ui",
                "ui"
            ).version(compose_ui_version)
            library(
                "androidx.compose.ui-ui-tooling-preview",
                "androidx.compose.ui",
                "ui-tooling-preview"
            ).version(compose_ui_version)
            library(
                "androidx.compose.material-material",
                "androidx.compose",
                "material:material"
            ).version("1.3.1")

            // KTOR
            library(
                "io.ktor.ktor-server-core",
                "io.ktor",
                "ktor-server-core"
            ).version("2.2.4")
            library(
                "io.ktor.ktor-server-netty",
                "io.ktor",
                "ktor-server-netty"
            ).version("2.2.4")
            library(
                "io.ktor.ktor-client.android",
                "io.ktor",
                "ktor-client-android"
            ).version("2.2.4")
            library(
                "io.ktor.ktor-client.okhttp",
                "io.ktor",
                "ktor-client-okhttp"
            ).version("2.2.4")
            library(
                "io.ktor.ktor-client.serialization",
                "io.ktor",
                "ktor-client-serialization"
            ).version("2.2.4")
        }
    }
}
rootProject.name = "Valorant Companion"
include(":app")
include(":auth")
include(":base")
include(":career")
include(":live")
