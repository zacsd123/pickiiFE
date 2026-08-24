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

        withHostTestBuilder {}
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
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

// 실제 백엔드에 붙는 수동 통합 테스트(*BackendIntegrationTest)는 일반 테스트 실행에서 뺀다 —
// 네트워크가 필요해서 CI/로컬 어디서든 불안정해질 수 있다. ./gradlew :shared:backendIntegrationTest로만
// 실행한다. 자격증명 없으면 각 테스트가 스스로 스킵된다(AuthApiServiceBackendIntegrationTest 참고).
// testAndroidHostTest는 KMP 안드로이드 플러그인이 나중에(afterEvaluate 시점 이후) 등록하므로
// afterEvaluate 안에서 참조해야 한다.
afterEvaluate {
    val hostTest = tasks.named<Test>("testAndroidHostTest")

    hostTest.configure {
        filter {
            excludeTestsMatching("*BackendIntegrationTest*")
        }
    }

    tasks.register<Test>("backendIntegrationTest") {
        group = "verification"
        description = "실제 백엔드에 붙는 수동 통합 테스트만 실행한다. 자격증명 없으면 스킵됨."
        dependsOn("compileAndroidHostTest")
        testClassesDirs = hostTest.get().testClassesDirs
        classpath = hostTest.get().classpath
        // 커맨드라인에 비밀번호를 안 남기려고 환경변수 대신 gitignore된 local.properties 파일에서
        // 읽는다 — 경로를 시스템 프로퍼티로 전달(AuthApiServiceBackendIntegrationTest가 읽음).
        systemProperty("pickii.localPropertiesPath", rootProject.file("local.properties").absolutePath)
        // 이 태스크는 항상 새로 실행돼야 한다 — local.properties 내용 변경이나 백엔드 상태 변화를
        // Gradle의 UP-TO-DATE 체크가 감지할 방법이 없다. 실제로 systemProperty에 넘긴 건 "경로"
        // 문자열(항상 동일)뿐이라, 파일 내용만 바뀌면 Gradle이 "입력 변경 없음"으로 보고 이전
        // 실행 결과(전부 스킵)를 그대로 재사용해버리는 걸 실측으로 확인했다.
        outputs.upToDateWhen { false }
        testLogging {
            events("skipped", "passed", "failed")
            showStandardStreams = true
        }
        filter {
            includeTestsMatching("*BackendIntegrationTest*")
            isFailOnNoMatchingTests = false
        }
    }
}
