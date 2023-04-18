plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.flammky.valorantcompanion.live"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        targetSdk = 33
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
    packagingOptions {
        resources.pickFirsts.add("META-INF/INDEX.LIST")
    }
}

dependencies {
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.androidx.compose.ui.ui.asProvider())
    implementation(libs.androidx.compose.ui.ui.tooling.preview)
    implementation(libs.androidx.activity.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}