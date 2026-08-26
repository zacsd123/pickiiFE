# Pickii iOS 마이그레이션 플랜

`C:\Pickii` 프로젝트(패키지 `com.example.pickii`, 단일 `:app` 모듈, Kotlin 2.2.10 / AGP 9.3.0 / Compose BOM 2026.02.01)를 실제로 살펴보고 짠 계획입니다. 화면 19개 영역, API 저장소 15개, 서비스 인터페이스 13개 규모의 앱이라 "한 번에 이식"보다 **걷기 → 뛰기** 순서로 리스크를 나눠서 진행하는 편이 안전합니다.

이 문서는 체크리스트로 쓰도록 만들었습니다. 진행하면서 `- [ ]`를 `- [x]`로 바꿔가며 쓰세요.

---

## 0. 시작 전에 반드시 확인할 것

> [!IMPORTANT]
> **iOS 빌드·실행·서명은 Mac 없이는 불가능합니다.** 지금 연결된 개발 PC(`mhfirstpc`)는 Windows예요. Kotlin/Native의 iOS 타깃 컴파일과 Xcode 툴체인은 macOS에서만 동작하기 때문에, 코드 작성 자체는 계속 Windows + Android Studio에서 할 수 있어도 **iOS 시뮬레이터 실행, 실기기 테스트, TestFlight/App Store 배포 시점에는 Mac(직접 소유 또는 클라우드 Mac — MacStadium, GitHub Actions macOS 러너 등)이 필요**합니다. 이 부분은 일정에 가장 먼저 반영해두세요.

- [ ] Mac 확보 방안 결정 (중고 Mac mini / 지인 Mac 대여 / MacStadium·Xcode Cloud 같은 클라우드 Mac)
- [ ] Apple Developer Program 계정 가입 ($99/년) — 실기기 테스트, 푸시 알림(APNs), 배포에 필요
- [ ] 현재 프로젝트에 실제 유닛 테스트가 없다는 점 인지 (`ExampleUnitTest`, `ExampleInstrumentedTest`만 존재) — 마이그레이션 중 "예전과 똑같이 동작하는지" 확인할 안전망이 없다는 뜻이라, 최소한 세션/인증·모집글 CRUD 같은 핵심 리포지토리 로직에는 이식 전에 특성화 테스트(characterization test)를 몇 개 추가해두는 걸 권장

---

## 1. 목표 아키텍처

지금은 `:app` 하나뿐인 순수 Android 모듈입니다. 최종 형태는 이렇게 됩니다.

```
Pickii/
├── shared/                     # 새로 추가하는 KMP 모듈
│   ├── commonMain/             # domain, UI(Compose), data 로직 대부분 — Android/iOS 공통
│   ├── androidMain/            # Android 전용 구현체 (Kakao Android SDK, FCM 등)
│   └── iosMain/                # iOS 전용 구현체 (Kakao iOS SDK, APNs 등)
├── androidApp/                 # 기존 :app을 얇게 — Activity + shared 참조만 남김
└── iosApp/                     # 새로 추가하는 Xcode 프로젝트 (Mac에서 생성)
```

`domain/`(모델 + 리포지토리 인터페이스)은 이미 순수 Kotlin이라 `commonMain`으로 옮기는 데 거의 손댈 게 없습니다. 반대로 `data/`는 지금 Retrofit·OkHttp·Hilt처럼 **Android/JVM 전용 라이브러리에 강하게 묶여 있어서** 이 프로젝트에서 가장 큰 작업량은 UI 이식이 아니라 데이터 레이어 교체가 될 겁니다.

---

## 2. 라이브러리 교체표

프로젝트에 실제로 들어있는 의존성 기준입니다.

| 현재 (`libs.versions.toml`) | 용도 | KMP 대안 | 비고 |
|---|---|---|---|
| Hilt `2.60.1` + KSP | DI | **Koin `4.1.1`** | Hilt는 어노테이션 프로세서 기반이라 Kotlin/Native(iOS) 미지원. Koin은 런타임 DI라 멀티플랫폼 공식 지원. `@HiltViewModel` 쓰는 화면 전부(거의 모든 화면) 수정 필요. ⚠️ **버전 고정 필요**: 프로젝트 Kotlin `2.2.10`이 소비 가능한 klib ABI는 `<=2.2.0`인데 Koin `4.2.0`부터 iOS klib가 Kotlin `2.3.x` 컴파일러로 빌드돼 있어 못 읽음(2026-08-22 확인, `PROGRESS_kmp-migration.md` 참고). `4.1.1`이 마지막 호환 버전 |
| ~~Retrofit `2.11.0`~~ | REST API 통신 | **Ktor Client `3.3.3`** | ✅ **완료(2026-08-24)**. `data/remote/api/*ApiService.kt` 13개 전부 Ktor로 재작성, `data/repository/*ApiRepository.kt` 13개 전부 새 서비스로 교체. Retrofit 의존성 자체를 프로젝트에서 완전히 제거함(`retrofit-core`, `retrofit-kotlinx-serialization-converter`). OkHttp는 아직 남아있음(아래 참고) |
| ~~`AuthInterceptor`, `TokenAuthenticator`~~ (OkHttp) | 토큰 첨부·갱신 | Ktor `Auth` 플러그인 (Bearer + refresh 콜백) | ✅ **완료(2026-08-24)**. 두 클래스 전부 삭제, `HttpClientFactory`의 Ktor `Auth` 플러그인이 전담. `okhttp-logging-interceptor`도 같이 제거(GitHub 이슈 #47 해소). OkHttpClient 싱글턴 자체는 `ChatStompClient`(Krossbow websocket-okhttp)가 아직 써서 남아있음 — 아래 Krossbow 행 참고 |
| `kotlinx.serialization` | JSON | 그대로 유지 | 이미 멀티플랫폼 라이브러리 |
| Krossbow `9.3.0` (`stomp-kxserialization-json` + `websocket-okhttp`) | 채팅 WebSocket(STOMP) | STOMP 부분은 그대로, `websocket-okhttp` → **`krossbow-websocket-ktor`** | Krossbow 자체는 이미 KMP 라이브러리. WebSocket 엔진만 OkHttp 전용에서 Ktor 엔진(Android=OkHttp, iOS=Darwin)으로 교체. ⚠️ **버전 고정 필요**: 최신 `10.0.0`의 iOS klib는 Kotlin `2.4.10` 컴파일러로 빌드돼 있어 프로젝트 Kotlin `2.2.10`과 불일치(2026-08-22 확인). `9.3.0`이 마지막 호환 버전(컴파일러 `2.1.20`) — `stomp-core`/`stomp-kxserialization-json`/`websocket-ktor` 3개 아티팩트 전부 확인함. 10.0.0은 Ktor 2 레거시 지원 제거가 주 변경점이라 어차피 Ktor 3만 쓰는 이 프로젝트엔 영향 없음, 대안 라이브러리 필요 없음 |
| Coil3 (`coil-compose`, `coil-network-okhttp`) | 이미지 로딩 | 그대로 + **`coil-network-ktor3`** | 이미 `io.coil-kt.coil3` 그룹이라 멀티플랫폼 버전을 쓰고 있음. 네트워크 엔진만 교체 |
| `androidx.datastore.preferences` | 토큰·설정 저장 | 그대로 유지 | 1.1.0부터 공식 멀티플랫폼 지원. `TokenStore` 등은 파일 경로 생성 부분만 `expect/actual` 필요 |
| Navigation Compose `2.9.8` | 화면 내비게이션 | 그대로 유지 (버전 확인) | androidx Navigation이 최근 멀티플랫폼 아티팩트를 지원 시작. Compose Multiplatform 1.10(2026-01)부터 Navigation 3 공식 지원도 추가됨 — 지금 버전이 멀티플랫폼 타깃을 인식 못 하면 Navigation 3로 갈아타는 것도 검토 |
| `androidx.compose.material.icons.extended` | 아이콘 | **`compose.materialIconsExtended`** (JetBrains CMP 아티팩트) | 그룹이 다름, import 경로만 바뀜 |
| Firebase Messaging (`firebase-bom`, `firebase-messaging`) + `google-services.json` | 푸시 알림 | **GitLive `firebase-kotlin-sdk`** (`dev.gitlive:firebase-messaging`) 또는 **KMPNotifier** | Firebase 공식 Android SDK는 iOS 미지원. iOS는 APNs 인증서 발급까지 별도로 필요 (Apple Developer 계정 필수) |
| Kakao SDK (`v2-user`) | 카카오 로그인 | 공식 KMP 아티팩트 없음 → **인터페이스(commonMain) + Swift 구현체 주입(iosApp)** | ✅ **스파이크 완료(2026-08-22), 실제 로그인 통과.** 카카오 iOS SDK(2.28.0)가 SPM 전용 배포되는 순수 Swift 패키지라 Kotlin/Native cinterop(당초 검토한 방식)은 사실상 불가능함을 확인 — 대신 `KakaoAuthBridge` Kotlin 인터페이스를 정의하고 `iosApp`의 Swift(`KakaoAuthBridgeImpl.swift`)가 실제 SDK를 호출해 구현체를 주입하는 방식으로 연동, 시뮬레이터에서 실제 액세스 토큰 획득까지 확인함. androidMain은 기존 Kakao Android SDK 그대로 같은 인터페이스로 구현하면 됨. 상세 내용은 `PROGRESS_kmp-migration.md` 참고 |
| `coreLibraryDesugaring` (java.time) | 날짜/시간 | **kotlinx-datetime** | desugaring은 Android 전용 트릭이라 iOS에서 안 통함. `LocalDate`/`LocalDateTime` 쓰는 `DateFormatter`, `DateTimeExt`, `ScheduleRecurrence` 등 전부 교체 |
| `CameraCaptureUtil`, `GalleryPickerBottomSheet`, `PhotoSourceBottomSheet` | 카메라/갤러리 | KMP 미디어 피커 라이브러리(예: Peekaboo) 또는 자체 `expect/actual` | `ActivityResultContracts`·`ContentResolver` 등 Android 전용 API 사용 중 |
| `res/values/strings.xml`, `res/drawable/*.xml`, `res/drawable/*.png` | 문자열·아이콘·이미지 리소스 | Compose Multiplatform 리소스 시스템 (`commonMain/composeResources/`) | 벡터 XML·PNG는 대체로 그대로 옮겨지고, `stringResource(R.string.x)` → `stringResource(Res.string.x)`로 호출부만 수정 |
| Hilt `SavedStateHandle` 주입 (`postId` 등) | 내비게이션 인자 전달 | Koin의 SavedStateHandle 연동 또는 Navigation 인자로 직접 전달 | 화면별 ViewModel 생성자 패턴 재검토 필요 |

---

## 3. 단계별 실행 계획

### Phase 0 — 준비 (착수 전)
- [ ] 위 "0. 시작 전에 반드시 확인할 것" 항목 완료
- [ ] IntelliJ IDEA 또는 최신 Android Studio에 Kotlin Multiplatform 플러그인 설치
- [ ] 새 브랜치 생성 (`feature/kmp-migration` 등), `develop` 최신 기준으로 시작 — `PROGRESS.md`에 이미 적혀 있는 팀 규칙 그대로 적용

### Phase 1 — 워킹 스켈레톤 (가장 중요한 단계)
전체 화면을 다 옮기기 전에, **로그인 → 홈 화면 2개만** 최소 기능으로 iOS까지 띄워보는 검증 단계입니다. 여기서 Koin·Ktor·Compose Multiplatform 리소스 시스템·Xcode 연동까지 한 번씩 다 건드려보고, 진짜 막히는 지점(특히 카카오 로그인)을 미리 찾아냅니다.

- [x] `shared` KMP 모듈 생성, `commonMain`/`androidMain`/`iosMain` 소스셋 구성 (AGP 9는 `com.android.library`+`androidTarget()` 조합이 막혀 있어 `com.android.kotlin.multiplatform.library` 플러그인 사용. Ktor `HttpClient` 팩토리(`network/HttpClientFactory.kt`, engine expect/actual)와 Koin 자리(`di/SharedModule.kt`, 아직 앱에 미연결)까지 배선 완료. Windows에는 Xcode가 없어 iOS 네이티브 타깃은 자동 비활성화됨 — 정상, `gradle.properties`에 `kotlin.native.ignoreDisabledTargets=true`로 경고만 숨김)
- [ ] `domain/model`, `domain/repository` 전체를 `commonMain`으로 이동 (거의 수정 없이 컴파일될 것) — **부분 완료, 계획 재검토 필요.** 실제로 확인해보니 domain 레이어의 상당수(`RecruitPost`, `MemberProfile`→`ExperienceEntry`/`LicenseEntry`, `NotificationEntry` 등)가 `java.time.LocalDate`/`LocalDateTime`/`YearMonth`를 직접 참조하고, 이걸 쓰는 `DateFormatter`/`DateTimeExt` 유틸과 UI 화면 55개 파일까지 얽혀 있어 "거의 수정 없이"는 사실이 아니었음. 날짜 의존성이 전혀 없는 부분만 우선 이동함:
  - 이동 완료: `SessionRepository`, `MasterDataRepository` (인터페이스) + `CurrentUser`, `CampusScope`, `RecruitCategory`, `RecruitTopic`, `RecruitStatus`, `ProjectCreationResult`, `ApplyKeywordCategory`, `LicenseOption`, `LinkCategory`, `TechStack`, `University` (모델)
  - 미이동(java.time 걸림): `ProfileRepository`(→`MemberProfile`), `RecruitRepository`, `NotificationRepository` 등 — Login/Home 화면이 실제로 필요로 하는 나머지 리포지토리. kotlinx-datetime(버전 0.8.0, `YearMonth` 포함해서 새로 추가됨)으로 프로젝트 전역 날짜 타입을 바꾸는 작업(Phase 2 항목)을 먼저 하거나, 최소한 이 리포지토리들이 의존하는 범위만 먼저 손대는 결정이 필요함
- [ ] Hilt → Koin 전환 (우선 로그인/홈 관련 모듈만) — 위 블로커 때문에 보류. `SharedModule.kt`에 빈 Koin 모듈만 만들어둠(앱에는 미연결, Hilt가 계속 전체 DI 담당 중)
- [x] `AuthApiService`, `RecruitAuthSessionRepository`(로그인) — Retrofit → Ktor로 재작성. Phase 1이
  의도했던 "로그인/홈만 좁게" 범위가 아니라 Phase 2의 13개 서비스 전체 스윕(2026-08-24)으로 같이
  완료됨
- [ ] `HomeScreen`, `HomeViewModel`, `LoginScreen`, `LoginViewModel` → `commonMain`으로 이동
- [x] `theme/Color.kt`·`Theme.kt`·`Type.kt` → `commonMain`으로 이동 — Color.kt는 SplashScreen 카나리아(2026-08-25)에서 먼저 옮겨짐, Theme.kt/Type.kt는 이번(2026-08-26)에 이동. `dynamicDarkColorScheme`/`dynamicLightColorScheme`/`LocalContext`가 Android 전용 API라 commonMain에서 직접 못 불러 `dynamicColorScheme(darkTheme): ColorScheme?` expect/actual(androidMain은 실제 계산, iosMain은 `null`)로 감쌌음 — `supportsDynamicColor()` 하나만으로는 안 되고, 다이나믹 컬러 계산 자체를 expect/actual로 빼야 iOS 타깃이 컴파일됨. iOS 진입점(`MainViewController.kt`)이 `PickiiTheme { SplashScreen() }`을 그리도록 바꿔 시뮬레이터에서 테마 적용 확인(노란 그라디언트+Bold 타이포 정상 렌더링). 카운터/카카오 스파이크 화면(`SpikeScreen.kt`)은 목적을 다해 제거하고 그 자리를 이 카나리아가 대체함 — 관련 UI 테스트도 `SplashScreenUITests.swift`로 교체
- [ ] `PickiiBottomNav`(`ui/common/`) → `commonMain`으로 이동 (남은 화면 이식과 함께, 아래 4-1 표 참고)
- [ ] Mac에서 `iosApp` Xcode 프로젝트 생성, `ComposeUIViewController`로 진입점 연결
- [ ] iOS 시뮬레이터에서 로그인 → 홈 화면까지 실제로 뜨는지 확인
- [x] 카카오 로그인 iOS 연동 스파이크 — **통과(2026-08-22)**. 인터페이스+Swift 구현체 주입 방식으로 실제 로그인·토큰 획득까지 확인. 백엔드 연동/세션/토큰 갱신은 범위 밖, Phase 5에서 정식 연동 시 처리. 상세는 `PROGRESS_kmp-migration.md` 참고

**이 단계가 끝나야 나머지 17개 화면 영역을 이식하는 데 드는 시간을 현실적으로 추정할 수 있습니다.**

### Phase 2 — 데이터 레이어 전면 교체
- [x] Ktor Client 공통 설정 (`commonMain`): base URL, 직렬화, 로깅 — `data/remote/HttpClientFactory.kt`
  (2026-08-23~24, `enableBodyLogging` + auth/ 경로 제외 + Authorization 마스킹까지 포함)
- [x] Ktor `Auth` 플러그인으로 `AuthInterceptor`/`TokenAuthenticator` 로직 이식 (토큰 자동 첨부 + 401 시 refresh) —
  완료(2026-08-24). 두 클래스는 완전히 삭제됨. `BearerAuthRefreshSpikeTest`/`HttpClientFactoryAuthTest`로
  갱신·순환 방지(`markAsRefreshTokenRequest()`) 실측 검증 완료. 다만 실기기/에뮬레이터에서 실제 401→갱신
  흐름 관찰은 아직 못 함(`PROGRESS_kmp-migration.md`의 미검증 항목 참고)
- [x] `data/remote/api/*ApiService.kt` 13개 전부 Ktor 기반으로 재작성 — 완료(2026-08-24). Auth,
  Project, NotificationSettings, Applicant, MasterData, Notification, Profile, MyPageActivity,
  Feedback, Calendar, MeetingPoll, Recruit, Chat(멀티파트 이미지 업로드 포함) 전부
- [x] `data/remote/dto/*.kt`는 그대로 유지 (kotlinx.serialization 기반이라 손댈 것 거의 없음)
- [x] `data/repository/*ApiRepository.kt` 13개 전부 새 API 서비스 연결로 교체 — 완료(2026-08-24)
- [ ] `TokenStore`, `DeviceIdProvider`, `SavedMeetingScheduleStore` — DataStore는 유지, 파일 경로 생성부만 `expect/actual`
- [ ] `data/remote/socket/ChatStompClient.kt` — `krossbow-websocket-okhttp` → `krossbow-websocket-ktor`
  (Retrofit 제거 시점(2026-08-24)에는 손대지 않음 — OkHttpClient 싱글턴이 아직 이걸 위해 남아있음)
- [x] `java.time` 사용처(`DateFormatter`, `DateTimeExt`, `ScheduleRecurrence` 등) → `kotlinx-datetime`으로 교체 — Phase 1 마무리하면서 앞당겨 완료(2026-08-22). 66개 파일 전환, 특성화 테스트 2개로 동작 동일함 검증. 발견한 이상한 점/개선 메모는 `PROGRESS_kmp-migration.md` 3번 참고
- [x] Koin 모듈로 DI 전면 전환 완료 (`di/NetworkModule.kt`, `di/RepositoryModule.kt`, `di/CalendarRepositoryModule.kt`) —
  Hilt·KSP 완전 제거 및 Retrofit 계열(Retrofit/AuthInterceptor/TokenAuthenticator/okhttp-logging-interceptor)
  전부 걷어낸 상태로 확인됨(2026-08-24)

### Phase 3 — 리소스 시스템 이식

> [!IMPORTANT]
> **`com.android.kotlin.multiplatform.library` + Compose Multiplatform 리소스 조합은 기본 설정으론
> Android에서 아예 안 뜬다.** Phase 3 착수 전 실측(2026-08-24)으로 발견: `compose.resources {}`를
> 설정하고 `composeResources/drawable`에 아이콘을 넣은 뒤 `painterResource()`로 불러오면 빌드는
> 통과하지만 런타임에 `org.jetbrains.compose.resources.MissingResourceException`으로 죽는다.
> 원인: `shared/build.gradle.kts`의 `android {}` 블록(= `com.android.kotlin.multiplatform.library`
> 플러그인이 제공하는 것 — AGP 9가 전통적인 `com.android.library`+`androidTarget()` 조합을 막아서
> 어쩔 수 없이 쓰고 있는 그 플러그인, 위 라이브러리 교체표 참고)이 기본적으로 Android 리소스 처리를
> 꺼둔 상태라, Compose Multiplatform의 리소스 복사 태스크(`copyAndroidMainComposeResourcesToAndroidAssets`)가
> `outputDirectory` 설정을 못 받아 조용히 실패한다.
>
> **알려진 이슈**: [CMP-9547](https://youtrack.jetbrains.com/issue/CMP-9547) (JetBrains YouTrack).
> "Answered" 상태로 종료 — 코드 수정이 아니라 워크어라운드 안내로 닫힘. JetBrains 담당자 코멘트:
> *"That's not our plugin, it's from Google."* — `com.android.kotlin.multiplatform.library`는
> AGP(Google) 소유라 CMP 쪽에서 고칠 수 있는 범위가 아니고, 2026-06-20 코멘트로도 AGP 9.1.0에서
> 여전히 재현된다고 확인됨 — **버전을 올려도 없어지는 문제가 아니라 opt-in 설정이 원래 필요한 것.**
>
> **해결(적용 완료, 2026-08-24)**: `shared/build.gradle.kts`의 `kotlin { android { ... } }` 블록
> 안에 `androidResources.enable = true` 한 줄 추가. 이 블록 이름이 문서상 `androidLibrary`로
> 나오지만 실제로는 `android`라는 이름으로도 동일하게 동작함을 실측 확인(별칭). 추가 후
> `ic_instagram.xml`/`ic_linkedin.xml`(gradient가 `<aapt:attr>`로 인라인된 벡터, 이번 조사에서
> 가장 위험하다고 판단했던 두 파일)을 에뮬레이터에서 실제로 렌더링해서 그라디언트까지 정상 출력됨을
> 확인 — **아이콘 자체는 문제 없었고, 문제는 전적으로 이 인프라 설정 하나였다.** `androidResources.enable`을
> 지우면 리소스 시스템 전체가 다시 조용히 죽으니, 나중에 "이거 왜 있지" 하고 지우지 말 것 — Ktor
> `TokenAuthenticator`의 `lazy {}` 순환 의존성 가드와 같은 성격의 "지우면 안 되는 한 줄"이다
> (`shared/build.gradle.kts`에 이유 주석 있음).
>
> **더 넓은 시사점**: `com.android.kotlin.multiplatform.library`는 AGP 9가 강제한 선택지이지 우리가
> 고른 게 아니다. 상대적으로 덜 다져진 조합이라, Compose Multiplatform 쪽 다른 기능(리소스뿐 아니라
> 이후 다른 영역)에서도 비슷한 마찰이 또 나올 가능성을 염두에 둘 것 — 뭔가 "빌드는 되는데 런타임에만
> 이상하게 죽는다" 싶으면 이 플러그인 조합부터 의심.

- [x] `res/values/strings.xml`(32KB, 문자열 규모 큼) → `commonMain/composeResources/values/strings.xml` — 완료(2026-08-24).
  374개 이동, `app_name`(AndroidManifest 참조)과 `general_notification_channel_name`(FcmService의
  동기 `Service#getString` 필요)은 CMP 리소스로 해석 불가해 `app/res/values/strings.xml`에 남김
- [x] `res/drawable/*.xml`(벡터 아이콘 31개), `res/drawable/*.png`(레벨 고양이 4종 + 준비중 마스코트) →
  `commonMain/composeResources/drawable/` — 완료(2026-08-24). `ic_launcher_background.xml`(adaptive
  icon 레이어)과 `ic_notification.xml`(`NotificationCompat.setSmallIcon()`이 진짜 Android 리소스 ID를
  요구 — CMP 쪽에도 사본을 남겨 Compose 아이콘으로는 계속 씀)은 `app/res`에 그대로 둠
- [x] 코드 전체에서 `stringResource(R.string.x)`/`painterResource(R.drawable.x)` →
  `Res.string.x`/`Res.drawable.x` 치환 — 완료(2026-08-24). Python 스크립트로 58개 파일 기계적 치환 +
  ViewModel/UiState가 들고 있던 Int 타입 리소스 ID(`toastMessageRes`, `loadFailureMessageRes`,
  `getLinkIcon` 반환 타입 등)를 `StringResource`/`DrawableResource`로 재설계. 커밋은 문자열/드로어블
  두 개로 분리했지만 둘 다 완전히 적용된 상태에서만 전체 모듈이 컴파일됨(Kotlin 전체 모듈 컴파일
  특성상 진짜 독립적으로 빌드되는 분할은 불가능했음)
- [x] `material-icons-extended` 제거 — 완료(2026-08-25). 실사용 아이콘이 36개뿐인데 CMP 대응
  아티팩트(`org.jetbrains.compose.material:material-icons-extended`)는 최신 버전이 1.7.3으로 CMP
  버전(1.10.3)보다 3단계 뒤처져 있고, 이번 마이그레이션에서 klib ABI 불일치로 이미 세 번(Koin/Ktor/
  Krossbow) 발목 잡혔던 전례가 있어 그 리스크를 다시 지지 않기로 하고 의존성 자체를 걷어냈다. Google
  공식 material-design-icons(Apache 2.0) SVG를 소스로 36개를 Android 벡터 XML로 변환해
  `composeResources/drawable`에 직접 추가하고, `Icons.Filled.X` 등 26개 파일의 64개 호출부를
  `painterResource(Res.drawable.ic_x)`로 교체했다. 라이선스 출처는 저장소 루트 `NOTICE` 파일에 기록.

### Phase 4 — 화면 이식 (권장 순서)
지금 구조가 이미 화면 단위(`ui/기능명/`)로 잘 나뉘어 있어서, 복잡도가 낮은 것부터 순서대로 옮기는 걸 권장합니다.

1. **낮은 난이도 (먼저)**: `splash`, `onboarding`, `signup`, `passwordreset`, `notification`, `memberprofile`
2. **중간 난이도**: `mypage/*`(home, profile, applications, mycomments, myrecruits, scraps, settings, withdrawal — 8개 하위 화면), `feedback`, `applicant`
3. **모집글 도메인**: `recruitdetail`, `recruitapply`, `recruitform` — 폼 검증 로직이 있지만 순수 Compose+상태라 이식 자체는 무난
4. **캘린더**: `calendar/monthly`, `calendar/daily`, `calendar/editor`, `calendar/category` — 반복 일정 로직(`ScheduleRecurrence`)이 `kotlinx-datetime` 전환과 맞물려서 Phase 2와 함께 검토
5. **채팅 (마지막, 가장 어려움)**: `chat/room`(`ChatRoomScreen.kt` 46KB, `ChatRoomViewModel.kt` + `+MeetingPoll.kt` 합쳐 50KB — 프로젝트에서 가장 큰 화면), `chat/list`, `chat/panel`, `chat/meeting`, `chat/photo`. WebSocket 실시간성 + 사진 업로드(`CameraCaptureUtil`, `GalleryPickerBottomSheet`) + 회의 투표 카드까지 얽혀 있어 별도 일정으로 분리 권장

- [ ] 화면 이식할 때마다 Android/iOS 양쪽에서 실제 확인 (에뮬레이터만 보고 "됐다"고 판단하지 않기 — 이전 대화에서 나온 애니메이션 체감 이슈도 실기기 기준으로 재확인)

#### 4-1. 남은 17개 영역 — androidx 의존성 전수 조사 (2026-08-26)

Theme.kt 이식 이후 남은 영역(splash·theme 제외, `mypage`/`calendar`/`chat`는 하위 화면 포함 재귀 조사)이
실제로 어떤 `androidx.*` import를 쓰는지 전부 grep으로 뽑고, 후보마다 로컬 Gradle 캐시의 klib 모듈
메타데이터(`~/.gradle/caches/modules-2`)를 뒤져 iOS 타깃(`iosarm64`/`iossimulatorarm64`/`iosx64` 또는
CMP의 `uikit*`) 아티팩트가 실제로 존재하는지 확인했다. 의심스러운 것은 `shared`에 임시 probe 파일을
만들어 `compileKotlinIosSimulatorArm64`/`compileDebugKotlin` 양쪽으로 실측 컴파일까지 돌려보고
지웠다 — 이 프로젝트가 지금까지 해온 "문서 대신 실측" 방식 그대로.

**이미 해결된 의존성 (컴파일 실측 완료, 그대로 옮기면 됨)**

| androidx 심볼 | 실제 아티팩트/경로 | 상태 |
|---|---|---|
| `compose.foundation`/`material3`/`runtime`/`ui` | `org.jetbrains.compose.*` accessor | ✅ Theme/Splash 카나리아로 이미 증명됨 |
| `androidx.compose.ui.tooling.preview.Preview` | `org.jetbrains.compose.ui:ui-tooling-preview` (좌표 직접 명시, 위 3번 패턴) | ✅ 기존 선언 그대로 |
| `androidx.lifecycle.compose.collectAsStateWithLifecycle`/`LocalLifecycleOwner` | `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.9.3` (미러) | ✅ 기존 선언 그대로 |
| `androidx.lifecycle.ViewModel`/`viewModelScope` | `androidx.lifecycle:lifecycle-viewmodel:2.9.4` (미러 불필요, 진짜 멀티플랫폼) | ✅ 기존 선언 그대로 |
| `androidx.lifecycle.Lifecycle`/`LifecycleEventObserver` | 위 lifecycle-runtime-compose가 전이 의존성으로 물고 옴 | ✅ 오늘 probe로 iOS+Android 둘 다 실측 확인, 추가 선언 불필요 |
| `androidx.compose.animation`(`.core`) — `AnimatedVisibility`/`animateColorAsState`/`animateDpAsState`/`spring` 등 | material3/foundation이 전이 의존성으로 물고 옴 | ✅ 오늘 probe로 iOS+Android 둘 다 실측 확인, **`compose.animation` accessor를 따로 안 붙여도 됨** |
| `androidx.lifecycle.SavedStateHandle` | 🆕 `androidx.lifecycle:lifecycle-viewmodel-savedstate:2.9.4` (lifecycle-viewmodel과 같은 버전, 역시 진짜 멀티플랫폼 — iOS klib 존재 확인) | ✅ 오늘 `libs.versions.toml`/`shared/build.gradle.kts`에 추가 + iOS 컴파일 실측 완료(별도 커밋 필요) |

**새 의존성이 필요했던 것 (오늘 추가 + 실측 완료, 실제 화면 카나리아는 아직)**

| androidx 심볼 | 문제 | 해결 |
|---|---|---|
| `androidx.activity.compose.BackHandler` (`feedback`, `applicant`, `recruitapply`에서 뒤로가기 처리용으로 씀) | `androidx.activity`는 그룹 전체가 iOS 타깃이 아예 없음(Android Activity 전용 API) | 🆕 CMP가 자체 멀티플랫폼 대체재를 이미 제공: `androidx.compose.ui.backhandler.BackHandler` (`@OptIn(ExperimentalComposeUiApi::class)` 필요, 상위 API에서 이미 `NavigationEventHandler`로 deprecated 표시되지만 아직 동작함). **주의**: CMP의 `compose.ui` accessor는 Android 타깃에서 진짜 AndroidX `ui` 아티팩트로 치환되는데, 그 아티팩트엔 `ui-backhandler`가 없어서 iOS에서만 되고 Android에서는 `Unresolved reference`로 깨짐 — `org.jetbrains.compose.ui:ui-backhandler:<버전>` 좌표를 **accessor 말고 직접** 선언해야 두 플랫폼 다 됨(2번 패턴과 같은 종류의 함정, 아래 4-2에 정리) |

**진짜 새 기능(라이브러리 교체가 아니라 플랫폼별 재구현, Phase 5 범위 — 변화 없음)**

| androidx 심볼 | 영역 | 비고 |
|---|---|---|
| `androidx.activity.result.contract.ActivityResultContracts`, `androidx.core.content.FileProvider`/`ContextCompat`, `ContentResolver` | `chat/room`, `chat/photo`(`GalleryPickerBottomSheet`, `PhotoSourceBottomSheet`) | `androidx.activity`/`androidx.core` 그룹 자체가 iOS 아티팩트가 없음(Gradle 캐시에 iOS 변형 0개, 확인 완료) — 라이브러리 좌표 교체로 안 되고 `expect/actual` + iOS는 `PHPickerViewController` 직접 구현 필요. 원래 계획(Phase 5)과 동일한 결론, 채팅을 마지막에 두는 이유가 다시 확인됨 |

**추가로 확인한 것(라이브러리 문제는 아니지만 이식 시 기계적으로 고쳐야 함)**
- `org.koin.androidx.compose.koinViewModel`(Android 전용) → `org.koin.compose.viewmodel.koinViewModel`(멀티플랫폼, Splash에서 이미 씀)로 import 한 줄 교체 — 남은 17개 영역 중 거의 전부(30개 파일)가 이 상태. 새 의존성은 아니고 이식 시 빠뜨리기 쉬운 기계적 치환이라 체크리스트에 남김
- 화면 자체는 `androidx.navigation.*`을 직접 import하지 않음(NavController는 전부 콜백 람다로만 전달받음) — Navigation-compose 자체의 iOS 실측은 `navigation`/`common`(NavHost)을 옮기는 마지막 단계에서 처리하면 됨

#### 4-2. 배치 계획

- **Batch 1 (이미 해결된 의존성만 사용, 바로 진행)**: `home`, `login`, `onboarding`, `signup`,
  `passwordreset`, `notification`, `memberprofile`, `common`(`PickiiBottomNav` 등 — 거의 모든 화면이
  참조하므로 먼저 옮기는 게 유리), `recruitdetail`, `recruitform`, `mypage/*`(8개), `calendar/*`(4개) —
  총 14개 영역. 여러 개씩 묶어서 진행
- **Batch 2 (새 의존성 붙이고 카나리아 검증 후 나머지)**: `ui-backhandler` 의존성은 이미 붙여놨으니(이번
  조사에서 실측 완료), `feedback`을 카나리아로 먼저 이식해 `BackHandler` import 교체가 실제 화면에서도
  문제없는지 확인한 뒤 `applicant`, `recruitapply` 진행
- **Batch 3 (Phase 5와 함께, 별도 일정)**: `chat/*`(5개) — 카메라/갤러리 피커 `expect/actual` 설계가
  먼저 끝나야 착수 가능. `navigation`(NavHost/`PickiiDestination`)은 나머지 18개를 다 옮긴 다음 마지막에

### Phase 5 — 플랫폼 전용 기능
- [ ] 카카오 로그인 iOS 정식 연동 (Phase 1 스파이크 결과 반영)
- [ ] Firebase Messaging → GitLive SDK 또는 KMPNotifier로 교체, iOS APNs 인증서 발급 및 연동
- [ ] 카메라/갤러리 피커 iOS 구현체 작성 (`PHPickerViewController` 연동)
- [ ] iOS 앱 아이콘, 런치 스크린, `Info.plist` 권한 문구(카메라/사진 라이브러리 접근 등) 설정

### Phase 6 — QA
- [ ] 두 플랫폼에서 동일 백엔드(`pikiibackend-production.up.railway.app`) 대상으로 전체 플로우 회귀 테스트
- [ ] 채팅 WebSocket 연결 끊김/재연결, 백그라운드 전환 시나리오 iOS에서 별도 검증 (Android와 백그라운드 동작 방식이 달라 여기서 버그가 잘 나옴)
- [ ] 푸시 알림 수신 확인 (Android FCM / iOS APNs 양쪽)
- [ ] 카카오 로그인 전체 플로우 iOS 실기기 확인

### Phase 7 — 배포 준비
- [ ] TestFlight 내부 테스트 배포
- [ ] App Store 심사 가이드라인 확인 (소셜 로그인 필수 시 "Sign in with Apple" 병행 요구 여부 — 카카오 로그인만 있으면 반려될 수 있어 미리 확인 필요)

---

## 4. 요약 체크리스트

- [ ] Phase 0 — Mac/Apple Developer 계정 확보
- [ ] Phase 1 — 로그인+홈 워킹 스켈레톤으로 전체 툴체인 검증
- [ ] Phase 2 — Retrofit/Hilt/OkHttp 계열 전면 교체
- [x] Phase 3 — 리소스 시스템 이식
- [ ] Phase 4 — 화면 19개 영역 순차 이식 (채팅은 마지막)
- [ ] Phase 5 — 카카오 로그인·푸시·카메라 등 플랫폼 전용 기능
- [ ] Phase 6 — QA
- [ ] Phase 7 — 배포

---

## 5. 반복되는 함정 (KMP 마이그레이션 패턴)

화면을 하나씩 옮길 때마다 매번 새로 부딪히는 게 아니라 이미 몇 번 확인된 패턴들. 새 화면에서 컴파일이
안 되거나 iOS에서만 깨질 때 여기부터 의심할 것.

1. **androidx 라이브러리는 iOS 타깃을 안 내는 경우가 많고, `org.jetbrains.androidx.*` 미러를 써야
   한다.** Navigation(`org.jetbrains.androidx.navigation:navigation-compose`),
   lifecycle-runtime-compose(`org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose`)에서 확인.
   **단, 전부 그런 건 아니다** — `lifecycle-viewmodel`, `lifecycle-viewmodel-savedstate`는 최근
   버전(2.9.x)부터 `androidx.lifecycle` 그룹 자체가 진짜 멀티플랫폼이라 미러가 필요 없다(2026-08-26
   확인). 새 androidx 라이브러리를 붙일 때마다 미러가 필요하다고 지레짐작하지 말고, 그룹 자체가 이미
   멀티플랫폼인지부터 Gradle 캐시(`~/.gradle/caches/modules-2/files-2.1/<group>/<artifact>-iossimulatorarm64`
   디렉터리 존재 여부)나 실제 iOS 컴파일로 먼저 확인할 것.
2. **`compose.components.uiToolingPreview`는 Gradle accessor가 iOS를 안 잡아서 좌표를 문자열로 직접
   박아야 한다** — `org.jetbrains.compose.ui:ui-tooling-preview:$버전`.
3. **Kotlin/Native는 `init`으로 시작하는 top-level 함수를 Swift에 `doInit*`로 리네임한다**(ObjC
   이니셜라이저 관례 회피). `initKoin()` → `doInitKoin()` — 문서에 없어서 헤더 까서 알아낸 것.
4. **CMP accessor(`compose.ui` 등)는 Android 타깃에서 진짜 AndroidX 아티팩트로 치환되는데, 그 진짜
   아티팩트엔 없고 CMP 전용 모듈에만 있는 것들이 있다** — 예: `androidx.compose.ui.backhandler.BackHandler`
   (`org.jetbrains.compose.ui:ui-backhandler`)가 그렇다. 이런 건 iOS에서는 accessor 경유로 전이
   의존성에 묻어와서 컴파일되는데 **Android에서는 `Unresolved reference`로 깨진다** — 2번 패턴과
   똑같이 좌표를 직접 선언해야 두 플랫폼 다 된다. "iOS는 됐는데 Android가 깨진다"는 순서로도 이 문제가
   나올 수 있다는 걸 기억할 것(지금까지는 반대 방향, "iOS가 깨진다"만 겪었음).
5. **androidx 그룹인데 iOS 타깃이 전혀 없는 것도 있다** — `androidx.activity`, `androidx.core`
   그룹은 통째로 Android 전용(Activity/Context에 강하게 묶인 API라 애초에 멀티플랫폼이 될 수 없음).
   이런 건 좌표를 아무리 바꿔도 안 되고 `expect/actual`로 플랫폼별 재구현이 필요하다 — 카메라/갤러리
   피커(`chat`)가 이 경우.
6. **Compose 안에도 Android에서만 컴파일되는 API가 있다** — 의존성 좌표 문제가 아니라 같은
   `androidx.compose.ui` 패키지 안에서도 생성자/오버로드 단위로 플랫폼이 갈린다. 예:
   `PlatformTextStyle(includeFontPadding = false)`는 Android 텍스트 렌더링의 폰트 패딩 보정용
   레거시 개념이라 iOS엔 그 생성자가 없다(`PickiiTopBar.kt`의 알림 뱃지 숫자에서 실측, `common` 이식
   중 발견). `expect fun noFontPaddingTextStyle(): PlatformTextStyle?`로 감싸고 androidMain은
   실제 보정값을, iosMain은 `null`(iOS는 애초에 이 보정이 필요한 폰트 패딩 문제가 없음)을 반환하게
   했다 — 시뮬레이터로 뱃지 숫자가 원 안에 중앙 정렬되는지 실제로 확인함. **이런 건 Android
   컴파일은 그냥 통과하고 iOS 컴파일에서만 깨지므로, 화면을 옮길 때 iOS 컴파일을 배치 끝까지 미루지
   말고 파일 단위로 자주 돌려볼 것.**

## 참고 자료

- [Compose Multiplatform for iOS is Now Stable](https://medium.com/@rushabhprajapati20/compose-multiplatform-for-ios-is-now-stable-bf4e2fc35596)
- [Navigation in Compose | Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)
- [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)
- [GitLive firebase-kotlin-sdk](https://github.com/GitLiveApp/firebase-kotlin-sdk)
- [Kotlin Multiplatform push notifications: a complete guide 2026](https://www.kmpship.app/blog/kotlin-multiplatform-push-notifications-guide-2026)
- [Kakao Login iOS 공식 문서](https://developers.kakao.com/docs/latest/en/kakaologin/ios)
- [Use platform-specific APIs (expect/actual) | Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform/multiplatform-connect-to-apis.html)
