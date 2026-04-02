plugins {
    alias(libs.plugins.android.library)
    id("com.vanniktech.maven.publish") version "0.34.0"
    id("signing")
}

android {
    namespace = "io.github.xesam.android.operatebar"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    compileOnly(libs.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

mavenPublishing {
    coordinates("io.github.xesam", "android-operatebar", "0.0.1")

    pom {
        name.set("android-operatebar")
        description.set("An Android custom View library for building a flexible bottom operation bar.")
        url.set("https://github.com/xesam/Android-OperateBar")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("xesam")
                name.set("xesam")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/xesam/Android-OperateBar.git")
            developerConnection.set("scm:git:ssh://git@github.com/xesam/Android-OperateBar.git")
            url.set("https://github.com/xesam/Android-OperateBar")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}
