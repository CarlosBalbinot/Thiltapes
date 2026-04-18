plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.frontend"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.frontend"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "environment"

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/api/\"")
            manifestPlaceholders["usesCleartextTraffic"] = true
        }

        create("prod") {
            dimension = "environment"
            buildConfigField("String", "API_BASE_URL", "\"https://thiltapes-prod.trycloudflare.com/api/\"")
            manifestPlaceholders["usesCleartextTraffic"] = false
        }
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
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ═══════════════════════════════════════════════════════════════
    // API Communication - Retrofit + OkHttp
    // ═══════════════════════════════════════════════════════════════
    // Retrofit - REST Client
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // Retrofit Gson Converter
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp - HTTP Client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // OkHttp Logging Interceptor (para debug)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson - JSON serialization/deserialization
    implementation("com.google.code.gson:gson:2.10.1")
}