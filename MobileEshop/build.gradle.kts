// Root build.gradle.kts
plugins {
    id("com.android.application") version "8.0.2" apply false
    kotlin("android") version "1.9.10" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
