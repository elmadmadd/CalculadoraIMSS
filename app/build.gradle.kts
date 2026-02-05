plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.consucalcu"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.consucalcu"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("com.google.code.gson:gson:2.10.1")

    // 2. Para el efecto de fiesta (Confeti)
    implementation("nl.dionsegijn:konfetti-compose:2.0.4")

    // 3. Para los iconos bonitos (RocketLaunch, Analytics, etc.)
    // ¡OJO! Sin esto, la mitad de tus iconos darán error.
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // 4. Asegúrate de tener Material3 actualizado para usar SwipeToDismissBox
    implementation("androidx.compose.material3:material3:1.2.1")

    // 5. ViewModel para Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // 6. Lottie Compose for Juicy Animations
    implementation("com.airbnb.android:lottie-compose:6.3.0")


}