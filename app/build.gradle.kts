plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.chaquopy)
}

android {
    namespace = "com.oneandonly.pythoncompiler"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.oneandonly.pythoncompiler"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // ABIs to bundle CPython for. Chaquopy's Python 3.13 only ships arm64-v8a and
            // x86_64 builds (no 32-bit armeabi-v7a/x86) - these two cover effectively all
            // real Android devices (32-bit-only devices require API < 21 anyway).
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    chaquopy {
        defaultConfig {
            version = "3.13"
            // Overridable via -PchaquopyPython=... (e.g. in CI); defaults to the local dev path.
            buildPython((project.findProperty("chaquopyPython") as String?) ?: "C:/Python313/python.exe")
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            // Sign with the debug key so CI-built release APKs are directly installable.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.sora.editor)
    implementation(libs.sora.language.textmate)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
