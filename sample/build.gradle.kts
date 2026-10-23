plugins { alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) }
android {
    namespace = "dev.androidbasekit.sample"
    compileSdk = 36
    defaultConfig {
        applicationId = "dev.androidbasekit.sample"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { viewBinding = true; buildConfig = true }
    buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:image"))
    implementation(project(":core:storage"))
    implementation(project(":core:lifecycle"))
    implementation(project(":core:ui"))
    implementation(project(":core:system"))
    implementation(libs.viewpager2)
    implementation(libs.timber)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(libs.androidx.test.core)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.room.testing)
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
