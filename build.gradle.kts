buildscript {

    val vKotlin = "1.8.10"
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:$vKotlin")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$vKotlin")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.20.2")
    }
    repositories {
        mavenCentral()
        google()
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.3.1" apply false
    id("com.android.library") version "7.3.1" apply false
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.10" apply false
}

apply(plugin = "kotlinx-atomicfu")