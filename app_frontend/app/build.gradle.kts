import java.net.NetworkInterface
import java.net.Inet4Address

fun getLocalIP(): String {
    val defaultIp = "10.0.2.2"
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        val candidates = mutableListOf<Triple<String, String, String>>()
        while (interfaces.hasMoreElements()) {
            val netInterface = interfaces.nextElement()
            if (netInterface.isLoopback || !netInterface.isUp || netInterface.isVirtual) {
                continue
            }
            val addresses = netInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val addr = addresses.nextElement()
                if (addr is Inet4Address) {
                    val ip = addr.hostAddress
                    val name = netInterface.name.lowercase()
                    val displayName = netInterface.displayName.lowercase()
                    candidates.add(Triple(ip, name, displayName))
                }
            }
        }
        for (c in candidates) {
            val ip = c.first
            val name = c.second
            val displayName = c.third
            if (displayName.contains("wi-fi") || displayName.contains("wireless") || name.contains("wlan")) {
                if (displayName.contains("virtual") || displayName.contains("direct") || ip == "192.168.137.1") {
                    continue
                }
                return ip
            }
        }
        for (c in candidates) {
            val ip = c.first
            val name = c.second
            val displayName = c.third
            if (displayName.contains("ethernet") || name.contains("eth")) {
                return ip
            }
        }
        for (c in candidates) {
            val ip = c.first
            val displayName = c.third
            if (!displayName.contains("virtual") && !displayName.contains("host-only")) {
                return ip
            }
        }
        if (candidates.isNotEmpty()) {
            return candidates[0].first
        }
    } catch (e: Exception) {
        // ignore
    }
    return defaultIp
}

val resolvedIp = getLocalIP()
println("----------------------------------------")
println("   [SymtoTrack] SERVER_IP resolved to: $resolvedIp")
println("----------------------------------------")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.symtotrack"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.symtotrack"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "SERVER_IP", "\"$resolvedIp\"")
    }

    buildFeatures {
        buildConfig = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("org.json:json:20231013")

    // External APIs and Libraries
    implementation("com.razorpay:checkout:1.6.33")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}