plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.flammky.valorantcompanion.career"
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
    implementation(project(":base"))
    implementation(project(":auth"))
    implementation(project(":assets"))
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.activity.activity.compose)
    implementation(libs.androidx.compose.ui.ui.asProvider())
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.compose.material.material)
    implementation(libs.io.coil.kt.coil.asProvider())
    implementation(libs.io.coil.kt.coil.compose)
}