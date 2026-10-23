plugins { alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android) }
android {
    namespace = "dev.androidbasekit.ui"
    compileSdk = 36
    defaultConfig { minSdk = 24; consumerProguardFiles("consumer-rules.pro") }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { viewBinding = true }
    testOptions { unitTests.isIncludeAndroidResources = true }
}
dependencies {
    api(project(":core:common"))
    api(project(":core:lifecycle"))
    api(libs.appcompat)
    api(libs.fragment)
    api(libs.lifecycle.viewmodel)
    api(libs.lifecycle.livedata)
    api(libs.recyclerview)
    api(libs.material)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
