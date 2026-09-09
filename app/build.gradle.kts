plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.vibe.forge"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vibe.forge"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            // BouncyCastle jars (bcpkix/bcutil/bcprov) all ship the same
            // OSGI-INF/MANIFEST.MF - exclude duplicates from the APK.
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "META-INF/{AL,BC,DSA,EC,RSA,SF}"
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.github.Rosemoe.sora-editor:editor:0.23.4")
    implementation("io.github.Rosemoe.sora-editor:language-textmate:0.23.4")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
    // In-process MODE_A build pipeline: ecj (Java compiler), r8 (D8 dexer) and
    // apksig (APK signer) run via their programmatic JVM APIs - they are
    // regular JVM bytecode and cannot be exec'd via dalvikvm as a subprocess.
    // AGP dexes them into the app classpath automatically.
    implementation("org.eclipse.jdt:ecj:3.33.0")
    implementation("com.android.tools:r8:8.3.37")
    implementation("com.android.tools.build:apksig:8.3.2")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78") // self-signed debug keystore
    debugImplementation("androidx.compose.ui:ui-tooling")
}
