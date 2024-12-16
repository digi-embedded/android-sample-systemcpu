plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.digi.android.sample.system.cpu"
    compileSdkVersion("Digi International:Digi SDK Add-On for Embedded:34")

    defaultConfig {
        applicationId = "com.digi.android.sample.system.cpu"
        minSdk = 34
        targetSdk = 34
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
}

dependencies {
    implementation(libs.material)
    implementation(libs.firebase)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":androidplot-core-1.5.7"))
    implementation(project(":figlib-1.0.7"))
}