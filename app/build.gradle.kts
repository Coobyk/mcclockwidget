plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// The release workflow stamps a tag build with -PversionName / -PversionCode. Without them a plain
// local build just gets the defaults below.
val appVersionCode = (findProperty("versionCode") as String?)?.toInt() ?: 1
val appVersionName = (findProperty("versionName") as String?) ?: "1.0"

// Signing material comes from the environment so the key never lands in the repo. The release
// workflow decodes it from repository secrets; a plain `./gradlew assembleRelease` on a fresh
// checkout has no key and produces an unsigned APK instead of failing.
val keystorePath: String? = System.getenv("MCCLOCK_KEYSTORE")
val keystorePassword: String? = System.getenv("MCCLOCK_KEYSTORE_PASSWORD")
val keystoreAlias: String? = System.getenv("MCCLOCK_KEY_ALIAS")
val keystoreKeyPassword: String? = System.getenv("MCCLOCK_KEY_PASSWORD")

android {
    namespace = "com.coobyk.mc.clock"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.coobyk.mc.clock"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = keystoreAlias
                keyPassword = keystoreKeyPassword
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
    }
}

// WidgetRenderTest writes what it drew to build/widget-renders so a run can be eyeballed afterwards,
// and CI uploads that directory. Declaring it as a task output means the Gradle build cache restores
// the PNGs on a cache hit; left undeclared they simply do not exist whenever the test task is not
// actually executed, which is how the upload step came to fail on an otherwise green build.
tasks.withType<Test>().configureEach {
    outputs.dir(layout.buildDirectory.dir("widget-renders"))
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
