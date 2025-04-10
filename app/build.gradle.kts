plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.escom.practica3"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.escom.practica3"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    
    // Add AppCompat dependency
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")
    
    // Constraint Layout para diseños responsivos
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Material Design para componentes de UI
    implementation("com.google.android.material:material:1.11.0")
    
    // Añade estas dependencias en la sección dependencies
    dependencies {
        // Dependencias existentes...
        
        // Glide for image loading and caching
        implementation("com.github.bumptech.glide:glide:4.16.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
        
        // Gson for JSON serialization/deserialization
        implementation("com.google.code.gson:gson:2.10.1")
    }
}