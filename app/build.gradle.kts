import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    id("com.android.application")
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
    }

//    signingConfigs {
//        getByName("release") {
//
//        }
//    }

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

    implementation(Dependencies.appLibraries)
    coreLibraryDesugaring(Dependencies.jdkDesugar)
    kapt(Dependencies.kaptLibraries)
    testImplementation(Dependencies.localTestLibraries)
    androidTestImplementation(Dependencies.instrumentedTestLibraries)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}