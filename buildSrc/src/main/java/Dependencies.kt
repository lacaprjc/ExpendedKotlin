import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {
    private val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    private val appCompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    private val material = "com.google.android.material:material:${Versions.material}"
    private val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    private val fragmentWithKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.fragmentKtx}"
    private val uiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.uiKtx}"
    private val runtimeKtx = "androidx.navigation:navigation-runtime-ktx:${Versions.runtimeKtx}"
    private val lifeCycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycleExt}"
    private val lifeCycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime:${Versions.lifecycleRuntimeKtx}"
    private val liveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.liveDataKtx}"
    private val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.viewModelKtx}"
    private val hiltAndroid = "com.google.dagger:hilt-android:${Versions.hiltAndroid}"
    private val hiltViewModel = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hiltViewModel}"
    private val roomRuntime = "androidx.room:room-runtime:${Versions.roomRuntime}"
    private val roomKtx = "androidx.room:room-ktx:${Versions.roomKtx}"
    private val kotlinCsv = "com.github.doyaaaaaken:kotlin-csv-jvm:${Versions.kotlinCsv}"
    private const val firebaseAnalytics = "com.google.firebase:firebase-analytics-ktx"
    private const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics-ktx"
    private const val firebasePerformance = "com.google.firebase:firebase-perf-ktx"

    val appLibraries = arrayListOf(
        coreKtx,
        appCompat,
        material,
        constraintLayout,
        fragmentWithKtx,
        uiKtx,
        runtimeKtx,
        lifeCycleExtensions,
        lifeCycleRuntimeKtx,
        liveDataKtx,
        viewModelKtx,
        hiltAndroid,
        hiltViewModel,
        roomKtx,
        roomRuntime,
        kotlinCsv,
        firebaseAnalytics,
        firebaseCrashlytics,
        firebasePerformance
    )

    private val hiltCompiler = "com.google.dagger:hilt-compiler:${Versions.hiltCompiler}"
    private val hiltAndroidCompiler = "androidx.hilt:hilt-compiler:${Versions.hiltAndroidCompiler}"
    private val roomCompiler = "androidx.room:room-compiler:${Versions.roomCompiler}"

    val kaptLibraries = arrayListOf(
        hiltCompiler,
        hiltAndroidCompiler,
        roomCompiler
    )

    private val junit = "junit:junit:${Versions.junit}"
    private val archCoreTesting = "androidx.arch.core:core-testing:${Versions.archCore}"
    private val roomTesting = "androidx.room:room-testing:${Versions.roomTest}"
    private val mockito = "org.mockito:mockito-inline:${Versions.mockitoInline}"
    private val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    private val coroutinesTest ="org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutinesTest}"

    val localTestLibraries = arrayListOf<String>(
        junit,
        archCoreTesting,
        roomTesting,
        mockito,
        mockitoKotlin,
        coroutinesTest
    )

    private val junitExt = "androidx.test.ext:junit:${Versions.extJunit}"
    private val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"

    val instrumentedTestLibraries = arrayListOf<String>(
        junitExt,
        espressoCore
    )

    val jdkDesugar = "com.android.tools:desugar_jdk_libs:${Versions.jdkDesugar}"

    private val leakCanary = "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}"

    val debugAppLibraries = arrayListOf(
        leakCanary
    )

    val firebaseBom = "com.google.firebase:firebase-bom:${Versions.firebase}"
}

//util functions for adding the different type dependencies from build.gradle file
fun DependencyHandler.kapt(list: List<String>) {
    list.forEach { dependency ->
        add("kapt", dependency)
    }
}

fun DependencyHandler.platformImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("implementation platform", dependency)
    }
}

fun DependencyHandler.implementation(list: List<String>) {
    list.forEach { dependency ->
        add("implementation", dependency)
    }
}

fun DependencyHandler.debugImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("debugImplementation", dependency)
    }
}

fun DependencyHandler.androidTestImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("androidTestImplementation", dependency)
    }
}

fun DependencyHandler.testImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("testImplementation", dependency)
    }
}