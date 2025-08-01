plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")                          // ← enable annotation processing for Glide
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mobileeshop"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mobileeshop"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true                       // you can keep Compose on
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    // AndroidX + AppCompat
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.6.0")

    // Material Components (includes RangeSlider)
    implementation("com.google.android.material:material:1.9.0")

    // Compose (optional in your case)
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.material:material:1.5.3")
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")

    // Firebase
    implementation("com.google.firebase:firebase-firestore-ktx:24.7.1")
    implementation("com.google.firebase:firebase-analytics-ktx:21.3.0")
    implementation("com.google.firebase:firebase-auth-ktx:22.1.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.billing)
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
}

private fun DependencyHandlerScope.kapt(string: kotlin.String) {}
