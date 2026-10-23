plugins { alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android) }
android {
    namespace = "dev.androidbasekit.lifecycle"
    compileSdk = 36
    defaultConfig { minSdk = 24; consumerProguardFiles("consumer-rules.pro") }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { viewBinding = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
dependencies {
    api(libs.lifecycle.runtime)
    api(libs.lifecycle.process)
    api(libs.lifecycle.livedata)
    api(libs.work)
    testImplementation(libs.junit)
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
