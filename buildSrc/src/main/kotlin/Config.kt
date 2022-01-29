
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories

fun Project.configRepository() {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.mozilla.org/maven2/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

fun com.android.build.gradle.internal.dsl.BaseAppModuleExtension.setup() {
    compileSdk = Versions.Android.compile
    buildToolsVersion = Versions.Android.buildTools
    defaultConfig {
        applicationId = Package.id
        minSdk = Versions.Android.min
        targetSdk = Versions.Android.target
        versionCode = Package.versionCode
        versionName = Package.versionName
    }

    compileOptions {
        sourceCompatibility = Versions.Java.java
        targetCompatibility = Versions.Java.java
    }
}

fun com.android.build.api.dsl.LibraryExtension.setupLibrary() {
    compileSdk = Versions.Android.compile
    defaultConfig {
        minSdk = Versions.Android.min
        targetSdk = Versions.Android.target
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = Versions.Java.java
        targetCompatibility = Versions.Java.java
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}


fun com.android.build.gradle.LibraryExtension.withCompose() {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose
    }
}