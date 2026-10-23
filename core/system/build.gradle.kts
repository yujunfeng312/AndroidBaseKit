plugins { alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android) }
android {
    namespace = "dev.androidbasekit.system"
    compileSdk = 36
    defaultConfig { minSdk = 24; consumerProguardFiles("consumer-rules.pro") }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { viewBinding = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
dependencies {
    api(libs.activity)
    api(libs.core)
    api(libs.navigation.fragment)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
