plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.example.pickii.shared"
        compileSdk = 36
        minSdk = 24

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            // 스파이크용 임시 제거, 버전 정책 결정 후 복구 (koin-core iOS klib가
            // Kotlin 2.3.20/ABI 2.3.0로 빌드돼 있어 2.2.10 컴파일러가 못 읽음)
            // implementation(libs.koin.core)
            // 스파이크용 임시 제거, 버전 정책 결정 후 복구 (ktor 3.5.2 전체가 iOS klib를
            // Kotlin 2.3.21/ABI 2.3.0로 빌드해서 2.2.10 컴파일러가 못 읽음)
            // implementation(libs.ktor.client.core)
            // implementation(libs.ktor.client.content.negotiation)
            // implementation(libs.ktor.serialization.kotlinx.json)
            // 스파이크용 임시 제거, 버전 정책 결정 후 복구 (ktor-client-logging
            // iOS klib가 Kotlin 2.3.21/ABI 2.3.0로 빌드돼 있어 2.2.10 컴파일러가 못 읽음)
            // implementation(libs.ktor.client.logging)
            // 스파이크용 임시 제거, 버전 정책 결정 후 복구 (ktor-client-auth
            // iOS klib가 Kotlin 2.3.21/ABI 2.3.0로 빌드돼 있어 2.2.10 컴파일러가 못 읽음)
            // implementation(libs.ktor.client.auth)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
        androidMain.dependencies {
            // 스파이크용 임시 제거, 버전 정책 결정 후 복구 (commonMain의 koin-core와 짝)
            // implementation(libs.koin.android)
            // 스파이크용 임시 제거, 버전 정책 결정 후 복구 (HttpClientEngine.android.kt와 짝,
            // Android 자체는 ABI 문제 없지만 쓰는 곳이 없어져서 같이 뺌)
            // implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            // 스파이크용 임시 제거, 버전 정책 결정 후 복구 (ktor-client-darwin
            // iOS klib가 Kotlin 2.3.21/ABI 2.3.0로 빌드돼 있어 2.2.10 컴파일러가 못 읽음)
            // implementation(libs.ktor.client.darwin)
        }
    }
}
