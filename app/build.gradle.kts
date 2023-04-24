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
        composeOptions.kotlinCompilerExtensionVersion = "1.4.4"
    }
    packagingOptions {
        resources.pickFirsts.add("META-INF/INDEX.LIST")
        resources.pickFirsts.add("META-INF/io.netty.*")
    }
}

dependencies {
    implementation(project(":base"))
    implementation(project(":auth"))
    implementation(project(":career"))
    implementation(project(":assets"))
    implementation(project(":pvp"))

    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.activity.activity.compose)
    implementation(libs.androidx.compose.ui.ui.asProvider())
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.compose.material.material)
}