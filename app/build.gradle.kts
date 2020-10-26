import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.github.ben-manes.versions") version "0.33.0"
}

apply(plugin = "kotlin-android")
apply(plugin = "kotlin-android-extensions")
apply(plugin = "org.jetbrains.kotlin.kapt")
apply(plugin = "androidx.navigation.safeargs.kotlin")
apply(plugin = "dagger.hilt.android.plugin")

android {
    compileSdkVersion(AppConfig.compileSdk)
    buildToolsVersion(AppConfig.buildToolsVersion)

    defaultConfig {
        applicationId = "com.lacaprjc.expended"
        minSdkVersion(AppConfig.minSdk)
        targetSdkVersion(AppConfig.targetSdk)
        versionCode = AppConfig.versionCode
        versionName = AppConfig.versionName
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = AppConfig.androidTestImplementation

        javaCompileOptions {
            annotationProcessorOptions {
                arguments.putIfAbsent("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

//    signingConfigs {
//        getByName("release") {
//
//        }
//    }
    sourceSets {
        getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt")
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    viewBinding {
        android.buildFeatures.viewBinding = true
    }

    lintOptions {
        disable("NewApi")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(platform(Dependencies.firebaseBom))
    implementation(Dependencies.appLibraries)
    debugImplementation(Dependencies.debugAppLibraries)
    coreLibraryDesugaring(Dependencies.jdkDesugar)
    kapt(Dependencies.kaptLibraries)
    testImplementation(Dependencies.localTestLibraries)
    androidTestImplementation(Dependencies.instrumentedTestLibraries)
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-Xopt-in=kotlin.RequiresOptIn",
        "-Xopt-in=kotlin.OptIn"
    )
}