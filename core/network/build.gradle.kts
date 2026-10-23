plugins { alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android) }
android {
    namespace = "dev.androidbasekit.network"
    compileSdk = 36
    defaultConfig { minSdk = 24; consumerProguardFiles("consumer-rules.pro") }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { viewBinding = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
dependencies {
    api(project(":core:common"))
    api(libs.retrofit)
    api(libs.okhttp)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    api(libs.lifecycle.runtime)
    api(libs.lifecycle.livedata)
    implementation(libs.timber)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit)
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
