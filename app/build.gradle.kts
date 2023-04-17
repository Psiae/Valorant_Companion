plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.flammky.valorantcompanion"
    compileSdk = 33

    defaultConfig {
        applicationId = "dev.flammky.valorantcompanion"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        composeOptions.kotlinCompilerExtensionVersion = "1.4.0"
    }
    packagingOptions {
    }
}

dependencies {
    implementation(project(":base"))
    implementation(project(":auth"))

    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.activity.activity.compose)
    implementation(libs.androidx.compose.ui.ui.asProvider())
    implementation(libs.androidx.compose.ui.ui.tooling.preview)

    val compose_ui_version = "1.4.0"

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.ui:ui:$compose_ui_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_ui_version")
    implementation("androidx.compose.material:material:1.3.1")
}