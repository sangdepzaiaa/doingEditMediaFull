plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.ksp) // bật KSP
    id("kotlin-parcelize")
 //   id("com.google.gms.google-services")
 //   id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Tắt làm rối mã nguồn và tối ưu tài nguyên để tránh lỗi khởi tạo ViewModel
            isMinifyEnabled = false
            // Giữ nguyên phần signing và proguard (vì khi false nó sẽ không chạy)
            signingConfig = signingConfigs.getByName("debug")
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

//    // variant dev , product
//
//    flavorDimensions += "default"
//    productFlavors {
//        create("for_dev") {
//            dimension = "default"
//        }
//
////        // For product release uncomment out here
////        create("for_product") {
////            dimension = "default"
////        }
//    }
//
//    sourceSets {
//        getByName("for_dev") {
//            res.srcDirs("src/for_dev_/res")
//        }
//
////        // For product release uncomment out here
////        getByName("for_product") {
////            res.srcDirs("src/for_product_/res")
////        }
//
//    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    /** Lifecycle copy từ đây **/
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.process)

    implementation(libs.androidx.recyclerview)

    /** Firebase BOM **/
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")

    /** Koin **/
    implementation(libs.koin.android)

    /** Retrofit **/
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.gson)

    /** Ktor Client **/
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")

    /** Glide **/
    implementation(libs.glide)
    ksp(libs.glide.compiler)
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    /** Room (KSP) **/
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    /** Coroutines **/
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)

    /** Play Core Review **/
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")

    /** Splash Screen **/
    implementation("androidx.core:core-splashscreen:1.0.1")

    /** WorkManager (tối ưu) **/
    implementation("androidx.work:work-runtime-ktx:2.10.5")
    androidTestImplementation("androidx.work:work-testing:2.10.5")

    /** Máy ảo cũ cần multidex **/
    implementation("androidx.multidex:multidex:2.0.1")

    /** UI & hiệu ứng **/
    implementation("com.airbnb.android:lottie:6.5.2")
    implementation("com.github.zhouzhuo810:ZzHorizontalProgressBar:1.1.1")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.github.ybq:Android-SpinKit:1.4.0")

    /** ML Kit Face Detection **/
    implementation("com.google.mlkit:face-detection:16.1.7")

    implementation("com.google.code.gson:gson:2.13.2")


}