plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.kapt")
	id("com.google.dagger.hilt.android")
}


android {
    namespace = "com.serranoie.app.media.sorter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.serranoie.app.media.sorter"
        minSdk = 31
        targetSdk = 36
        versionCode = 102
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            // Debug builds use the default debug signing config
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
    
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.material3)
	implementation(libs.androidx.work.runtime.ktx)
	// Unit Testing
	testImplementation(libs.junit)
	testImplementation("io.mockk:mockk:1.13.14")
	testImplementation("io.mockk:mockk-android:1.13.14")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
	testImplementation("androidx.arch.core:core-testing:2.2.0")
	testImplementation("app.cash.turbine:turbine:1.2.0")
	
	// UI Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.telephoto.zoomable.image.coil)

    // ExoPlayer for video playback
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    
    // Wavy Slider for video progress
    implementation(libs.wavy.slider)

	implementation(libs.hilt.android)
	kapt(libs.hilt.android.compiler)
	implementation(libs.androidx.hilt.navigation.compose)
	
	// Hilt Work integration
	kapt(libs.androidx.hilt.compiler)
	implementation(libs.androidx.hilt.work)

	// Update checking dependencies
	implementation(libs.app.update)
	implementation(libs.app.update.ktx)
	implementation(libs.androidx.work.runtime.ktx)
	implementation(libs.retrofit)
	implementation(libs.retrofit.gson)
	implementation(libs.okhttp)
	implementation(libs.okhttp.logging)

	implementation("androidx.compose.animation:animation:1.6.7")
}

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/../detekt.yml")
    baseline = file("$projectDir/../detekt-baseline.xml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(false)
    }
}