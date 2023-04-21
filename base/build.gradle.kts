plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.flammky.valorantcompanion.base"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }

    buildTypes {
    }
    buildFeatures {
        compose = true
        composeOptions.kotlinCompilerExtensionVersion = "1.4.4"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packagingOptions {
        resources.pickFirsts.add("META-INF/INDEX.LIST")
    }
}

dependencies {
    api("com.google.android.material:material:1.5.0")
    val compose_ui_version = "1.4.0"
    api(libs.androidx.core.core.ktx)
    api("androidx.activity:activity-compose:1.6.1")
    api("androidx.compose.ui:ui:$compose_ui_version")
    api("androidx.compose.ui:ui-tooling-preview:$compose_ui_version")

    val vFoundation = "1.3.1"
    // Foundation
    api("androidx.compose.foundation:foundation:$vFoundation")

    // Material
    api("androidx.compose.material:material:$vFoundation")

    // Material3
    val vMaterial3 = "1.0.1"
    api("androidx.compose.material3:material3:$vMaterial3")
    api("io.insert-koin:koin-android:3.3.3")
    api("io.insert-koin:koin-androidx-compose:3.4.2")
    api("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
}