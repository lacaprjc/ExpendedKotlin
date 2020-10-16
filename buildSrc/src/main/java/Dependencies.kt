import org.gradle.api.artifacts.dsl.DependencyHandler

object Dependencies {
    private val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    private val appCompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    private val material = "com.google.android.material:material:${Versions.material}"
    private val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    private val fragmentWithKtx = "androidx.navigation:navigation-fragment-ktx:${Versions.fragmentKtx}"
    private val uiKtx = "androidx.navigation:navigation-ui-ktx:${Versions.uiKtx}"
    private val lifeCycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycleExt}"
    private val lifeCycleRuntimeKtx = "androidx.lifecycle:lifecycle-runtime:${Versions.lifecycleRuntimeKtx}"
    private val liveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.liveDataKtx}"
    private val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.viewmodelKtx}"
    private val hiltAndroid = "com.google.dagger:hilt-android:${Versions.hiltAndroid}"
    private val hiltViewModel = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hiltViewModel}"
    private val roomRuntime = "androidx.room:room-runtime:${Versions.roomRuntime}"
    private val roomKtx = "androidx.room:room-ktx:${Versions.roomKtx}"
    private val kotlinCsv = "com.github.doyaaaaaken:kotlin-csv-jvm:${Versions.kotlinCsv}"

    val jdkDesugar = "com.android.tools:desugar_jdk_libs:${Versions.jdkDesugar}"

    val appLibraries = arrayListOf(
        coreKtx,
        appCompat,
        material,
        constraintLayout,
        fragmentWithKtx,
        uiKtx,
        lifeCycleExtensions,
        lifeCycleRuntimeKtx,
        liveDataKtx,
        viewModelKtx,
        hiltAndroid,
        hiltViewModel,
        jdkDesugar,
        roomKtx,
        roomRuntime,
        kotlinCsv
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
}

//util functions for adding the different type dependencies from build.gradle file
fun DependencyHandler.kapt(list: List<String>) {
    list.forEach { dependency ->
        add("kapt", dependency)
    }
}

fun DependencyHandler.implementation(list: List<String>) {
    list.forEach { dependency ->
        add("implementation", dependency)
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