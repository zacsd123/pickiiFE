# 📐 Android Kotlin 코드 컨벤션

> 이 문서는 프로젝트의 코드 일관성을 위해 반드시 지켜야 하는 규칙을 정리한 문서입니다.
> 새로운 규칙이 필요하면 팀 논의 후 이 문서에 추가합니다.

---

## 1. 네이밍 규칙

### 1.1 Kotlin 코드

| 대상 | 규칙 | 예시 |
|---|---|---|
| 클래스 / 인터페이스 | PascalCase | `LoginViewModel`, `UserRepository` |
| 함수 / 변수 | camelCase | `fetchUserData()`, `isLoggedIn` |
| 상수 (`const val`, `companion object` 내부) | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `BASE_URL` |
| 제네릭 타입 | 대문자 한 글자 | `T`, `K`, `V` |
| Boolean 변수 | is / has / can 접두사 | `isLoading`, `hasPermission`, `canSubmit` |

- 축약어는 사용하지 않는다. (`usr` ❌ → `user` ⭕)
- 함수 이름은 동사로 시작한다. (`userData()` ❌ → `getUserData()` ⭕)

### 1.2 리소스 파일

| 리소스 | 규칙 | 예시 |
|---|---|---|
| Layout | `{화면유형}_{화면이름}.xml` | `activity_login.xml`, `fragment_home.xml`, `item_user.xml`, `dialog_confirm.xml` |
| Drawable | `{유형}_{이름}.xml` | `ic_arrow_back.xml`, `bg_button_rounded.xml`, `selector_tab.xml` |
| View ID | `{뷰타입}_{이름}` (camelCase) | `btnLogin`, `tvUserName`, `rvPostList`, `etEmail` |
| String | `{화면}_{용도}` | `login_error_empty_email`, `home_title` |
| Color | 색상 역할 기반 | `primary`, `text_secondary`, `error` (색상값 이름 `red_500` ❌) |

### 1.3 패키지 / 파일

- 패키지 이름은 모두 소문자, 언더스코어 없이 작성한다. (`ui.login` ⭕, `ui.Login` ❌)
- 파일 이름은 그 안의 대표 클래스 이름과 동일하게 한다. (`LoginViewModel.kt`)

---

## 2. 프로젝트 구조

**기능(feature) 기반 구조**를 사용한다. 새 화면을 추가할 때는 `ui/` 아래에 기능 폴더를 새로 만든다.

```
com.example.app
├── data
│   ├── local          # Room DAO, DataStore
│   ├── remote         # Retrofit API, DTO
│   └── repository     # Repository 구현체
├── domain
│   ├── model          # 앱 내부에서 쓰는 순수 데이터 모델
│   └── repository     # Repository 인터페이스
├── ui
│   ├── login          # 화면 단위 폴더 (Activity/Fragment + ViewModel + Adapter)
│   ├── home
│   └── common         # 여러 화면에서 공유하는 커스텀 뷰, BaseActivity 등
├── di                 # Hilt 모듈
└── util               # 확장 함수, 공통 유틸
```

규칙:
- DTO(서버 응답 모델)는 `data/remote`에만 두고, UI에서는 반드시 `domain/model`로 변환해서 사용한다.
- 한 화면에 관련된 파일(Fragment, ViewModel, Adapter)은 같은 폴더에 둔다.

---

## 3. 아키텍처 규칙 (MVVM)

- **MVVM + Repository 패턴**을 따른다.
- 데이터 흐름은 단방향으로: `View → ViewModel → Repository → DataSource`
- **View (Activity/Fragment)**
    - UI 표시와 사용자 입력 전달만 담당한다. 비즈니스 로직 금지.
    - ViewModel의 상태(`StateFlow`/`LiveData`)를 관찰(observe)해서 화면을 갱신한다.
- **ViewModel**
    - `Activity`/`Fragment`/`View`/`Context`를 직접 참조하지 않는다. (메모리 릭 방지)
    - UI 상태는 `StateFlow`로 노출하고, 외부에는 읽기 전용(`asStateFlow()`)으로 공개한다.
    - 코루틴은 `viewModelScope`에서 실행한다.
- **Repository**
    - 데이터 출처(서버/로컬)를 ViewModel이 모르게 감춘다.
    - 인터페이스는 `domain`, 구현체는 `data`에 둔다.
- 의존성 주입은 **Hilt**를 사용한다. 직접 객체 생성(`Repository()`) 대신 생성자 주입.

---

## 4. 코드 스타일

- [Kotlin 공식 코딩 컨벤션](https://kotlinlang.org/docs/coding-conventions.html)을 기본으로 따른다.
- 코드 스타일은 **ktlint**로 강제한다. 커밋 전 `./gradlew ktlintFormat` 실행.
- 들여쓰기: 스페이스 4칸, 최대 줄 길이: 120자
- import 시 와일드카드(`import java.util.*`) 사용 금지.
- `if-else`로 값을 대입할 때는 `when` 또는 표현식 형태를 우선 고려한다.
- 널 처리:
    - `!!` 사용 금지. `?.`, `?:`, `requireNotNull()`로 대체한다.
    - 널이 될 이유가 없는 값은 처음부터 non-null 타입으로 선언한다.
- `var`보다 `val`을 우선 사용한다.
- data class는 데이터 보관 용도로만 사용하고 로직을 넣지 않는다.
- 매직 넘버 금지. 의미 있는 상수로 추출한다. (`if (type == 2)` ❌ → `if (type == TYPE_PREMIUM)` ⭕)

---

## 5. Git 규칙

### 5.1 브랜치

```
main        # 배포 가능한 상태만 유지
develop     # 개발 통합 브랜치
feature/*   # 기능 개발  → feature/login
fix/*       # 버그 수정  → fix/crash-on-splash
refactor/*  # 리팩토링   → refactor/user-repository
```

- `main`, `develop`에 직접 push 금지. 반드시 PR을 통해 병합한다.

### 5.2 커밋 메시지 (Conventional Commits)

```
feat: 로그인 화면 UI 구현
fix: 홈 화면 진입 시 크래시 수정
refactor: UserRepository 인터페이스 분리
docs: README 실행 방법 추가
chore: 라이브러리 버전 업데이트
style: 코드 포맷팅 (로직 변경 없음)
test: LoginViewModel 테스트 추가
```

- 제목은 50자 이내, 무엇을 왜 했는지 알 수 있게 작성한다.
- 하나의 커밋에는 하나의 작업만 담는다.

### 5.3 PR

- PR 제목은 커밋 컨벤션과 동일한 형식으로 작성한다.
- PR 본문에 작업 내용, 스크린샷(UI 변경 시), 테스트 방법을 적는다.

---

## 6. 금지 / 주의 사항

- 🚫 **하드코딩 문자열 금지** — 사용자에게 보이는 모든 텍스트는 `strings.xml`에 정의한다.
- 🚫 **API 키, 시크릿을 코드에 직접 작성 금지** — `local.properties` + `BuildConfig`로 관리하고, `local.properties`는 `.gitignore`에 포함한다.
- 🚫 **메인 스레드에서 네트워크 / DB 접근 금지** — 반드시 코루틴(`Dispatchers.IO`)에서 수행한다.
- 🚫 **`GlobalScope` 사용 금지** — `viewModelScope`, `lifecycleScope`를 사용한다.
- 🚫 **Fragment에서 `onDestroyView` 이후 binding 접근 금지** — `_binding = null` 처리 필수.
- 🚫 **하드코딩된 dp/sp 크기 남용 주의** — 반복되는 값은 `dimens.xml`로 추출한다.
- 🚫 **주석 처리된 죽은 코드 커밋 금지** — 필요 없는 코드는 삭제한다. (Git에 기록이 남는다)
- ⚠️ Context가 필요한 경우 Activity Context와 Application Context를 구분해서 사용한다.
- ⚠️ RecyclerView Adapter에는 `ListAdapter + DiffUtil`을 사용한다. (`notifyDataSetChanged()` 지양)

---

## 7. 주석 규칙

**모든 함수에는 어떤 기능을 하는지 설명하는 주석을 작성한다.** KDoc 형식(`/** */`)을 사용한다.

```kotlin
/**
 * 이메일과 비밀번호로 로그인을 요청하고 결과를 UI 상태에 반영한다.
 *
 * @param email 사용자 이메일
 * @param password 사용자 비밀번호
 */
fun login(email: String, password: String) {
    viewModelScope.launch {
        // ...
    }
}
```

- 한 줄로 충분한 간단한 함수는 `@param` 없이 설명 한 줄만 작성해도 된다.

```kotlin
/** 입력된 이메일이 올바른 형식인지 검사한다. */
fun isValidEmail(email: String): Boolean = ...
```

- 함수 내부 주석은 "무엇을"이 아니라 "왜"를 설명할 때만 작성한다.
    - `// i를 1 증가시킨다` ❌ (코드만 봐도 안다)
    - `// 서버가 0-based index를 요구해서 -1 처리` ⭕
- `TODO`, `FIXME` 태그 규칙:
    - `// TODO: 페이징 처리 추가 필요` — 나중에 할 작업
    - `// FIXME: 특정 기기에서 키보드가 안 닫히는 문제` — 알려진 버그