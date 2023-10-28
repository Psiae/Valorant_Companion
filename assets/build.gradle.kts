plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.flammky.valorantcompanion.assets"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":base"))
    implementation(project(":pvp"))
    implementation(libs.koin.android)
    implementation(libs.io.ktor.ktor.client.android)
    implementation(libs.io.ktor.ktor.client.okhttp)
    implementation(libs.io.ktor.ktor.client.serialization)
    implementation(libs.io.ktor.ktor.client.auth)
    implementation(libs.io.ktor.ktor.client.logging)
    implementation(libs.io.ktor.ktor.client.content.negotiation)
    implementation(libs.io.ktor.ktor.serialization)
    implementation(libs.io.ktor.ktor.serialization.kotlinx.json)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.collections.immutable)
    implementation(libs.org.jetbrains.kotlinx.atomicfu.jvm)
    implementation(kotlin("reflect"))
}