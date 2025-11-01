import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val majorVersion = 2
val minorVersion = 1
val patchVersion = 0

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "win.downops.wallettracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "win.downops.wallettracker"
        minSdk = 24
        targetSdk = 34
        versionCode = majorVersion * 10000 + minorVersion * 100 + patchVersion
        versionName = "$majorVersion.$minorVersion.$patchVersion"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.141:5000\"")
            buildConfigField("String", "API_VERSION", "\"1\"")
            buildConfigField("String", "DEFAULT_USER", "\"noel\"")
            buildConfigField("String", "DEFAULT_PASSWORD", "\"noelnoel\"")
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"http://api.wallettracker.downops.win\"")
            buildConfigField("String", "API_VERSION", "\"1\"")
            buildConfigField("String", "DEFAULT_USER", "\"\"")
            buildConfigField("String", "DEFAULT_PASSWORD", "\"\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.ui.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.swiperefreshlayout)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.biometric)
}