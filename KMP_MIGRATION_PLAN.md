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
| Retrofit `2.11.0` + OkHttp `4.12.0` | REST API 통신 | **Ktor Client `3.3.3`** | 가장 큰 작업. `data/remote/api/*ApiService.kt` 13개 + `data/repository/*ApiRepository.kt` 15개가 전부 대상. ⚠️ **버전 고정 필요**: Ktor `3.4.0`부터 iOS klib가 Kotlin `2.3.x` 컴파일러로 빌드돼 `2.2.10`과 ABI 불일치(2026-08-22 확인). `3.3.3`이 마지막 호환 버전 |
| `AuthInterceptor`, `TokenAuthenticator` (OkHttp) | 토큰 첨부·갱신 | Ktor `Auth` 플러그인 (Bearer + refresh 콜백) | 로직은 거의 그대로 옮겨 쓸 수 있음, API만 다름 |
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
- [ ] `AuthApiService`, `RecruitAuthSessionRepository`(로그인) — Retrofit → Ktor로 재작성
- [ ] `HomeScreen`, `HomeViewModel`, `LoginScreen`, `LoginViewModel` → `commonMain`으로 이동
- [ ] `PickiiBottomNav`, `theme/Color.kt`·`Theme.kt`·`Type.kt` → `commonMain`으로 이동 (Compose 테마는 대부분 그대로 포팅됨)
- [ ] Mac에서 `iosApp` Xcode 프로젝트 생성, `ComposeUIViewController`로 진입점 연결
- [ ] iOS 시뮬레이터에서 로그인 → 홈 화면까지 실제로 뜨는지 확인
- [x] 카카오 로그인 iOS 연동 스파이크 — **통과(2026-08-22)**. 인터페이스+Swift 구현체 주입 방식으로 실제 로그인·토큰 획득까지 확인. 백엔드 연동/세션/토큰 갱신은 범위 밖, Phase 5에서 정식 연동 시 처리. 상세는 `PROGRESS_kmp-migration.md` 참고

**이 단계가 끝나야 나머지 17개 화면 영역을 이식하는 데 드는 시간을 현실적으로 추정할 수 있습니다.**

### Phase 2 — 데이터 레이어 전면 교체
- [ ] Ktor Client 공통 설정 (`commonMain`): base URL, 직렬화, 로깅
- [ ] Ktor `Auth` 플러그인으로 `AuthInterceptor`/`TokenAuthenticator` 로직 이식 (토큰 자동 첨부 + 401 시 refresh)
- [ ] `data/remote/api/*ApiService.kt` 13개 전부 Ktor 기반으로 재작성
- [ ] `data/remote/dto/*.kt`는 그대로 유지 (kotlinx.serialization 기반이라 손댈 것 거의 없음)
- [ ] `data/repository/*ApiRepository.kt` 15개 전부 새 API 서비스 연결로 교체
- [ ] `TokenStore`, `DeviceIdProvider`, `SavedMeetingScheduleStore` — DataStore는 유지, 파일 경로 생성부만 `expect/actual`
- [ ] `data/remote/socket/ChatStompClient.kt` — `krossbow-websocket-okhttp` → `krossbow-websocket-ktor`
- [x] `java.time` 사용처(`DateFormatter`, `DateTimeExt`, `ScheduleRecurrence` 등) → `kotlinx-datetime`으로 교체 — Phase 1 마무리하면서 앞당겨 완료(2026-08-22). 66개 파일 전환, 특성화 테스트 2개로 동작 동일함 검증. 발견한 이상한 점/개선 메모는 `PROGRESS_kmp-migration.md` 3번 참고
- [ ] Koin 모듈로 DI 전면 전환 완료 (`di/NetworkModule.kt`, `di/RepositoryModule.kt`, `di/CalendarRepositoryModule.kt`)

### Phase 3 — 리소스 시스템 이식
- [ ] `res/values/strings.xml`(32KB, 문자열 규모 큼) → `commonMain/composeResources/values/strings.xml`
- [ ] `res/drawable/*.xml`(아이콘 26개), `res/drawable/*.png`(레벨 고양이 이미지 등) → `commonMain/composeResources/drawable/`
- [ ] 코드 전체에서 `stringResource(R.string.x)` → `stringResource(Res.string.x)` 치환 (화면 수가 많아 기계적 치환 스크립트 권장)
- [ ] `material-icons-extended` import 경로 전환

### Phase 4 — 화면 이식 (권장 순서)
지금 구조가 이미 화면 단위(`ui/기능명/`)로 잘 나뉘어 있어서, 복잡도가 낮은 것부터 순서대로 옮기는 걸 권장합니다.

1. **낮은 난이도 (먼저)**: `splash`, `onboarding`, `signup`, `passwordreset`, `notification`, `memberprofile`
2. **중간 난이도**: `mypage/*`(home, profile, applications, mycomments, myrecruits, scraps, settings, withdrawal — 8개 하위 화면), `feedback`, `applicant`
3. **모집글 도메인**: `recruitdetail`, `recruitapply`, `recruitform` — 폼 검증 로직이 있지만 순수 Compose+상태라 이식 자체는 무난
4. **캘린더**: `calendar/monthly`, `calendar/daily`, `calendar/editor`, `calendar/category` — 반복 일정 로직(`ScheduleRecurrence`)이 `kotlinx-datetime` 전환과 맞물려서 Phase 2와 함께 검토
5. **채팅 (마지막, 가장 어려움)**: `chat/room`(`ChatRoomScreen.kt` 46KB, `ChatRoomViewModel.kt` + `+MeetingPoll.kt` 합쳐 50KB — 프로젝트에서 가장 큰 화면), `chat/list`, `chat/panel`, `chat/meeting`, `chat/photo`. WebSocket 실시간성 + 사진 업로드(`CameraCaptureUtil`, `GalleryPickerBottomSheet`) + 회의 투표 카드까지 얽혀 있어 별도 일정으로 분리 권장

- [ ] 화면 이식할 때마다 Android/iOS 양쪽에서 실제 확인 (에뮬레이터만 보고 "됐다"고 판단하지 않기 — 이전 대화에서 나온 애니메이션 체감 이슈도 실기기 기준으로 재확인)

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
- [ ] Phase 3 — 리소스 시스템 이식
- [ ] Phase 4 — 화면 19개 영역 순차 이식 (채팅은 마지막)
- [ ] Phase 5 — 카카오 로그인·푸시·카메라 등 플랫폼 전용 기능
- [ ] Phase 6 — QA
- [ ] Phase 7 — 배포

---

## 참고 자료

- [Compose Multiplatform for iOS is Now Stable](https://medium.com/@rushabhprajapati20/compose-multiplatform-for-ios-is-now-stable-bf4e2fc35596)
- [Navigation in Compose | Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform/compose-navigation.html)
- [Navigation 3 in Compose Multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html)
- [GitLive firebase-kotlin-sdk](https://github.com/GitLiveApp/firebase-kotlin-sdk)
- [Kotlin Multiplatform push notifications: a complete guide 2026](https://www.kmpship.app/blog/kotlin-multiplatform-push-notifications-guide-2026)
- [Kakao Login iOS 공식 문서](https://developers.kakao.com/docs/latest/en/kakaologin/ios)
- [Use platform-specific APIs (expect/actual) | Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform/multiplatform-connect-to-apis.html)
